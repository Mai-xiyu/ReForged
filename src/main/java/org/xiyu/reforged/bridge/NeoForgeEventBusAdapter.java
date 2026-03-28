package org.xiyu.reforged.bridge;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;

/**
 * Wraps a Forge {@link net.minecraftforge.eventbus.api.IEventBus} and presents it as a
 * {@link net.neoforged.bus.api.IEventBus}.
 *
 * <p>Uses {@link java.lang.reflect.Proxy} to dynamically implement the interface,
 * delegating all calls to the underlying Forge event bus while also handling
 * NeoForge's {@code @SubscribeEvent} annotations.</p>
 *
 * <h3>Event wrapping</h3>
 * <p>NeoForge event shims (in package {@code net.neoforged}) may NOT extend Forge's
 * {@link Event} class. Instead, they wrap Forge events via a single-arg constructor.
 * When a NeoForge mod registers a handler for such a type, this adapter:</p>
 * <ol>
 *   <li>Discovers the Forge event type from the wrapper's constructor</li>
 *   <li>Registers a Forge-side listener for that type</li>
 *   <li>When the Forge event fires, instantiates the NeoForge wrapper and calls the handler</li>
 * </ol>
 */
public final class NeoForgeEventBusAdapter {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Create a dynamic proxy that implements {@code net.neoforged.bus.api.IEventBus}
     * and delegates all calls to the given Forge event bus.
     */
    public static net.neoforged.bus.api.IEventBus wrap(net.minecraftforge.eventbus.api.IEventBus delegate) {
        return (net.neoforged.bus.api.IEventBus) Proxy.newProxyInstance(
                NeoForgeEventBusAdapter.class.getClassLoader(),
                new Class<?>[]{net.neoforged.bus.api.IEventBus.class},
                (proxy, method, args) -> {
                    String name = method.getName();

                    // Special handling for register() — scan for NeoForge annotations
                    if ("register".equals(name) && args != null && args.length == 1) {
                        handleRegister(delegate, args[0]);
                        return null;
                    }

                    // Intercept ALL addListener() calls — NeoForge events need wrapping
                    // before they can be registered on the Forge bus.  Forge's EventBus
                    // cannot resolve NeoForge event types from Consumer generics.
                    if ("addListener".equals(name) && args != null && args.length > 0) {
                        handleAddListener(delegate, args);
                        return null;
                    }

                    // Invoke default methods on the NeoForge IEventBus interface directly.
                    if (method.isDefault()) {
                        return java.lang.reflect.InvocationHandler.invokeDefault(proxy, method, args);
                    }

                    // Delegate all other calls to the Forge event bus
                    try {
                        Method delegateMethod = findMatchingMethod(delegate.getClass(), method);
                        if (delegateMethod != null) {
                            delegateMethod.setAccessible(true);
                            return delegateMethod.invoke(delegate, args);
                        }
                    } catch (Throwable t) {
                        LOGGER.debug("[ReForged] Delegating {}() failed: {}", name, t.getMessage());
                    }

                    // Fallback for Object methods
                    if ("toString".equals(name)) return "NeoForgeEventBusAdapter[" + delegate + "]";
                    if ("hashCode".equals(name)) return delegate.hashCode();
                    if ("equals".equals(name)) return proxy == args[0];

                    return null;
                }
        );
    }

    /**
     * Handle register() calls by scanning for both Forge and NeoForge @SubscribeEvent.
     * Public so it can be called from {@code NeoForgeModLoader} for {@code @EventBusSubscriber} auto-registration.
     */
    public static void handleRegister(net.minecraftforge.eventbus.api.IEventBus delegate, Object target) {
        // Let Forge handle its own @SubscribeEvent annotations.
        // Catch Throwable — not just Exception — because Class.getMethods() can throw
        // NoClassDefFoundError if ANY method parameter references a missing NeoForge type.
        try {
            delegate.register(target);
        } catch (Throwable t) {
            LOGGER.debug("[ReForged] Forge register() skipped for {}: {}",
                    (target instanceof Class<?> c ? c.getSimpleName() : target.getClass().getSimpleName()),
                    t.getMessage());
        }

        // Scan for NeoForge's @SubscribeEvent annotations.
        // getDeclaredMethods() may also throw NoClassDefFoundError if the class has
        // methods whose parameter types reference missing shim classes.
        Class<?> clazz = target instanceof Class<?> c ? c : target.getClass();
        Method[] methods;
        try {
            methods = clazz.getDeclaredMethods();
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] Cannot scan methods of {} — unresolvable types: {}",
                    clazz.getSimpleName(), t.getMessage());
            return;
        }

        for (Method method : methods) {
            try {
                if (method.isAnnotationPresent(net.neoforged.bus.api.SubscribeEvent.class)) {
                    net.neoforged.bus.api.SubscribeEvent ann = method.getAnnotation(net.neoforged.bus.api.SubscribeEvent.class);
                    registerNeoForgeHandler(delegate, target, method, ann);
                }
            } catch (Throwable t) {
                LOGGER.warn("[ReForged] Skipping unresolvable method {}.{}: {}",
                        clazz.getSimpleName(), method.getName(), t.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void registerNeoForgeHandler(net.minecraftforge.eventbus.api.IEventBus delegate,
                                                 Object target, Method method,
                                                 net.neoforged.bus.api.SubscribeEvent ann) {
        try {
            Class<?>[] params = method.getParameterTypes();
            if (params.length != 1) return;

            EventPriority priority = ann.priority().toForge();
            boolean receiveCancelled = ann.receiveCanceled();
            method.setAccessible(true);

            // Case 1 (preferred): Check if the parameter type is a NeoForge wrapper that
            // has a constructor taking a Forge Event subclass. This maps the NeoForge event
            // to the correct Forge event type so the handler fires when Forge dispatches it.
            Class<?> neoType = params[0];
            Constructor<?> wrapperCtor = findWrapperConstructor(neoType);
            if (wrapperCtor != null) {
                Class<? extends Event> forgeEventType = (Class<? extends Event>) wrapperCtor.getParameterTypes()[0];
                wrapperCtor.setAccessible(true);
                Object invokeTarget = target instanceof Class<?> ? null : target;

                delegate.addListener(priority, receiveCancelled, forgeEventType, forgeEvent -> {
                    try {
                        Object neoEvent = wrapperCtor.newInstance(forgeEvent);
                        method.invoke(invokeTarget, neoEvent);
                    } catch (Throwable t) {
                        LOGGER.error("[ReForged] NeoForge wrapped handler error: {}.{}",
                                method.getDeclaringClass().getSimpleName(), method.getName(), t);
                    }
                });
                LOGGER.info("[ReForged] Registered wrapped @SubscribeEvent: {}.{}({}) → Forge: {}",
                        method.getDeclaringClass().getSimpleName(), method.getName(),
                        neoType.getSimpleName(), forgeEventType.getSimpleName());
                return;
            }

            // Case 2 (fallback): The parameter type IS a Forge Event subclass with no
            // wrapper constructor → register directly. This handles cases where
            // NeoForge shims extend Event directly (e.g. stub events).
            // We use the EXACT type as filter to avoid dispatching unrelated subtypes.
            if (Event.class.isAssignableFrom(neoType)) {
                Class<? extends Event> eventType = (Class<? extends Event>) neoType;
                Object invokeTarget = target instanceof Class<?> ? null : target;
                Class<?> finalNeoType = neoType;
                delegate.addListener(priority, receiveCancelled, eventType, event -> {
                    if (!finalNeoType.isInstance(event)) return;
                    try { method.invoke(invokeTarget, event); }
                    catch (Throwable t) { LOGGER.error("[ReForged] NeoForge handler error: {}", method.getName(), t); }
                });
                LOGGER.debug("[ReForged] Registered direct NeoForge @SubscribeEvent: {}.{}({})",
                        method.getDeclaringClass().getSimpleName(), method.getName(), neoType.getSimpleName());
                return;
            }

            LOGGER.warn("[ReForged] Could not register handler {}.{} — parameter type {} " +
                            "is neither a Forge Event nor a NeoForge wrapper",
                    method.getDeclaringClass().getSimpleName(), method.getName(), neoType.getName());
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] Skipping handler {}.{}: {}",
                    method.getName(), method.getDeclaringClass().getSimpleName(), t.getMessage());
        }
    }

    /**
     * Find a single-arg constructor on {@code neoType} whose parameter is a Forge {@link Event} subclass.
     * This establishes the NeoForge wrapper → Forge event mapping by convention.
     *
     * @return the wrapper constructor, or {@code null} if none found
     */
    private static Constructor<?> findWrapperConstructor(Class<?> neoType) {
        for (Constructor<?> ctor : neoType.getDeclaredConstructors()) {
            Class<?>[] ctorParams = ctor.getParameterTypes();
            if (ctorParams.length == 1 && Event.class.isAssignableFrom(ctorParams[0])) {
                return ctor;
            }
        }
        return null;
    }

    /**
     * Intercept all addListener() calls and bridge NeoForge event types to Forge.
     * Parses all NeoForge addListener overloads (8 variants), extracts the event type
     * from an explicit Class arg or via TypeResolver on the Consumer generic, then
     * registers the appropriate Forge listener with wrapper logic.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void handleAddListener(net.minecraftforge.eventbus.api.IEventBus delegate, Object[] args) {
        EventPriority priority = EventPriority.NORMAL;
        boolean receiveCancelled = false;
        Class<?> eventType = null;
        Consumer<?> consumer = null;

        // The Consumer is always the last argument in all addListener overloads
        if (args[args.length - 1] instanceof Consumer<?> c) {
            consumer = c;
        }
        if (consumer == null) {
            LOGGER.warn("[ReForged] addListener called with no Consumer argument");
            return;
        }

        // Parse remaining args (priority, receiveCancelled, eventType)
        for (int i = 0; i < args.length - 1; i++) {
            Object arg = args[i];
            if (arg instanceof net.neoforged.bus.api.EventPriority nep) {
                priority = nep.toForge();
            } else if (arg instanceof EventPriority fp) {
                priority = fp;
            } else if (arg instanceof Boolean b) {
                receiveCancelled = b;
            } else if (arg instanceof Class<?> c) {
                eventType = c;
            }
        }

        // If event type not explicitly provided, extract from Consumer's generic type
        if (eventType == null) {
            eventType = extractEventTypeFromConsumer(consumer);
        }
        if (eventType == null) {
            LOGGER.warn("[ReForged] Could not determine event type for addListener — Consumer: {}",
                    consumer.getClass().getName());
            return;
        }

        // Case 1: NeoForge wrapper event (has constructor taking a Forge Event subclass)
        Constructor<?> wrapperCtor = findWrapperConstructor(eventType);
        if (wrapperCtor != null) {
            Class<? extends Event> forgeEventType = (Class<? extends Event>) wrapperCtor.getParameterTypes()[0];
            wrapperCtor.setAccessible(true);
            Consumer<?> finalConsumer = consumer;
            Class<?> finalEventType = eventType;
            delegate.addListener(priority, receiveCancelled, forgeEventType, forgeEvent -> {
                try {
                    Object neoEvent = wrapperCtor.newInstance(forgeEvent);
                    ((Consumer<Object>) finalConsumer).accept(neoEvent);
                } catch (Throwable t) {
                    String message = t.getMessage() != null ? t.getMessage() : "";
                    boolean balmConfigNull = message.contains("config") && message.contains("null")
                            && stackContains(t, "net.blay09.mods.balm");
                    if (balmConfigNull) {
                        LOGGER.debug("[ReForged] Suppressed Balm config-null error in {} handler; config not ready yet",
                                finalEventType.getSimpleName());
                        return;
                    }
                    LOGGER.error("[ReForged] Wrapped addListener handler error for {}: {}",
                            finalEventType.getSimpleName(), t.getMessage(), t);
                }
            });
            LOGGER.info("[ReForged] Registered wrapped addListener: {} → {}",
                    eventType.getSimpleName(), forgeEventType.getSimpleName());
            return;
        }

        // Case 2: Direct Forge Event subclass — register directly on Forge bus
        // Use instanceof guard to only dispatch exact type matches
        if (Event.class.isAssignableFrom(eventType)) {
            Class<?> finalEventType2 = eventType;
            Consumer<?> finalConsumer2 = consumer;
            try {
                delegate.addListener(priority, receiveCancelled,
                    (Class) eventType, (Consumer<Event>) event -> {
                        if (!finalEventType2.isInstance(event)) return;
                        try {
                            ((Consumer<Object>) finalConsumer2).accept(event);
                        } catch (Throwable t2) {
                            String msg = t2.getMessage() != null ? t2.getMessage() : "";
                            if (msg.contains("config") && msg.contains("null")
                                    && stackContains(t2, "net.blay09.mods.balm")) {
                                LOGGER.debug("[ReForged] Suppressed Balm config-null error in direct {} handler",
                                        finalEventType2.getSimpleName());
                                return;
                            }
                            LOGGER.error("[ReForged] Direct addListener handler error for {}: {}",
                                    finalEventType2.getSimpleName(), t2.getMessage(), t2);
                        }
                    });
                LOGGER.info("[ReForged] Registered direct addListener for {}", eventType.getSimpleName());
            } catch (Throwable t) {
                LOGGER.warn("[ReForged] Failed to register direct addListener for {}: {}",
                        eventType.getName(), t.getMessage());
            }
            return;
        }

        LOGGER.warn("[ReForged] Cannot register addListener for {} — not a Forge Event or NeoForge wrapper",
                eventType.getName());
    }

    /**
     * Extract the event type T from a Consumer&lt;T&gt; using Forge's TypeResolver.
     * Uses reflection to access typetools (transitive dependency of Forge EventBus).
     */
    private static Class<?> extractEventTypeFromConsumer(Consumer<?> consumer) {
        try {
            Class<?> resolverClass = Class.forName("net.jodah.typetools.TypeResolver");
            java.lang.reflect.Method resolveMethod = resolverClass.getMethod(
                    "resolveRawArgument", Class.class, Class.class);
            Class<?> type = (Class<?>) resolveMethod.invoke(null, Consumer.class, consumer.getClass());
            // Check for TypeResolver.Unknown sentinel
            Class<?> unknownClass = Class.forName("net.jodah.typetools.TypeResolver$Unknown");
            if (type == unknownClass) {
                return null;
            }
            return type;
        } catch (Throwable t) {
            LOGGER.debug("[ReForged] Failed to resolve event type from Consumer: {}", t.getMessage());
            return null;
        }
    }

    /**
     * Find a method on the delegate class that matches the proxy method's name and parameter types.
     */
    private static Method findMatchingMethod(Class<?> clazz, Method proxyMethod) {
        try {
            return clazz.getMethod(proxyMethod.getName(), proxyMethod.getParameterTypes());
        } catch (NoSuchMethodException e) {
            // Try with broader search
            for (Method m : clazz.getMethods()) {
                if (m.getName().equals(proxyMethod.getName()) &&
                        m.getParameterCount() == proxyMethod.getParameterCount()) {
                    return m;
                }
            }
            return null;
        }
    }

    private static boolean stackContains(Throwable throwable, String token) {
        if (throwable == null || token == null || token.isBlank()) {
            return false;
        }
        Throwable current = throwable;
        while (current != null) {
            for (StackTraceElement element : current.getStackTrace()) {
                String className = element.getClassName();
                if (className != null && className.contains(token)) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }
}

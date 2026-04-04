package org.xiyu.reforged.bridge;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import org.xiyu.reforged.core.NeoForgeModLoader;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
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
     * Fallback listener store for events that cannot be registered on Forge's EventBus.
     * This handles Flywheel/NeoForge custom events that extend net.neoforged.bus.api.Event
     * (rewritten to net.minecraftforge.eventbus.api.Event) but lack proper Forge ListenerList setup.
     */
    private static final Map<Class<?>, List<Consumer<Object>>> FALLBACK_LISTENERS = new ConcurrentHashMap<>();

    /**
     * Dispatch an event to fallback listeners. Called by post() interception and ModLoader.postEvent().
     */
    @SuppressWarnings("unchecked")
    public static boolean dispatchFallback(Object event) {
        List<Consumer<Object>> listeners = FALLBACK_LISTENERS.get(event.getClass());
        if (listeners == null || listeners.isEmpty()) return false;
        for (Consumer<Object> listener : listeners) {
            try {
                listener.accept(event);
            } catch (Throwable t) {
                LOGGER.error("[ReForged] Fallback listener error for {}: {}",
                        event.getClass().getSimpleName(), t.getMessage(), t);
            }
        }
        return true;
    }

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

                    // Intercept post() — dispatch to fallback listeners for non-Forge events,
                    // then also delegate to Forge's bus for normal events.
                    if ("post".equals(name) && args != null && args.length == 1) {
                        Object event = args[0];
                        boolean dispatched = dispatchFallback(event);
                        // Also delegate to Forge bus if the event is a Forge Event
                        if (event instanceof Event forgeEvent) {
                            try {
                                delegate.post(forgeEvent);
                            } catch (Throwable t) {
                                if (!dispatched) {
                                    LOGGER.debug("[ReForged] Forge bus post() failed for {}: {}",
                                            event.getClass().getSimpleName(), t.getMessage());
                                }
                            }
                        }
                        // Return type depends on which post() overload was invoked:
                        // Forge's post(Event) returns boolean, NeoForge's post(Event) returns Event.
                        // The Dynamic Proxy will auto-unbox the return for primitive types,
                        // so we MUST return a Boolean when the resolved method returns boolean.
                        if (method.getReturnType() == boolean.class) {
                            return dispatched;
                        }
                        return event;
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
     *
     * <p>We intentionally do NOT delegate to {@code Forge EventBus.register()} because
     * Forge's ASM code generation ({@code ModLauncherFactory}) creates dynamic classes
     * that {@code NeoModClassLoader} cannot load, causing all handlers to fail.
     * Instead, we scan all annotated methods ourselves and register them via
     * reflection-based dispatch through {@code addListener()}.</p>
     *
     * <p>After bytecode rewriting, NeoForge's {@code @SubscribeEvent} annotation descriptor
     * is remapped to Forge's {@code @SubscribeEvent}. We therefore check for BOTH
     * annotation types to cover both rewritten and unrewritten classes.</p>
     */
    public static void handleRegister(net.minecraftforge.eventbus.api.IEventBus delegate, Object target) {
        Class<?> clazz = target instanceof Class<?> c ? c : target.getClass();
        Method[] methods;
        try {
            methods = clazz.getDeclaredMethods();
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] Cannot scan methods of {} — unresolvable types: {}",
                    clazz.getSimpleName(), t.getMessage());
            return;
        }

        int registered = 0;
        for (Method method : methods) {
            try {
                EventPriority priority = EventPriority.NORMAL;
                boolean receiveCancelled = false;
                boolean found = false;

                // Check NeoForge @SubscribeEvent (unrewritten bytecode)
                if (method.isAnnotationPresent(net.neoforged.bus.api.SubscribeEvent.class)) {
                    net.neoforged.bus.api.SubscribeEvent ann =
                            method.getAnnotation(net.neoforged.bus.api.SubscribeEvent.class);
                    priority = ann.priority().toForge();
                    receiveCancelled = ann.receiveCanceled();
                    found = true;
                }
                // Check Forge @SubscribeEvent (rewritten bytecode)
                else if (method.isAnnotationPresent(net.minecraftforge.eventbus.api.SubscribeEvent.class)) {
                    net.minecraftforge.eventbus.api.SubscribeEvent ann =
                            method.getAnnotation(net.minecraftforge.eventbus.api.SubscribeEvent.class);
                    priority = ann.priority();
                    receiveCancelled = ann.receiveCanceled();
                    found = true;
                }

                if (!found) continue;

                if (registerEventHandler(delegate, target, method, priority, receiveCancelled)) {
                    registered++;
                }
            } catch (Throwable t) {
                LOGGER.warn("[ReForged] Skipping unresolvable method {}.{}: {}",
                        clazz.getSimpleName(), method.getName(), t.getMessage());
            }
        }

        if (registered > 0) {
            LOGGER.info("[ReForged] Registered {} event handler(s) for {}",
                    registered, clazz.getSimpleName());
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean registerEventHandler(net.minecraftforge.eventbus.api.IEventBus delegate,
                                                Object target, Method method,
                                                EventPriority priority, boolean receiveCancelled) {
        try {
            Class<?>[] params = method.getParameterTypes();
            if (params.length != 1) return false;

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

                final String handlerDesc = method.getDeclaringClass().getSimpleName() + "." + method.getName();
                final String forgeEventName = forgeEventType.getSimpleName();
                Consumer<Event> bridgeHandler = forgeEvent -> {
                    if (!forgeEventType.isInstance(forgeEvent)) return;
                    try {
                        Object neoEvent = wrapperCtor.newInstance(forgeEvent);
                        method.invoke(invokeTarget, neoEvent);
                    } catch (Throwable t) {
                        LOGGER.error("[ReForged] NeoForge wrapped handler error: {}.{}",
                                method.getDeclaringClass().getSimpleName(), method.getName(), t);
                        if (t instanceof java.lang.reflect.InvocationTargetException && t.getCause() != null) {
                            LOGGER.error("[ReForged] Wrapped handler root cause:", t.getCause());
                        }
                    }
                };

                // Route mod bus events to the Forge MOD bus
                boolean isModBusEvent = net.minecraftforge.fml.event.IModBusEvent.class.isAssignableFrom(forgeEventType);
                IEventBus targetBus = delegate;
                if (isModBusEvent) {
                    IEventBus modBus = NeoForgeModLoader.getForgeModBus();
                    if (modBus != null) targetBus = modBus;
                }
                targetBus.addListener(priority, receiveCancelled, forgeEventType, (Consumer) bridgeHandler);
                LOGGER.info("[ReForged] Registered wrapped @SubscribeEvent: {}.{}({}) \u2192 Forge: {}{}",
                        method.getDeclaringClass().getSimpleName(), method.getName(),
                        neoType.getSimpleName(), forgeEventType.getSimpleName(),
                        isModBusEvent ? " (MOD bus)" : "");
                return true;
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
                return true;
            }

            LOGGER.warn("[ReForged] Could not register handler {}.{} — parameter type {} " +
                            "is neither a Forge Event nor a NeoForge wrapper",
                    method.getDeclaringClass().getSimpleName(), method.getName(), neoType.getName());
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] Skipping handler {}.{}: {}",
                    method.getName(), method.getDeclaringClass().getSimpleName(), t.getMessage());
        }
        return false;
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
        LOGGER.info("[ReForged] addListener: eventType={} (pkg={}), isForgeEvent={}, classLoader={}",
                eventType.getName(), eventType.getPackageName(),
                Event.class.isAssignableFrom(eventType),
                eventType.getClassLoader());
        Constructor<?> wrapperCtor = findWrapperConstructor(eventType);

        // Case 1b: DISABLED — When TypeResolver returns a Forge type, it means the
        // consumer's bytecode was remapped from NeoForge → Forge types. The consumer
        // expects the Forge type. Wrapping in a NeoForge wrapper causes ClassCastException
        // because the wrapper does not extend the Forge event class.
        // Fall through to Case 2 (direct registration) instead.

        LOGGER.info("[ReForged] addListener: wrapperCtor={} for {}", wrapperCtor, eventType.getSimpleName());
        if (wrapperCtor != null) {
            Constructor<?> finalWrapperCtor = wrapperCtor;
            Class<? extends Event> forgeEventType = (Class<? extends Event>) finalWrapperCtor.getParameterTypes()[0];
            finalWrapperCtor.setAccessible(true);
            Consumer<?> finalConsumer = consumer;
            Class<?> finalEventType = eventType;

            Consumer<Event> bridgeListener = forgeEvent -> {
                // Guard: Forge EventBus may dispatch events of unexpected types if
                // ListenerList parent chains are shared. Skip if the event is not
                // the expected Forge type (e.g. a NeoForge wrapper posted separately).
                if (!forgeEventType.isInstance(forgeEvent)) return;
                try {
                    Object neoEvent = finalWrapperCtor.newInstance(forgeEvent);
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
            };

            // Determine which Forge bus to register on:
            // IModBusEvent events are fired on the Forge MOD bus, not the GAME bus.
            boolean isModBusEvent = net.minecraftforge.fml.event.IModBusEvent.class.isAssignableFrom(forgeEventType);
            IEventBus targetBus = delegate;
            if (isModBusEvent) {
                IEventBus modBus = NeoForgeModLoader.getForgeModBus();
                if (modBus != null) {
                    targetBus = modBus;
                    LOGGER.info("[ReForged] Routing addListener for {} to MOD bus (was on {})",
                            eventType.getSimpleName(), delegate == modBus ? "mod bus" : "game bus");
                }
            }
            targetBus.addListener(priority, receiveCancelled, forgeEventType, (Consumer) bridgeListener);
            LOGGER.info("[ReForged] Registered wrapped addListener: {} → {}{}",
                    eventType.getSimpleName(), forgeEventType.getSimpleName(),
                    isModBusEvent ? " (MOD bus)" : "");
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
                // Forge's EventBus can't compute listener lists for non-native events
                // (e.g. Flywheel's custom events). Store in fallback map instead.
                LOGGER.info("[ReForged] Forge bus registration failed for {} — using fallback listener: {}",
                        eventType.getName(), t.getMessage());
                FALLBACK_LISTENERS.computeIfAbsent(finalEventType2, k -> new CopyOnWriteArrayList<>())
                        .add(event -> {
                            if (!finalEventType2.isInstance(event)) return;
                            try {
                                ((Consumer<Object>) finalConsumer2).accept(event);
                            } catch (Throwable t2) {
                                LOGGER.error("[ReForged] Fallback handler error for {}: {}",
                                        finalEventType2.getSimpleName(), t2.getMessage(), t2);
                            }
                        });
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

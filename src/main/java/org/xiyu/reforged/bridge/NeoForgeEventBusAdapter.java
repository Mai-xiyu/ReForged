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

                    // Delegate all other calls to the Forge event bus
                    try {
                        Method delegateMethod = findMatchingMethod(delegate.getClass(), method);
                        if (delegateMethod != null) {
                            delegateMethod.setAccessible(true);
                            return delegateMethod.invoke(delegate, args);
                        }
                    } catch (Exception e) {
                        LOGGER.debug("[ReForged] Delegating {}() failed: {}", name, e.getMessage());
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

            // Case 1: The parameter type IS a Forge Event subclass → direct registration
            if (Event.class.isAssignableFrom(params[0])) {
                Class<? extends Event> eventType = (Class<? extends Event>) params[0];
                Object invokeTarget = target instanceof Class<?> ? null : target;
                delegate.addListener(priority, receiveCancelled, eventType, event -> {
                    try { method.invoke(invokeTarget, event); }
                    catch (Exception e) { LOGGER.error("[ReForged] NeoForge handler error: {}", method.getName(), e); }
                });
                LOGGER.debug("[ReForged] Registered NeoForge @SubscribeEvent: {}.{}({})",
                        method.getDeclaringClass().getSimpleName(), method.getName(), params[0].getSimpleName());
                return;
            }

            // Case 2: The parameter type is a NeoForge wrapper (NOT a Forge Event subclass).
            // Look for a constructor taking a single Forge Event subclass → register for
            // that Forge type and wrap the event when it fires.
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
                    } catch (Exception e) {
                        LOGGER.error("[ReForged] NeoForge wrapped handler error: {}.{}",
                                method.getDeclaringClass().getSimpleName(), method.getName(), e);
                    }
                });
                LOGGER.info("[ReForged] Registered wrapped @SubscribeEvent: {}.{}({}) → Forge: {}",
                        method.getDeclaringClass().getSimpleName(), method.getName(),
                        neoType.getSimpleName(), forgeEventType.getSimpleName());
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
    @SuppressWarnings("unchecked")
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
}

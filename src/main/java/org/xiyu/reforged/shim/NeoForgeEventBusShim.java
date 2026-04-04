package org.xiyu.reforged.shim;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.xiyu.reforged.core.NeoForgeModLoader;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * NeoForgeEventBusShim — Full-featured NeoForge GAME event bus replacement.
 *
 * <p>NeoForge mods reference {@code NeoForge.EVENT_BUS} which gets mapped to
 * {@code NeoForgeShim.EVENT_BUS}, an instance of this class.</p>
 *
 * <h3>Wrapper Bridge Pattern</h3>
 * <p>NeoForge event stubs (in {@code net.neoforged.*}) are <em>different classes</em>
 * from Forge's equivalents (in {@code net.minecraftforge.*}). When a NeoForge mod
 * registers a handler for e.g. {@code net.neoforged...ScreenEvent.Render.Post},
 * Forge fires its own {@code net.minecraftforge...ScreenEvent.Render.Post} —
 * different class hierarchy, so {@code isAssignableFrom} fails.</p>
 *
 * <p>To bridge this gap, NeoForge event stubs can declare a <b>wrapper constructor</b>
 * that accepts the corresponding Forge event as its single parameter. When this shim
 * detects such a constructor during handler registration, it registers a bridge
 * listener on Forge's {@code MinecraftForge.EVENT_BUS} that:</p>
 * <ol>
 *   <li>Receives the Forge event</li>
 *   <li>Creates the NeoForge wrapper via the wrapper constructor</li>
 *   <li>Dispatches to the original NeoForge handler</li>
 * </ol>
 *
 * <p>This is the same pattern that {@link org.xiyu.reforged.bridge.NeoForgeEventBusAdapter}
 * implements for the MOD event bus.</p>
 */
public final class NeoForgeEventBusShim {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Event handler entry — wraps a handler method or lambda with its metadata.
     */
    private record HandlerEntry(
            Object owner,
            Consumer<Event> handler,
            Class<? extends Event> eventType,
            EventPriority priority,
            boolean receiveCancelled
    ) {}

    /**
     * Per-event-type handler list, kept sorted by priority.
     */
    private final ConcurrentHashMap<Class<? extends Event>, CopyOnWriteArrayList<HandlerEntry>> handlers =
            new ConcurrentHashMap<>();

    /**
     * Track registered owners for unregister support.
     */
    private final Set<Object> registeredOwners = ConcurrentHashMap.newKeySet();

    /**
     * Cache: NeoForge event type → wrapper constructor (null = no wrapper, checked).
     * Avoids repeated reflection lookups.
     */
    private static final ConcurrentHashMap<Class<?>, Optional<Constructor<?>>> wrapperCtorCache =
            new ConcurrentHashMap<>();

    /**
     * Guard against re-entrant dispatch when a Forge bridge listener fires
     * and the handler re-posts to this bus.
     */
    private static final ThreadLocal<Boolean> BRIDGE_DISPATCHING = ThreadLocal.withInitial(() -> false);

    // ─── Registration methods ─────────────────────────────────────

    /**
     * Register an object instance — scans for methods annotated with
     * {@code @SubscribeEvent} (instance methods).
     */
    public void register(Object target) {
        if (target instanceof Class<?> clazz) {
            registerClass(clazz);
            return;
        }
        if (registeredOwners.contains(target)) return;
        registeredOwners.add(target);

        Class<?> clazz = target.getClass();
        Method[] methods;
        try {
            methods = clazz.getMethods();
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] Cannot scan methods of {} — unresolvable types: {}",
                    clazz.getSimpleName(), t.getMessage());
            return;
        }

        for (Method method : methods) {
            try {
                if (method.isAnnotationPresent(SubscribeEvent.class) && method.getParameterCount() == 1) {
                    Class<?> paramType = method.getParameterTypes()[0];
                    if (Event.class.isAssignableFrom(paramType)) {
                        @SuppressWarnings("unchecked")
                        Class<? extends Event> eventType = (Class<? extends Event>) paramType;
                        SubscribeEvent ann = method.getAnnotation(SubscribeEvent.class);
                        EventPriority priority = ann.priority();
                        boolean receiveCancelled = ann.receiveCanceled();

                        method.setAccessible(true);
                        final Object instance = target;
                        Consumer<Event> handler = event -> {
                            try {
                                method.invoke(instance, event);
                            } catch (Exception e) {
                                LOGGER.error("[ReForged] Error invoking event handler {}.{}",
                                        clazz.getSimpleName(), method.getName(), e);
                            }
                        };

                        registerWithBridging(new HandlerEntry(target, handler, eventType, priority, receiveCancelled));
                        LOGGER.debug("[ReForged] Registered handler: {}.{}({})",
                                clazz.getSimpleName(), method.getName(), eventType.getSimpleName());
                    }
                }
            } catch (Throwable t) {
                LOGGER.warn("[ReForged] Skipping unresolvable method {}.{}: {}",
                        clazz.getSimpleName(), method.getName(), t.getMessage());
            }
        }
    }

    /**
     * Register a class — scans for static methods annotated with {@code @SubscribeEvent}.
     */
    public void registerClass(Class<?> clazz) {
        if (registeredOwners.contains(clazz)) return;
        registeredOwners.add(clazz);

        Method[] methods;
        try {
            methods = clazz.getMethods();
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] Cannot scan methods of {} — unresolvable types: {}",
                    clazz.getSimpleName(), t.getMessage());
            return;
        }

        for (Method method : methods) {
            try {
                if (method.isAnnotationPresent(SubscribeEvent.class)
                        && method.getParameterCount() == 1
                        && java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                    Class<?> paramType = method.getParameterTypes()[0];
                    if (Event.class.isAssignableFrom(paramType)) {
                        @SuppressWarnings("unchecked")
                        Class<? extends Event> eventType = (Class<? extends Event>) paramType;
                        SubscribeEvent ann = method.getAnnotation(SubscribeEvent.class);
                        EventPriority priority = ann.priority();
                        boolean receiveCancelled = ann.receiveCanceled();

                        method.setAccessible(true);
                        Consumer<Event> handler = event -> {
                            try {
                                method.invoke(null, event);
                            } catch (Exception e) {
                                LOGGER.error("[ReForged] Error invoking static handler {}.{}",
                                        clazz.getSimpleName(), method.getName(), e);
                            }
                        };

                        registerWithBridging(new HandlerEntry(clazz, handler, eventType, priority, receiveCancelled));
                        LOGGER.debug("[ReForged] Registered static handler: {}.{}({})",
                                clazz.getSimpleName(), method.getName(), eventType.getSimpleName());
                    }
                }
            } catch (Throwable t) {
                LOGGER.warn("[ReForged] Skipping unresolvable method {}.{}: {}",
                        clazz.getSimpleName(), method.getName(), t.getMessage());
            }
        }
    }

    /**
     * Lambda-style listener — NeoForge's preferred registration pattern.
     * {@code bus.addListener(this::onEvent)}
     */
    @SuppressWarnings("unchecked")
    public <T extends Event> void addListener(Consumer<T> handler) {
        addListener(EventPriority.NORMAL, false, handler);
    }

    /**
     * Lambda with priority.
     */
    @SuppressWarnings("unchecked")
    public <T extends Event> void addListener(EventPriority priority, Consumer<T> handler) {
        addListener(priority, false, handler);
    }

    /**
     * Lambda with priority and cancellation flag.
     */
    @SuppressWarnings("unchecked")
    public <T extends Event> void addListener(EventPriority priority, boolean receiveCancelled, Consumer<T> handler) {
        Class<? extends Event> eventType = inferEventType(handler);
        if (eventType == null) {
            eventType = Event.class;
            LOGGER.warn("[ReForged] Could not infer event type for lambda handler, using Event.class");
        }
        registerWithBridging(new HandlerEntry(handler, (Consumer<Event>) (Consumer<?>) handler,
                eventType, priority, receiveCancelled));
    }

    /**
     * Typed lambda listener with explicit event class.
     * {@code bus.addListener(EventPriority.NORMAL, false, ServerStartingEvent.class, this::onStart)}
     */
    @SuppressWarnings("unchecked")
    public <T extends Event> void addListener(EventPriority priority, boolean receiveCancelled,
                                               Class<T> eventType, Consumer<T> handler) {
        registerWithBridging(new HandlerEntry(handler, (Consumer<Event>) (Consumer<?>) handler,
                eventType, priority, receiveCancelled));
        LOGGER.debug("[ReForged] Registered typed lambda handler for {}", eventType.getSimpleName());
    }

    // ─── Unregistration ───────────────────────────────────────────

    /**
     * Unregister all handlers associated with the given owner.
     */
    public void unregister(Object owner) {
        registeredOwners.remove(owner);
        handlers.values().forEach(list -> list.removeIf(entry -> entry.owner() == owner));
    }

    // ─── Event Posting ────────────────────────────────────────────

    /**
     * Post an event to all registered handlers.
     *
     * <p>Dispatches to handlers whose registered event type is assignable from
     * the posted event's class. Respects priority ordering and cancellation.</p>
     *
     * @param event the event to post
     * @return {@code true} if the event was cancelled by a handler
     */
    public boolean post(Event event) {
        Class<? extends Event> eventClass = event.getClass();

        // Collect all matching handlers (for this event type and all supertypes)
        List<HandlerEntry> matching = new ArrayList<>();
        for (var entry : handlers.entrySet()) {
            if (entry.getKey().isAssignableFrom(eventClass)) {
                matching.addAll(entry.getValue());
            }
        }

        // Sort by priority (highest first)
        matching.sort(Comparator.comparingInt(h -> h.priority().ordinal()));

        // Dispatch
        for (HandlerEntry handler : matching) {
            if (event.isCancelable() && event.isCanceled() && !handler.receiveCancelled()) {
                continue;
            }
            try {
                handler.handler().accept(event);
            } catch (Exception e) {
                LOGGER.error("[ReForged] Exception in event handler for {}", eventClass.getSimpleName(), e);
            }
        }

        return event.isCancelable() && event.isCanceled();
    }

    /**
     * Untyped post entrypoint for synthetic mixin call sites where the JVM verifier
     * cannot prove assignability to Forge Event at compile-time.
     */
    public boolean postUntyped(Object event) {
        if (!(event instanceof Event forgeEvent)) {
            return false;
        }
        return post(forgeEvent);
    }

    // ─── Wrapper Bridge Logic ─────────────────────────────────────

    /**
     * Register a handler entry with automatic Forge→NeoForge bridge detection.
     *
     * <p>If the NeoForge event type has a wrapper constructor (single-arg taking
     * a Forge Event subclass), we register a bridge listener on Forge's bus that
     * wraps the Forge event and dispatches to the NeoForge handler.</p>
     *
     * <p>If the event type IS a Forge Event (no wrapper needed), we also register
     * on Forge's bus directly so the handler fires when Forge dispatches the event.</p>
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void registerWithBridging(HandlerEntry entry) {
        // Always register in our local handler map for direct post() dispatch
        addHandlerEntry(entry);

        Class<? extends Event> neoEventType = entry.eventType();

        // Skip base Event class — no bridging needed
        if (neoEventType == Event.class) return;

        // Check if this NeoForge event type has a wrapper constructor
        Constructor<?> wrapperCtor = findWrapperConstructor(neoEventType);
        if (wrapperCtor != null) {
            // NeoForge wrapper event — bridge from Forge bus
            Class<? extends Event> forgeEventType = (Class<? extends Event>) wrapperCtor.getParameterTypes()[0];
            wrapperCtor.setAccessible(true);
            final Constructor<?> ctor = wrapperCtor;
            final Consumer<Event> neoHandler = entry.handler();

            Consumer<Event> bridgeListener = forgeEvent -> {
                if (BRIDGE_DISPATCHING.get()) return;
                try {
                    BRIDGE_DISPATCHING.set(true);
                    Event neoEvent = (Event) ctor.newInstance(forgeEvent);
                    neoHandler.accept(neoEvent);
                } catch (Throwable t) {
                    LOGGER.debug("[ReForged] Bridge handler error for {} → {}: {}",
                            forgeEventType.getSimpleName(), neoEventType.getSimpleName(),
                            t.getCause() != null ? t.getCause().getMessage() : t.getMessage());
                } finally {
                    BRIDGE_DISPATCHING.set(false);
                }
            };

            // Determine which Forge bus to register on:
            // IModBusEvent events are fired on the Forge MOD bus, not the GAME bus.
            boolean isModBusEvent = net.minecraftforge.fml.event.IModBusEvent.class.isAssignableFrom(forgeEventType);
            if (isModBusEvent) {
                IEventBus modBus = NeoForgeModLoader.getForgeModBus();
                if (modBus != null) {
                    modBus.addListener(entry.priority(), entry.receiveCancelled(),
                            forgeEventType, (Consumer) bridgeListener);
                    LOGGER.info("[ReForged] Bridged GAME→MOD bus handler: {} → Forge {} (registered on MOD bus)",
                            neoEventType.getSimpleName(), forgeEventType.getSimpleName());
                } else {
                    LOGGER.error("[ReForged] Cannot bridge mod bus event {} — Forge mod bus not yet available!",
                            forgeEventType.getSimpleName());
                }
            } else {
                MinecraftForge.EVENT_BUS.addListener(entry.priority(), entry.receiveCancelled(),
                        forgeEventType, (Consumer) bridgeListener);
                LOGGER.info("[ReForged] Bridged GAME bus handler: {} → Forge {}",
                        neoEventType.getSimpleName(), forgeEventType.getSimpleName());
            }
            return;
        }

        // Check if the NeoForge event type is a proper subclass of Forge Event
        // that Forge itself fires (e.g. events created by our Mixins like RenderGuiEvent).
        // These are already dispatched correctly via EventBridge forwarding, so no
        // additional bridge is needed. The local handler map + EventBridge handles it.
        String className = neoEventType.getName();
        if (className.startsWith("net.neoforged.")) {
            // NeoForge-specific event without wrapper constructor.
            // These are fired by our Mixins directly as the stub class.
            // EventBridge forwards them from Forge bus to this shim bus.
            // No additional bridge needed.
            LOGGER.debug("[ReForged] NeoForge-only event registered: {} (handled via direct post/EventBridge)",
                    neoEventType.getSimpleName());
        }
    }

    private void addHandlerEntry(HandlerEntry entry) {
        handlers.computeIfAbsent(entry.eventType(), k -> new CopyOnWriteArrayList<>()).add(entry);
    }

    // ─── Wrapper Constructor Detection ────────────────────────────

    /**
     * Find a single-arg constructor on {@code eventType} whose parameter is a
     * Forge {@link Event} subclass that is NOT in the {@code net.neoforged}
     * package (i.e., it wraps a real Forge event).
     *
     * @return the wrapper constructor, or {@code null} if none found
     */
    static Constructor<?> findWrapperConstructor(Class<?> eventType) {
        return wrapperCtorCache.computeIfAbsent(eventType, type -> {
            for (Constructor<?> ctor : type.getDeclaredConstructors()) {
                Class<?>[] params = ctor.getParameterTypes();
                if (params.length == 1
                        && Event.class.isAssignableFrom(params[0])
                        && !params[0].getName().startsWith("net.neoforged.")) {
                    return Optional.of(ctor);
                }
            }
            return Optional.empty();
        }).orElse(null);
    }

    // ─── Event Type Inference ─────────────────────────────────────

    /**
     * Attempt to infer the event type from a lambda Consumer.
     * Tries reflection on the lambda class, then falls back to TypeResolver.
     */
    @SuppressWarnings("unchecked")
    private <T extends Event> Class<? extends Event> inferEventType(Consumer<T> handler) {
        // Strategy 1: Check for a non-Object accept() parameter type
        try {
            for (Method m : handler.getClass().getMethods()) {
                if ("accept".equals(m.getName()) && m.getParameterCount() == 1
                        && !m.getParameterTypes()[0].equals(Object.class)) {
                    Class<?> paramType = m.getParameterTypes()[0];
                    if (Event.class.isAssignableFrom(paramType)) {
                        return (Class<? extends Event>) paramType;
                    }
                }
            }
        } catch (Throwable ignored) {}

        // Strategy 2: Check generic interfaces
        try {
            for (java.lang.reflect.Type iface : handler.getClass().getGenericInterfaces()) {
                if (iface instanceof java.lang.reflect.ParameterizedType pt) {
                    for (java.lang.reflect.Type arg : pt.getActualTypeArguments()) {
                        if (arg instanceof Class<?> c && Event.class.isAssignableFrom(c)) {
                            return (Class<? extends Event>) c;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}

        // Strategy 3: Use Forge's TypeResolver if available
        try {
            Class<?> resolverClass = Class.forName("net.jodah.typetools.TypeResolver");
            Method resolveMethod = resolverClass.getMethod("resolveRawArgument", Class.class, Class.class);
            Class<?> type = (Class<?>) resolveMethod.invoke(null, Consumer.class, handler.getClass());
            Class<?> unknownClass = Class.forName("net.jodah.typetools.TypeResolver$Unknown");
            if (type != unknownClass && Event.class.isAssignableFrom(type)) {
                return (Class<? extends Event>) type;
            }
        } catch (Throwable ignored) {}

        return null;
    }
}

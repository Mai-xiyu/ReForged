package org.xiyu.reforged.shim;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * NeoForgeEventBusShim — Full-featured fake NeoForge event bus.
 *
 * <p>After bytecode rewriting, NeoForge mods call methods on this bus via
 * {@code NeoForgeShim.EVENT_BUS}. The bus must support:</p>
 * <ul>
 *     <li>{@link #register(Object)} — register an instance with {@code @SubscribeEvent} methods</li>
 *     <li>{@link #register(Class)} — register a class with static {@code @SubscribeEvent} methods</li>
 *     <li>{@link #addListener(Consumer)} — lambda-style listener registration</li>
 *     <li>{@link #addListener(EventPriority, Consumer)} — lambda with priority</li>
 *     <li>{@link #addListener(EventPriority, boolean, Consumer)} — with cancellation handling</li>
 *     <li>{@link #addListener(EventPriority, boolean, Class, Consumer)} — typed listener</li>
 *     <li>{@link #post(Event)} — fire an event to all subscribers</li>
 *     <li>{@link #unregister(Object)} — remove listener</li>
 * </ul>
 *
 * <p>This is the heart of NeoForge mod compatibility — every event handler registration
 * from NeoForge mods flows through this bus.</p>
 */
public final class NeoForgeEventBusShim {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Event handler entry — wraps a handler method or lambda with its metadata.
     */
    private record HandlerEntry(
            Object owner,                    // instance or Class for static
            Consumer<Event> handler,         // the actual handler invocation
            Class<? extends Event> eventType,// which event type this handles
            EventPriority priority,          // priority level
            boolean receiveCancelled         // should it receive cancelled events?
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
        for (Method method : clazz.getMethods()) {
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
                            LOGGER.error("[ReForged] Error invoking event handler {}.{}", clazz.getSimpleName(), method.getName(), e);
                        }
                    };

                    addHandlerEntry(new HandlerEntry(target, handler, eventType, priority, receiveCancelled));
                    LOGGER.debug("[ReForged] Registered handler: {}.{}({})",
                            clazz.getSimpleName(), method.getName(), eventType.getSimpleName());
                }
            }
        }
    }

    /**
     * Register a class — scans for static methods annotated with {@code @SubscribeEvent}.
     */
    public void registerClass(Class<?> clazz) {
        if (registeredOwners.contains(clazz)) return;
        registeredOwners.add(clazz);

        for (Method method : clazz.getMethods()) {
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
                            LOGGER.error("[ReForged] Error invoking static handler {}.{}", clazz.getSimpleName(), method.getName(), e);
                        }
                    };

                    addHandlerEntry(new HandlerEntry(clazz, handler, eventType, priority, receiveCancelled));
                    LOGGER.debug("[ReForged] Registered static handler: {}.{}({})",
                            clazz.getSimpleName(), method.getName(), eventType.getSimpleName());
                }
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
        // Try to infer the event type from the lambda
        Class<? extends Event> eventType = inferEventType(handler);
        if (eventType == null) {
            eventType = Event.class; // fallback
            LOGGER.warn("[ReForged] Could not infer event type for lambda handler, using Event.class");
        }
        addHandlerEntry(new HandlerEntry(handler, (Consumer<Event>) (Consumer<?>) handler, eventType, priority, receiveCancelled));
    }

    /**
     * Typed lambda listener with explicit event class.
     * {@code bus.addListener(EventPriority.NORMAL, false, ServerStartingEvent.class, this::onStart)}
     */
    @SuppressWarnings("unchecked")
    public <T extends Event> void addListener(EventPriority priority, boolean receiveCancelled,
                                               Class<T> eventType, Consumer<T> handler) {
        addHandlerEntry(new HandlerEntry(handler, (Consumer<Event>) (Consumer<?>) handler, eventType, priority, receiveCancelled));
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

    // ─── Internal helpers ─────────────────────────────────────────

    private void addHandlerEntry(HandlerEntry entry) {
        handlers.computeIfAbsent(entry.eventType(), k -> new CopyOnWriteArrayList<>()).add(entry);
    }

    /**
     * Attempt to infer the event type from a lambda Consumer.
     * Uses MethodHandles to look at the lambda's target method signature.
     */
    @SuppressWarnings("unchecked")
    private <T extends Event> Class<? extends Event> inferEventType(Consumer<T> handler) {
        try {
            // Try to find the accept method's parameter type via reflection on the lambda
            for (Method m : handler.getClass().getMethods()) {
                if ("accept".equals(m.getName()) && m.getParameterCount() == 1
                        && !m.getParameterTypes()[0].equals(Object.class)) {
                    Class<?> paramType = m.getParameterTypes()[0];
                    if (Event.class.isAssignableFrom(paramType)) {
                        return (Class<? extends Event>) paramType;
                    }
                }
            }
            // Fallback: check generic interfaces
            for (java.lang.reflect.Type iface : handler.getClass().getGenericInterfaces()) {
                if (iface instanceof java.lang.reflect.ParameterizedType pt) {
                    for (java.lang.reflect.Type arg : pt.getActualTypeArguments()) {
                        if (arg instanceof Class<?> c && Event.class.isAssignableFrom(c)) {
                            return (Class<? extends Event>) c;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Inference failed — caller will use Event.class fallback
        }
        return null;
    }
}

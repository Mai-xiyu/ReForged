package net.neoforged.bus.api;

/**
 * Proxy for NeoForge's {@code ICancellableEvent} marker interface.
 * In NeoForge, events implement this to be cancellable.
 * In Forge, cancellability is controlled by {@code @Cancelable} annotation on the Event class.
 */
public interface ICancellableEvent {
    // Marker interface — no methods required.
    // Forge uses Event.isCancelable() / @Cancelable instead.
}

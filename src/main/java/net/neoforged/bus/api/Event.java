package net.neoforged.bus.api;

/**
 * Stub: NeoForge Event base class — delegates to Forge's event system.
 *
 * <p>Overrides {@link #isCancelable()} so that NeoForge events implementing
 * {@link ICancellableEvent} are treated as cancelable by the Forge event bus,
 * which normally requires the {@code @Cancelable} annotation.</p>
 */
public class Event extends net.minecraftforge.eventbus.api.Event {

    @Override
    public boolean isCancelable() {
        return this instanceof ICancellableEvent || super.isCancelable();
    }
}

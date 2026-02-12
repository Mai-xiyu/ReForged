package net.neoforged.neoforge.client.event;

import net.minecraft.server.packs.resources.PreparableReloadListener;

/**
 * NeoForge shim wrapping Forge's {@code RegisterClientReloadListenersEvent}.
 *
 * <p>When Forge fires its event on the mod bus, the event bus adapter creates an
 * instance of this class wrapping the Forge event, then dispatches it to NeoForge
 * mod handlers that registered for this type.</p>
 */
public class RegisterClientReloadListenersEvent {

    private final net.minecraftforge.client.event.RegisterClientReloadListenersEvent delegate;

    public RegisterClientReloadListenersEvent(
            net.minecraftforge.client.event.RegisterClientReloadListenersEvent delegate) {
        this.delegate = delegate;
    }

    /**
     * Registers the given reload listener to the client-side resource manager.
     */
    public void registerReloadListener(PreparableReloadListener reloadListener) {
        delegate.registerReloadListener(reloadListener);
    }
}

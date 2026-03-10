package net.neoforged.neoforge.event.server;

import net.minecraft.server.MinecraftServer;

/**
 * Stub: Fired after the server is fully started.
 */
public class ServerStartedEvent extends ServerLifecycleEvent {
    public ServerStartedEvent(MinecraftServer server) {
        super(server);
    }

    /** Forge wrapper constructor for automatic event bridging */
    public ServerStartedEvent(net.minecraftforge.event.server.ServerStartedEvent delegate) {
        this(delegate.getServer());
    }
}

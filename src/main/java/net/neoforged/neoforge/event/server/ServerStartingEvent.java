package net.neoforged.neoforge.event.server;

import net.minecraft.server.MinecraftServer;

/**
 * Stub: Fired during server starting phase.
 */
public class ServerStartingEvent extends ServerLifecycleEvent {
    public ServerStartingEvent(MinecraftServer server) {
        super(server);
    }

    /** Forge wrapper constructor for automatic event bridging */
    public ServerStartingEvent(net.minecraftforge.event.server.ServerStartingEvent delegate) {
        this(delegate.getServer());
    }
}

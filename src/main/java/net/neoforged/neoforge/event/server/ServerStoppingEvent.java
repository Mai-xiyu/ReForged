package net.neoforged.neoforge.event.server;

import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.Event;

/**
 * Fired when the server is stopping.
 */
public class ServerStoppingEvent extends Event {
    private final MinecraftServer server;

    public ServerStoppingEvent(MinecraftServer server) {
        this.server = server;
    }

    public MinecraftServer getServer() { return server; }
}

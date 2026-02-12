package org.xiyu.reforged.bridge.events;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.Event;

/**
 * NeoServerStoppingEvent — NeoForge-compatible wrapper for Forge's
 * {@link net.minecraftforge.event.server.ServerStoppingEvent}.
 */
public class NeoServerStoppingEvent extends Event {

    private final MinecraftServer server;

    public NeoServerStoppingEvent(MinecraftServer server) {
        this.server = server;
    }

    /**
     * @return the server instance that is stopping
     */
    public MinecraftServer getServer() {
        return server;
    }
}

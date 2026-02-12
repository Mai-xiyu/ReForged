package org.xiyu.reforged.bridge.events;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.Event;

/**
 * NeoServerStartingEvent — NeoForge-compatible wrapper for Forge's
 * {@link net.minecraftforge.event.server.ServerStartingEvent}.
 *
 * <p>NeoForge mods subscribe to their own {@code ServerStartingEvent} type.
 * After bytecode rewriting those references point to Forge's event class, but
 * our {@link org.xiyu.reforged.bridge.EventBridge} fires this wrapper through
 * the {@link org.xiyu.reforged.shim.NeoForgeEventBusShim} so that mods using
 * custom NeoForge event patterns still receive the event.</p>
 */
public class NeoServerStartingEvent extends Event {

    private final MinecraftServer server;

    public NeoServerStartingEvent(MinecraftServer server) {
        this.server = server;
    }

    /**
     * @return the server instance that is starting
     */
    public MinecraftServer getServer() {
        return server;
    }
}

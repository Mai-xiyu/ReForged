package net.neoforged.fml.event.lifecycle;

import net.neoforged.fml.event.IModBusEvent;

/**
 * NeoForge wrapper for Forge's {@link net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent}.
 * Fired when the mod is being constructed — the very first lifecycle event.
 */
public class FMLConstructModEvent extends net.neoforged.bus.api.Event implements IModBusEvent {

    private final net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent delegate;

    /** Wrapper constructor — used by NeoForgeEventBusAdapter to bridge Forge events. */
    public FMLConstructModEvent(net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent delegate) {
        this.delegate = delegate;
    }

    /** Returns the underlying Forge event. */
    public net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent getForgeDelegate() {
        return delegate;
    }
}

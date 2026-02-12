package net.neoforged.bus.api;

/**
 * Proxy for NeoForge's {@code IEventBus}.
 * Simply re-exports Forge's IEventBus so NeoForge mods' references resolve.
 *
 * <p>Since this interface extends Forge's IEventBus, any object implementing
 * Forge's IEventBus is also an instance of this interface. Method calls
 * from NeoForge mod bytecode (e.g. {@code INVOKEINTERFACE net/neoforged/bus/api/IEventBus.addListener})
 * will resolve through the inheritance chain to Forge's implementation.</p>
 */
public interface IEventBus extends net.minecraftforge.eventbus.api.IEventBus {
    // No additional methods — NeoForge's IEventBus API matches Forge's
}

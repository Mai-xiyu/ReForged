package net.neoforged.neoforge.client.extensions.common;

import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * Event for registering client extensions for various game objects.
 * Fired on the mod event bus.
 */
public class RegisterClientExtensionsEvent extends Event implements IModBusEvent {

    public RegisterClientExtensionsEvent() {
    }

    /**
     * Register client fluid type extensions.
     */
    public void registerFluidType(IClientFluidTypeExtensions extensions, FluidType... fluidTypes) {
        // Shim: client extensions registration
    }
}

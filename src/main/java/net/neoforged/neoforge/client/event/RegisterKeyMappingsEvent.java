package net.neoforged.neoforge.client.event;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.neoforged.fml.event.IModBusEvent;
import org.apache.commons.lang3.ArrayUtils;

/**
 * NeoForge wrapper for Forge's {@link net.minecraftforge.client.event.RegisterKeyMappingsEvent}.
 * Provides the same {@code register(KeyMapping)} API.
 */
public class RegisterKeyMappingsEvent extends net.neoforged.bus.api.Event implements IModBusEvent {

    private final net.minecraftforge.client.event.RegisterKeyMappingsEvent delegate;
    private final Options options;

    /** Wrapper constructor — used by NeoForgeEventBusAdapter to bridge Forge events. */
    public RegisterKeyMappingsEvent(net.minecraftforge.client.event.RegisterKeyMappingsEvent delegate) {
        this.delegate = delegate;
        this.options = null;
    }

	public RegisterKeyMappingsEvent(Options options) {
		this.delegate = null;
		this.options = options;
    }

    /**
     * Registers a new key mapping.
     */
    public void register(KeyMapping key) {
        if (delegate != null) {
        	delegate.register(key);
        } else {
            options.keyMappings = ArrayUtils.add(options.keyMappings, key);
        }
    }
}

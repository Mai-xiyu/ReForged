package net.neoforged.neoforge.registries;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Event for registering new data-pack driven registries.
 */
public class DataPackRegistryEvent extends net.neoforged.bus.api.Event implements IModBusEvent {

    public static class NewRegistry extends DataPackRegistryEvent {
        public <T> void dataPackRegistry(ResourceKey<Registry<T>> registryKey, Codec<T> codec) {
            dataPackRegistry(registryKey, codec, null);
        }

        public <T> void dataPackRegistry(ResourceKey<Registry<T>> registryKey, Codec<T> codec, @Nullable Codec<T> networkCodec) {
            // Stub: registration is handled by Forge's DataPackRegistriesHooks
        }
    }
}

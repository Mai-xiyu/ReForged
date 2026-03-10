package net.neoforged.neoforge.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Wrapper around Forge's {@link net.minecraftforge.registries.RegisterEvent}.
 * NeoForge mods use {@code event.register(registryKey, helper -> ...)} to register objects.
 *
 * <p>Extends {@link net.neoforged.bus.api.Event} so that NeoForge code (e.g. Registrate's
 * {@code OneTimeEventReceiver}) can safely cast to {@code Event}.</p>
 */
public class RegisterEvent extends net.neoforged.bus.api.Event implements IModBusEvent {
    private final net.minecraftforge.registries.RegisterEvent delegate;

    public RegisterEvent(net.minecraftforge.registries.RegisterEvent delegate) {
        this.delegate = delegate;
    }

    /**
     * @return The registry key linked to this event
     */
    public ResourceKey<? extends Registry<?>> getRegistryKey() {
        return delegate.getRegistryKey();
    }

    /**
     * @return The vanilla registry for this event, or {@code null}
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> Registry<T> getRegistry() {
        return (Registry<T>) delegate.getVanillaRegistry();
    }

    /**
     * @param key the registry key to compare against {@link #getRegistryKey()}
     * @return The registry typed to the given registry key if it matches, or {@code null}
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> Registry<T> getRegistry(ResourceKey<? extends Registry<T>> key) {
        return key.equals(this.getRegistryKey()) ? (Registry<T>) delegate.getVanillaRegistry() : null;
    }

    /**
     * Registers the value with the given name to the stored registry if the provided
     * registry key matches this event's registry key.
     */
    public <T> void register(ResourceKey<? extends Registry<T>> registryKey,
                             ResourceLocation name, Supplier<T> valueSupplier) {
        delegate.register(registryKey, name, valueSupplier);
    }

    /**
     * Register objects for the given registry. The consumer receives a helper
     * whose {@link RegisterHelper#register} method adds entries.
     */
    @SuppressWarnings("unchecked")
    public <T> void register(ResourceKey<? extends Registry<T>> registryKey,
                             Consumer<RegisterHelper<T>> consumer) {
        delegate.register(registryKey, forgeHelper -> {
            RegisterHelper<T> neoHelper = (id, value) -> forgeHelper.register(id, value);
            consumer.accept(neoHelper);
        });
    }

    @FunctionalInterface
    public interface RegisterHelper<T> {
        void register(ResourceLocation id, T value);

        default void register(ResourceKey<T> key, T value) {
            register(key.location(), value);
        }

        @SuppressWarnings("removal")
        default void register(String name, T value) {
            register(ResourceLocation.fromNamespaceAndPath(
                    net.minecraftforge.fml.ModLoadingContext.get().getActiveNamespace(), name), value);
        }
    }
}

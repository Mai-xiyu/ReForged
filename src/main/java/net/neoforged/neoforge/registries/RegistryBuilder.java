package net.neoforged.neoforge.registries;

import com.mojang.logging.LogUtils;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import com.mojang.serialization.Lifecycle;
import net.neoforged.neoforge.registries.callback.AddCallback;
import org.slf4j.Logger;

import java.util.function.Consumer;

/**
 * Proxy: NeoForge's RegistryBuilder for creating custom registries.
 * <p>
 * In NeoForge, {@code new RegistryBuilder<>(registryKey)} creates a builder
 * for a custom registry. Our shim stores the key and returns a lightweight
 * {@link MappedRegistry} stub from {@link #build()}.
 * </p>
 */
public class RegistryBuilder<T> {

    private static final Logger LOGGER = LogUtils.getLogger();

    private ResourceKey<? extends Registry<T>> registryKey;
    private boolean sync;
    private int maxId = Integer.MAX_VALUE - 1;
    private ResourceLocation defaultKey;

    public RegistryBuilder() {}

    /**
     * NeoForge-style constructor: {@code new RegistryBuilder<>(ResourceKey)}.
     */
    public RegistryBuilder(ResourceKey<? extends Registry<T>> registryKey) {
        this.registryKey = registryKey;
    }

    public RegistryBuilder<T> sync(boolean sync) {
        this.sync = sync;
        return this;
    }

    public RegistryBuilder<T> maxId(int maxId) {
        this.maxId = maxId;
        return this;
    }

    public RegistryBuilder<T> defaultKey(ResourceLocation defaultKey) {
        this.defaultKey = defaultKey;
        return this;
    }

    /**
     * Overload accepting ResourceKey (used by some NeoForge mods).
     */
    public RegistryBuilder<T> defaultKey(ResourceKey<?> defaultKey) {
        this.defaultKey = defaultKey.location();
        return this;
    }

    public RegistryBuilder<T> hasTags() {
        return this;
    }

    /** NeoForge callback: called when an entry is added. No-op in shim. */
    public RegistryBuilder<T> onAdd(AddCallback<T> callback) {
        return this;
    }

    /** NeoForge: called when the registry is cleared. No-op in shim. */
    public RegistryBuilder<T> onClear(Runnable callback) {
        return this;
    }

    /** NeoForge: called when the registry is baked. No-op in shim. */
    public RegistryBuilder<T> onBake(Consumer<Registry<T>> callback) {
        return this;
    }

    /** NeoForge: disallow modifications after registry freeze. No-op in shim. */
    public RegistryBuilder<T> disableSync() {
        return this;
    }

    /** NeoForge: disallow overrides. No-op in shim. */
    public RegistryBuilder<T> disableOverrides() {
        return this;
    }

    public ResourceKey<? extends Registry<T>> getRegistryKey() {
        return registryKey;
    }

    /**
     * Build the registry. Returns a lightweight MappedRegistry backed by the key.
     * This is a no-op shim — the registry exists only to satisfy API calls.
     */
    @SuppressWarnings("unchecked")
    public Registry<T> build() {
        if (registryKey == null) {
            LOGGER.warn("[ReForged] RegistryBuilder.build() called without a registry key, returning empty registry");
            registryKey = (ResourceKey<? extends Registry<T>>) (ResourceKey<?>)
                    ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("reforged", "unknown"));
        }
        LOGGER.debug("[ReForged] Building shim registry for key: {}", registryKey.location());
        return new MappedRegistry<>((ResourceKey<Registry<T>>) registryKey, Lifecycle.stable());
    }

    /**
     * NeoForge uses {@code create()} as the terminal method (equivalent to {@code build()}).
     */
    public Registry<T> create() {
        return build();
    }
}

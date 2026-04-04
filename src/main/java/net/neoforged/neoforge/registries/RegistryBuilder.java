package net.neoforged.neoforge.registries;

import com.mojang.logging.LogUtils;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import com.mojang.serialization.Lifecycle;
import net.neoforged.neoforge.registries.callback.AddCallback;
import net.neoforged.neoforge.registries.callback.BakeCallback;
import org.slf4j.Logger;

import java.lang.reflect.Field;
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
    private boolean intrusiveHolders;

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

    /** NeoForge: called when the registry is baked (BakeCallback variant). No-op in shim. */
    public RegistryBuilder<T> onBake(BakeCallback<T> callback) {
        return this;
    }

    /** NeoForge: use intrusive holders for this registry. */
    public RegistryBuilder<T> withIntrusiveHolders() {
        this.intrusiveHolders = true;
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
     * Build the registry. Returns a MappedRegistry and registers it in the root
     * registry (unfreezing temporarily if needed, similar to NeoForge's NewRegistryEvent).
     */
    @SuppressWarnings("unchecked")
    public Registry<T> build() {
        if (registryKey == null) {
            LOGGER.warn("[ReForged] RegistryBuilder.build() called without a registry key, returning empty registry");
            registryKey = (ResourceKey<? extends Registry<T>>) (ResourceKey<?>)
                    ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath("reforged", "unknown"));
        }
        LOGGER.debug("[ReForged] Building shim registry for key: {}", registryKey.location());
        MappedRegistry<T> registry = new MappedRegistry<>((ResourceKey<Registry<T>>) registryKey, Lifecycle.stable(), intrusiveHolders);

        // Register the custom registry in the root registry so lookups work.
        // The root registry is likely frozen by this point, so we unfreeze temporarily.
        try {
            Registry<Registry<?>> rootRegistry = (Registry<Registry<?>>) (Registry<?>) BuiltInRegistries.REGISTRY;
            if (!rootRegistry.containsKey(registryKey.location())) {
                boolean unfrozen = false;
                Field frozenField = null;
                try {
                    frozenField = findField(rootRegistry.getClass(), "frozen", "f_205845_");
                    if (frozenField != null) {
                        frozenField.setAccessible(true);
                        if (frozenField.getBoolean(rootRegistry)) {
                            frozenField.setBoolean(rootRegistry, false);
                            unfrozen = true;
                        }
                    }
                    Registry.register((Registry<Registry<?>>) rootRegistry, registryKey.location().toString(), (Registry<?>) registry);
                    LOGGER.info("[ReForged] Registered custom registry '{}' in root registry", registryKey.location());
                } catch (Exception e) {
                    LOGGER.warn("[ReForged] Could not register custom registry '{}': {}", registryKey.location(), e.getMessage());
                } finally {
                    if (unfrozen && frozenField != null) {
                        try { frozenField.setBoolean(rootRegistry, true); } catch (Exception ignored) {}
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("[ReForged] Failed to register custom registry in root: {}", e.getMessage());
        }

        return registry;
    }

    /**
     * Find a declared field in the class hierarchy, trying multiple names (SRG/official).
     */
    private static Field findField(Class<?> clazz, String... names) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (String name : names) {
                try {
                    return current.getDeclaredField(name);
                } catch (NoSuchFieldException ignored) {}
            }
            current = current.getSuperclass();
        }
        return null;
    }

    /**
     * NeoForge uses {@code create()} as the terminal method (equivalent to {@code build()}).
     */
    public Registry<T> create() {
        return build();
    }
}

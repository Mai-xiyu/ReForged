package net.neoforged.neoforge.registries;

import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.callback.RegistryCallback;
import net.neoforged.neoforge.registries.datamaps.DataMapStorage;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jetbrains.annotations.Nullable;

/**
 * Extension interface for {@link Registry}, adding NeoForge-specific functionality such as
 * callbacks, aliases, ID limits, and data map access.
 *
 * @param <T> the type of registry entries
 */
public interface IRegistryExtension<T> {
    @SuppressWarnings("unchecked")
    private Registry<T> self() {
        return (Registry<T>) this;
    }

    /**
     * {@return whether this registry should be synced to clients}
     */
    default boolean doesSync() {
        return false;
    }

    /**
     * {@return the highest id that an entry in this registry is allowed to use}
     */
    default int getMaxId() {
        return Integer.MAX_VALUE - 1;
    }

    /**
     * Adds a callback to this registry.
     */
    default void addCallback(RegistryCallback<T> callback) {
        // Default no-op; implemented by patched Registry
    }

    default <C extends RegistryCallback<T>> void addCallback(Class<C> type, C callback) {
        addCallback(callback);
    }

    /**
     * Adds an alias that maps from one name to another.
     */
    default void addAlias(ResourceLocation from, ResourceLocation to) {
        // Default no-op; implemented by patched Registry
    }

    /**
     * Resolves a registry name, following aliases if the name is not directly registered.
     */
    default ResourceLocation resolve(ResourceLocation name) {
        return name;
    }

    /**
     * Resolves a registry key, following aliases if the key is not directly registered.
     */
    default ResourceKey<T> resolve(ResourceKey<T> key) {
        ResourceLocation resolved = resolve(key.location());
        return resolved == key.location() ? key : ResourceKey.create(key.registryKey(), resolved);
    }

    /**
     * Gets the integer id linked to the given key.
     */
    default int getId(ResourceKey<T> key) {
        return self().getId(self().get(key));
    }

    /**
     * Gets the integer id linked to the given name.
     */
    default int getId(ResourceLocation name) {
        return self().getId(self().get(name));
    }

    /**
     * {@return true if this registry contains the value}
     */
    default boolean containsValue(T value) {
        return self().getKey(value) != null;
    }

    /**
     * Gets the data map value for an object with the given key.
     */
    @Nullable
    default <A> A getData(DataMapType<T, A> type, ResourceKey<T> key) {
        Map<ResourceKey<T>, A> map = getDataMap(type);
        return map.get(key);
    }

    /**
     * {@return the data map of the given type}
     */
    default <A> Map<ResourceKey<T>, A> getDataMap(DataMapType<T, A> type) {
        return DataMapStorage.getDataMap(self().key(), type);
    }

    /**
     * {@return the key of the element, or null if not present}
     */
    @Nullable
    default ResourceLocation getKeyOrNull(T element) {
        return self().getKey(element);
    }
}

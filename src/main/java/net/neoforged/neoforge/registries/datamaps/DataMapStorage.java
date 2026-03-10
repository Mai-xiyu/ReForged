// Copyright (c) 2025-2026 ReForged Contributors — MIT License
package net.neoforged.neoforge.registries.datamaps;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global storage for NeoForge data map values.
 *
 * <p>Data maps are populated by:
 * <ol>
 *   <li>Mods registering types via {@link RegisterDataMapTypesEvent}</li>
 *   <li>JSON loading from datapacks (when implemented)</li>
 *   <li>Direct API calls for bridged vanilla/Forge equivalents</li>
 * </ol>
 *
 * <p>Queried by {@link net.neoforged.neoforge.registries.IRegistryExtension#getDataMap(DataMapType)}
 * and {@link IWithData#getData(DataMapType)}.
 */
public final class DataMapStorage {
    private DataMapStorage() {}

    /**
     * Registry key → DataMapType → ResourceKey → value
     */
    @SuppressWarnings("rawtypes")
    private static final Map<ResourceKey<? extends Registry<?>>, Map<DataMapType, Map<ResourceKey<?>, Object>>> STORAGE =
            new ConcurrentHashMap<>();

    /**
     * Get the entire data map for a given type under a given registry.
     */
    @SuppressWarnings("unchecked")
    public static <T, A> Map<ResourceKey<T>, A> getDataMap(ResourceKey<? extends Registry<T>> registryKey, DataMapType<T, A> type) {
        var registryMaps = STORAGE.get(registryKey);
        if (registryMaps == null) return Map.of();
        var map = registryMaps.get(type);
        return map != null ? (Map<ResourceKey<T>, A>) (Map<?, ?>) map : Map.of();
    }

    /**
     * Get a single data map value for a specific registry entry key.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public static <T, A> A getData(ResourceKey<? extends Registry<T>> registryKey, DataMapType<T, A> type, ResourceKey<T> key) {
        var registryMaps = STORAGE.get(registryKey);
        if (registryMaps == null) return null;
        var map = registryMaps.get(type);
        return map != null ? (A) map.get(key) : null;
    }

    /**
     * Store a data map value for a specific registry entry.
     */
    @SuppressWarnings("rawtypes")
    public static <T, A> void put(ResourceKey<? extends Registry<T>> registryKey, DataMapType<T, A> type, ResourceKey<T> key, A value) {
        var registryMaps = STORAGE.computeIfAbsent(registryKey, k -> new ConcurrentHashMap<>());
        var map = (Map<ResourceKey<?>, Object>) (Map) registryMaps.computeIfAbsent(type, k -> new ConcurrentHashMap<>());
        map.put(key, value);
    }

    /**
     * Remove a data map value for a specific registry entry.
     */
    @SuppressWarnings("rawtypes")
    public static <T, A> void remove(ResourceKey<? extends Registry<T>> registryKey, DataMapType<T, A> type, ResourceKey<T> key) {
        var registryMaps = STORAGE.get(registryKey);
        if (registryMaps == null) return;
        var map = registryMaps.get(type);
        if (map != null) map.remove(key);
    }

    /**
     * Clear all data maps (used on world reload).
     */
    public static void clear() {
        STORAGE.clear();
    }

    /**
     * Clear data maps for a specific registry.
     */
    public static void clear(ResourceKey<? extends Registry<?>> registryKey) {
        STORAGE.remove(registryKey);
    }
}

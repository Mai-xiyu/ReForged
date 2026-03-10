package net.neoforged.neoforge.registries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Internal hooks for managing data pack registries, including modded ones.
 */
@ApiStatus.Internal
public final class DataPackRegistriesHooks {
    private DataPackRegistriesHooks() {}

    private static final List<RegistryDataLoader.RegistryData<?>> NETWORKABLE_REGISTRIES = new ArrayList<>();
    private static final List<RegistryDataLoader.RegistryData<?>> DATA_PACK_REGISTRIES = new ArrayList<>(RegistryDataLoader.WORLDGEN_REGISTRIES);
    private static final List<RegistryDataLoader.RegistryData<?>> DATA_PACK_REGISTRIES_VIEW = Collections.unmodifiableList(DATA_PACK_REGISTRIES);
    private static final Set<ResourceKey<? extends Registry<?>>> SYNCED_CUSTOM_REGISTRIES = new HashSet<>();
    private static final Set<ResourceKey<? extends Registry<?>>> SYNCED_CUSTOM_REGISTRIES_VIEW = Collections.unmodifiableSet(SYNCED_CUSTOM_REGISTRIES);

    /**
     * Internal hook that retains mutable access to RegistryAccess's codec registry.
     */
    public static List<RegistryDataLoader.RegistryData<?>> grabNetworkableRegistries(List<RegistryDataLoader.RegistryData<?>> list) {
        List<RegistryDataLoader.RegistryData<?>> builder = new ArrayList<>(list);
        builder.addAll(NETWORKABLE_REGISTRIES);
        NETWORKABLE_REGISTRIES.clear();
        NETWORKABLE_REGISTRIES.addAll(builder);
        return Collections.unmodifiableList(NETWORKABLE_REGISTRIES);
    }

    @SuppressWarnings("unchecked")
    static <T> void addRegistryCodec(RegistryDataLoader.RegistryData<T> loaderData, @Nullable com.mojang.serialization.Codec<T> networkCodec) {
        DATA_PACK_REGISTRIES.add(loaderData);
        if (networkCodec != null) {
            SYNCED_CUSTOM_REGISTRIES.add(loaderData.key());
            NETWORKABLE_REGISTRIES.add(new RegistryDataLoader.RegistryData<>(loaderData.key(), networkCodec, false));
        }
    }

    /**
     * {@return An unmodifiable view of the list of datapack registries}
     */
    public static List<RegistryDataLoader.RegistryData<?>> getDataPackRegistries() {
        return DATA_PACK_REGISTRIES_VIEW;
    }

    public static Stream<RegistryDataLoader.RegistryData<?>> getDataPackRegistriesWithDimensions() {
        return Stream.concat(DATA_PACK_REGISTRIES_VIEW.stream(), RegistryDataLoader.DIMENSION_REGISTRIES.stream());
    }

    /**
     * {@return An unmodifiable view of the set of synced non-vanilla datapack registry IDs}
     */
    public static Set<ResourceKey<? extends Registry<?>>> getSyncedCustomRegistries() {
        return SYNCED_CUSTOM_REGISTRIES_VIEW;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> RegistryDataLoader.RegistryData<T> getSyncedRegistry(final ResourceKey<? extends Registry<T>> registry) {
        return (RegistryDataLoader.RegistryData<T>) NETWORKABLE_REGISTRIES.stream()
                .filter(data -> data.key().equals(registry))
                .findFirst().orElse(null);
    }

    /**
     * Passthrough compatibility method.
     */
    public static RegistryAccess.Frozen wrapRegistryAccess(RegistryAccess.Frozen registryAccess) {
        return registryAccess;
    }
}

package net.neoforged.neoforge.common.data;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.data.registries.RegistriesDatapackGenerator;

/**
 * Stub: NeoForge DatapackBuiltinEntriesProvider — provider for datapack registry entries.
 */
public class DatapackBuiltinEntriesProvider extends RegistriesDatapackGenerator {
    private final CompletableFuture<HolderLookup.Provider> fullRegistries;

    public DatapackBuiltinEntriesProvider(PackOutput output,
                                           CompletableFuture<HolderLookup.Provider> registries,
                                           RegistrySetBuilder datapackEntriesBuilder,
                                           Set<String> modIds) {
        super(output, registries, modIds);
        this.fullRegistries = registries;
    }

    public DatapackBuiltinEntriesProvider(PackOutput output,
                                           CompletableFuture<RegistrySetBuilder.PatchedRegistries> registries,
                                           Set<String> modIds) {
        super(output, registries.thenApply(RegistrySetBuilder.PatchedRegistries::patches), modIds);
        this.fullRegistries = registries.thenApply(RegistrySetBuilder.PatchedRegistries::full);
    }

    public CompletableFuture<HolderLookup.Provider> getRegistryProvider() {
        return fullRegistries;
    }
}

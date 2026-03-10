package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

/**
 * Extension interface for HolderLookup.Provider.
 */
public interface IHolderLookupProviderExtension {

    @Nullable
    default <T> HolderLookup.RegistryLookup<T> lookup(ResourceKey<? extends Registry<T>> key) {
        if (this instanceof HolderLookup.Provider provider) {
            return provider.lookup(key).orElse(null);
        }
        return null;
    }
}

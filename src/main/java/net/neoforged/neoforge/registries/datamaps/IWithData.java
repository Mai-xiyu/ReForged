package net.neoforged.neoforge.registries.datamaps;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for objects that carry data map data.
 * In NeoForge, this is mixed into Holder via IHolderExtension.
 */
public interface IWithData<T> {

    @Nullable
    default <D> D getData(DataMapType<T, D> type) {
        // Attempt to resolve via holder's resource key → DataMapStorage
        if (this instanceof Holder<?> holder) {
            @SuppressWarnings("unchecked")
            var h = (Holder<T>) holder;
            var optKey = h.unwrapKey();
            if (optKey.isPresent()) {
                ResourceKey<T> key = optKey.get();
                return DataMapStorage.getData(type.registryKey(), type, key);
            }
        }
        return null;
    }
}

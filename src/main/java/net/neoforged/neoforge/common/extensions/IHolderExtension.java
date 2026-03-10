package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

/**
 * Extension interface for {@link Holder}.
 */
public interface IHolderExtension<T> {

    @SuppressWarnings("unchecked")
    default Holder<T> getDelegate() { return (Holder<T>) this; }

    @Nullable
    default ResourceKey<T> getKey() { return ((Holder<T>) this).unwrapKey().orElse(null); }
}

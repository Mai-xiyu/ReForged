package net.neoforged.neoforge.registries.holdersets;

import net.minecraft.core.HolderSet;

/**
 * Interface for custom holder sets defined by NeoForge.
 * Implementations must return a registered {@link HolderSetType}.
 */
public interface ICustomHolderSet<T> extends HolderSet<T> {
    /**
     * @return the holder set type registered in the HOLDER_SET_TYPES registry
     */
    HolderSetType type();
}

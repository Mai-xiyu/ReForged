package net.neoforged.neoforge.registries.callback;

import net.minecraft.core.Registry;

/**
 * Fired when the registry is cleared before a reload.
 */
@FunctionalInterface
public interface ClearCallback<T> extends RegistryCallback<T> {
    /**
     * @param registry the registry
     * @param full     if true, all entries cleared; if false, only integer IDs cleared
     */
    void onClear(Registry<T> registry, boolean full);
}

package net.createmod.ponder.mixin.accessor;

import net.minecraft.world.level.storage.LevelStorageSource;

/**
 * Accessor interface matching Ponder's MinecraftServerAccessor mixin.
 * Implemented via {@link org.xiyu.reforged.mixin.MinecraftServerAccessorMixin}.
 */
public interface MinecraftServerAccessor {
    LevelStorageSource.LevelStorageAccess catnip$getStorageSource();
}

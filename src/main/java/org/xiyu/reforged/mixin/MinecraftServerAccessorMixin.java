package org.xiyu.reforged.mixin;

import net.createmod.ponder.mixin.accessor.MinecraftServerAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerAccessorMixin implements MinecraftServerAccessor {

    @Shadow @Final
    public LevelStorageSource.LevelStorageAccess storageSource;

    @Override
    public LevelStorageSource.LevelStorageAccess catnip$getStorageSource() {
        return this.storageSource;
    }
}

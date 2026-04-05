package org.xiyu.reforged.mixin;

import net.createmod.ponder.mixin.accessor.BiomeManagerAccessor;
import net.minecraft.world.level.biome.BiomeManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = BiomeManager.class, remap = false)
public abstract class BiomeManagerAccessorMixin implements BiomeManagerAccessor {

    @Shadow @Final
    public long biomeZoomSeed;

    @Override
    public long catnip$getBiomeZoomSeed() {
        return this.biomeZoomSeed;
    }
}

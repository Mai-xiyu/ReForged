package org.xiyu.reforged.mixin;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Injects Flywheel's SkyDataLayerStorageMapAccessor methods:
 * flywheel$currentLowestY() and flywheel$topSections()
 * Used by Flywheel's sky light calculation for GPU rendering.
 */
@Mixin(targets = "net.minecraft.world.level.lighting.SkyLightSectionStorage$SkyDataLayerStorageMap", remap = false)
public abstract class FlywheelSkyDataLayerStorageMapAccessorMixin implements FlywheelSkyStorageMapHelper {

    @Shadow(remap = false)
    int currentLowestY;

    @Shadow(remap = false) @Final
    Long2IntOpenHashMap topSections;

    @Override
    public int flywheel$currentLowestY() {
        return this.currentLowestY;
    }

    @Override
    public Long2IntOpenHashMap flywheel$topSections() {
        return this.topSections;
    }
}

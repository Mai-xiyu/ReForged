package org.xiyu.reforged.mixin;

import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Injects Flywheel's LayerLightSectionStorageAccessor.flywheel$callGetDataLayer()
 * so Flywheel can efficiently read light data for its GPU-accelerated renderer.
 */
@Mixin(value = LayerLightSectionStorage.class, remap = false)
public abstract class FlywheelLayerLightStorageAccessorMixin {

    @Shadow(remap = false)
    protected abstract DataLayer getDataLayer(long sectionPos, boolean isSkyLight);

    public DataLayer flywheel$callGetDataLayer(long sectionPos, boolean isSkyLight) {
        return this.getDataLayer(sectionPos, isSkyLight);
    }
}

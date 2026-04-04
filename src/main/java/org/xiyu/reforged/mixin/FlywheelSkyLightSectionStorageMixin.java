package org.xiyu.reforged.mixin;

import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.DataLayerStorageMap;
import net.minecraft.world.level.lighting.SkyLightSectionStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Replicates Flywheel's SkyLightSectionStorageMixin — injects
 * flywheel$skyDataLayer(long) for Flywheel's GPU-accelerated sky lighting.
 *
 * <p>Traverses upward through light sections to find the appropriate
 * sky data layer for a given section position.</p>
 */
@Mixin(value = SkyLightSectionStorage.class, remap = false)
public abstract class FlywheelSkyLightSectionStorageMixin {

    @Shadow(remap = false)
    protected volatile DataLayerStorageMap visibleSectionData;

    @Shadow(remap = false)
    public abstract DataLayer getDataLayerData(long sectionPos);

    /**
     * Flywheel's SkyLightSectionStorageExtension.flywheel$skyDataLayer(long).
     */
    public DataLayer flywheel$skyDataLayer(long sectionPos) {
        long pos = sectionPos;
        int sectionY = SectionPos.y(pos);

        // visibleSectionData is SkyDataLayerStorageMap at runtime in SkyLightSectionStorage.
        // Our FlywheelSkyDataLayerStorageMapAccessorMixin makes it implement FlywheelSkyStorageMapHelper.
        FlywheelSkyStorageMapHelper storageMap = (FlywheelSkyStorageMapHelper) this.visibleSectionData;

        int topSectionY = storageMap.flywheel$topSections().get(SectionPos.getZeroNode(pos));

        if (topSectionY != storageMap.flywheel$currentLowestY()) {
            if (sectionY >= topSectionY) {
                return null;
            }

            DataLayer dataLayer = this.getDataLayerData(pos);
            if (dataLayer != null) {
                return dataLayer;
            }

            while (dataLayer == null) {
                sectionY++;
                if (sectionY >= topSectionY) {
                    return null;
                }
                pos = SectionPos.offset(pos, Direction.UP);
                dataLayer = this.getDataLayerData(pos);
            }
            return dataLayer;
        }
        return null;
    }
}

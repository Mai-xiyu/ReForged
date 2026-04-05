package org.xiyu.reforged.mixin;

import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.DataLayerStorageMap;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.xiyu.reforged.bridge.FlywheelSkyStorageMapHelper;

/**
 * Injects Flywheel accessor/extension methods into {@link LayerLightSectionStorage}.
 *
 * <ul>
 *   <li>{@code flywheel$callGetDataLayer()} — Flywheel's GPU light data reader</li>
 *   <li>{@code flywheel$skyDataLayer()} — Sky-light section traversal for GPU rendering
 *       (originally on SkyLightSectionStorage, moved here to avoid @Shadow inheritance
 *       resolution issues when no refMap is loaded)</li>
 * </ul>
 */
@Mixin(value = LayerLightSectionStorage.class, remap = false)
public abstract class FlywheelLayerLightStorageAccessorMixin {

    @Shadow(remap = false)
    protected volatile DataLayerStorageMap visibleSectionData;

    @Shadow(remap = false)
    protected abstract DataLayer getDataLayer(long sectionPos, boolean isSkyLight);

    public DataLayer flywheel$callGetDataLayer(long sectionPos, boolean isSkyLight) {
        return this.getDataLayer(sectionPos, isSkyLight);
    }

    /**
     * Flywheel's SkyLightSectionStorageExtension.flywheel$skyDataLayer(long).
     *
     * <p>Traverses upward through sky-light sections to find the appropriate data layer.
     * Only meaningful when {@code this} is a {@code SkyLightSectionStorage} instance
     * (i.e. when {@code visibleSectionData} is a {@code SkyDataLayerStorageMap}).</p>
     */
    public DataLayer flywheel$skyDataLayer(long sectionPos) {
        // Guard: only meaningful for SkyLightSectionStorage
        if (!(this.visibleSectionData instanceof FlywheelSkyStorageMapHelper storageMap)) {
            return null;
        }

        long pos = sectionPos;
        int sectionY = SectionPos.y(pos);

        int topSectionY = storageMap.flywheel$topSections().get(SectionPos.getZeroNode(pos));

        if (topSectionY != storageMap.flywheel$currentLowestY()) {
            if (sectionY >= topSectionY) {
                return null;
            }

            DataLayer dataLayer = this.visibleSectionData.getLayer(pos);
            if (dataLayer != null) {
                return dataLayer;
            }

            while (dataLayer == null) {
                sectionY++;
                if (sectionY >= topSectionY) {
                    return null;
                }
                pos = SectionPos.offset(pos, Direction.UP);
                dataLayer = this.visibleSectionData.getLayer(pos);
            }
            return dataLayer;
        }
        return null;
    }
}

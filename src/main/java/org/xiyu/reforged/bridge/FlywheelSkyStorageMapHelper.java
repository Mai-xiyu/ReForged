package org.xiyu.reforged.bridge;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

/**
 * Helper interface for flywheel$skyDataLayer() to access fields on
 * SkyLightSectionStorage.SkyDataLayerStorageMap via our mixin.
 * Unlike Flywheel's accessor (loaded by NeoModClassLoader), this interface
 * lives in the TRANSFORMER classloader alongside our mixins.
 */
public interface FlywheelSkyStorageMapHelper {
    int flywheel$currentLowestY();
    Long2IntOpenHashMap flywheel$topSections();
}

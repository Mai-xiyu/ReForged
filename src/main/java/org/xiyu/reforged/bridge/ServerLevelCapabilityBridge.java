package org.xiyu.reforged.bridge;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.capabilities.ICapabilityInvalidationListener;

public interface ServerLevelCapabilityBridge {
    void reforged$invalidateCapabilities(BlockPos pos);

    void reforged$invalidateCapabilities(ChunkPos pos);

    void reforged$registerCapabilityListener(BlockPos pos, ICapabilityInvalidationListener listener);

    void reforged$cleanCapabilityListenerReferences();
}
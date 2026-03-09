package org.xiyu.reforged.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.capabilities.CapabilityListenerHolder;
import net.neoforged.neoforge.capabilities.ICapabilityInvalidationListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.xiyu.reforged.bridge.ServerLevelCapabilityBridge;

@Mixin(ServerLevel.class)
public abstract class ServerLevelCapabilityMixin implements ServerLevelCapabilityBridge {

    @Unique
    private final CapabilityListenerHolder reforged$capabilityListenerHolder = new CapabilityListenerHolder();

    public void invalidateCapabilities(BlockPos pos) {
        reforged$invalidateCapabilities(pos);
    }

    public void invalidateCapabilities(ChunkPos pos) {
        reforged$invalidateCapabilities(pos);
    }

    public void registerCapabilityListener(BlockPos pos, ICapabilityInvalidationListener listener) {
        reforged$registerCapabilityListener(pos, listener);
    }

    public void cleanCapabilityListenerReferences() {
        reforged$cleanCapabilityListenerReferences();
    }

    @Override
    public void reforged$invalidateCapabilities(BlockPos pos) {
        reforged$capabilityListenerHolder.invalidatePos(pos);
    }

    @Override
    public void reforged$invalidateCapabilities(ChunkPos pos) {
        reforged$capabilityListenerHolder.invalidateChunk(pos);
    }

    @Override
    public void reforged$registerCapabilityListener(BlockPos pos, ICapabilityInvalidationListener listener) {
        reforged$capabilityListenerHolder.addListener(pos, listener);
    }

    @Override
    public void reforged$cleanCapabilityListenerReferences() {
        reforged$capabilityListenerHolder.clean();
    }
}
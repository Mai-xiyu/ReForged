package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Stub extension interface for BlockEntity.
 */
public interface IBlockEntityExtension {

    default BlockEntity self() {
        return (BlockEntity) this;
    }

    default void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        Level level = self().getLevel();
        if (level != null) {
            onDataPacket(net, pkt, level.registryAccess());
        }
    }

    default void handleUpdateTag(CompoundTag tag) {
        Level level = self().getLevel();
        if (level != null) {
            handleUpdateTag(tag, level.registryAccess());
        }
    }

    default void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        CompoundTag compoundTag = pkt.getTag();
        if (compoundTag != null && !compoundTag.isEmpty()) {
            self().loadWithComponents(compoundTag, lookupProvider);
        }
    }

    default void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        self().loadWithComponents(tag, lookupProvider);
    }

    default CompoundTag getPersistentData() {
        return new CompoundTag();
    }

    default void onChunkUnloaded() {
    }

    default void onLoad() {
        requestModelDataUpdate();
    }

    default void requestModelDataUpdate() {
        BlockEntity blockEntity = self();
        Level level = blockEntity.getLevel();
        if (level != null && level.isClientSide) {
            var modelDataManager = level.getModelDataManager();
            if (modelDataManager != null) {
                modelDataManager.requestRefresh(blockEntity);
            }
        }
    }

    default boolean hasCustomOutlineRendering(Player player) {
        return false;
    }

    default void invalidateCapabilities() {
    }
}

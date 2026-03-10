package net.neoforged.neoforge.client.model.data;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager for model data — tracks per-position model data for block rendering.
 */
public class ModelDataManager {
    private final ConcurrentHashMap<BlockPos, ModelData> dataMap = new ConcurrentHashMap<>();

    @Nullable
    public ModelData getAt(BlockPos pos) {
        return dataMap.get(pos);
    }

    public void setAt(BlockPos pos, ModelData data) {
        dataMap.put(pos.immutable(), data);
    }

    public void removeAt(BlockPos pos) {
        dataMap.remove(pos);
    }

    /**
     * Requests a refresh of model data at the given position.
     * Delegates to Forge's per-level ModelDataManager via the BlockEntity.
     */
    public static void requestModelDataRefresh(BlockAndTintGetter level, BlockPos pos) {
        if (level instanceof Level lvl && lvl.isClientSide) {
            BlockEntity be = lvl.getBlockEntity(pos);
            if (be != null) {
                net.minecraftforge.client.model.data.ModelDataManager forgeManager = lvl.getModelDataManager();
                if (forgeManager != null) {
                    forgeManager.requestRefresh(be);
                }
            }
        }
    }
}

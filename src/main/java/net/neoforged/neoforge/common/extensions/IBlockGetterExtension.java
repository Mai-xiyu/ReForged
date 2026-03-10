package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
import org.jetbrains.annotations.Nullable;

/**
 * Extension interface for {@link net.minecraft.world.level.BlockGetter}.
 */
public interface IBlockGetterExtension {

    @Nullable
    default AuxiliaryLightManager getAuxLightManager(BlockPos pos) { return null; }

    @Nullable
    default AuxiliaryLightManager getAuxLightManager(ChunkPos pos) { return null; }

    default ModelData getModelData(BlockPos pos) { return ModelData.EMPTY; }
}

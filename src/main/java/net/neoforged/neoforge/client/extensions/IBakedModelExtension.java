package net.neoforged.neoforge.client.extensions;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import javax.annotation.Nullable;

/**
 * Extension interface for {@link BakedModel} to support NeoForge model data and render type.
 */
public interface IBakedModelExtension {

    /**
     * Returns the model data for this model in the given context.
     */
    default ModelData getModelData() {
        return ModelData.EMPTY;
    }

    /**
     * Override to provide custom render types for this model.
     */
    default net.neoforged.neoforge.client.ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return net.neoforged.neoforge.client.ChunkRenderTypeSet.all();
    }
}

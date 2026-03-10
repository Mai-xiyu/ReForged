package net.neoforged.neoforge.client.model;

import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import javax.annotation.Nullable;

/**
 * Convenience interface for baked models that need to be dynamic (context-dependent quads).
 */
public interface IDynamicBakedModel extends BakedModel {
    @Override
    default List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return getQuads(state, side, rand, ModelData.EMPTY, null);
    }

    List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType);
}

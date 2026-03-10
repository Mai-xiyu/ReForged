package net.neoforged.neoforge.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;

import javax.annotation.Nullable;

/**
 * Wrapper for {@link BakedModel} which delegates all operations to its parent.
 */
public abstract class BakedModelWrapper<T extends BakedModel> implements BakedModel {
    protected final T originalModel;

    public BakedModelWrapper(T originalModel) {
        this.originalModel = originalModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return originalModel.getQuads(state, side, rand);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return originalModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return originalModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return originalModel.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return originalModel.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return originalModel.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return originalModel.getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return originalModel.getOverrides();
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext cameraTransformType, PoseStack poseStack, boolean applyLeftHandTransform) {
        return originalModel.applyTransform(cameraTransformType, poseStack, applyLeftHandTransform);
    }

    public TextureAtlasSprite getParticleIcon(ModelData data) {
        return originalModel.getParticleIcon();
    }

    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
        return originalModel.getQuads(state, side, rand);
    }

    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        return modelData;
    }

    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        return ChunkRenderTypeSet.all();
    }

    public List<RenderType> getRenderTypes(ItemStack itemStack, boolean fabulous) {
        return List.of();
    }

    public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
        return List.of(this);
    }
}

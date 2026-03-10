package net.neoforged.neoforge.client.model;

import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.RenderTypeGroup;

/**
 * Builder interface for collecting culled and unculled quads and baking them into a model.
 */
public interface IModelBuilder<T extends IModelBuilder<T>> {
    static IModelBuilder<?> of(boolean hasAmbientOcclusion, boolean usesBlockLight, boolean isGui3d,
            ItemTransforms transforms, ItemOverrides overrides, TextureAtlasSprite particle,
            RenderTypeGroup renderTypes) {
        return new Simple(hasAmbientOcclusion, usesBlockLight, isGui3d, transforms, overrides, particle, renderTypes);
    }

    static IModelBuilder<?> collecting(List<BakedQuad> quads) {
        return new Collecting(quads);
    }

    T addCulledFace(Direction facing, BakedQuad quad);

    T addUnculledFace(BakedQuad quad);

    BakedModel build();

    class Simple implements IModelBuilder<Simple> {
        private final SimpleBakedModel.Builder builder;
        private final RenderTypeGroup renderTypes;

        private Simple(boolean hasAmbientOcclusion, boolean usesBlockLight, boolean isGui3d,
                ItemTransforms transforms, ItemOverrides overrides, TextureAtlasSprite particle,
                RenderTypeGroup renderTypes) {
            this.builder = new SimpleBakedModel.Builder(hasAmbientOcclusion, usesBlockLight, isGui3d, transforms, overrides).particle(particle);
            this.renderTypes = renderTypes;
        }

        @Override
        public Simple addCulledFace(Direction facing, BakedQuad quad) {
            builder.addCulledFace(facing, quad);
            return this;
        }

        @Override
        public Simple addUnculledFace(BakedQuad quad) {
            builder.addUnculledFace(quad);
            return this;
        }

        @Override
        public BakedModel build() {
            builder.renderTypes(renderTypes.toForge());
            return builder.build();
        }
    }

    class Collecting implements IModelBuilder<Collecting> {
        private final List<BakedQuad> quads;

        private Collecting(List<BakedQuad> quads) {
            this.quads = quads;
        }

        @Override
        public Collecting addCulledFace(Direction facing, BakedQuad quad) {
            quads.add(quad);
            return this;
        }

        @Override
        public Collecting addUnculledFace(BakedQuad quad) {
            quads.add(quad);
            return this;
        }

        @Override
        public BakedModel build() {
            throw new UnsupportedOperationException("Collecting builder cannot build a model.");
        }
    }
}

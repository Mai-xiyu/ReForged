package net.neoforged.neoforge.client.model;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;

import javax.annotation.Nullable;

public class CompositeModel {

    public static class Baked implements BakedModel {
        private final List<BakedQuad> quads;
        private final boolean usesBlockLight;
        private final TextureAtlasSprite particle;
        private final ItemOverrides overrides;
        private final ItemTransforms transforms;

        private Baked(List<BakedQuad> quads, boolean usesBlockLight, TextureAtlasSprite particle, ItemOverrides overrides, ItemTransforms transforms) {
            this.quads = quads;
            this.usesBlockLight = usesBlockLight;
            this.particle = particle;
            this.overrides = overrides;
            this.transforms = transforms;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
            return side == null ? quads : List.of();
        }

        @Override public boolean useAmbientOcclusion() { return true; }
        @Override public boolean isGui3d() { return false; }
        @Override public boolean usesBlockLight() { return usesBlockLight; }
        @Override public boolean isCustomRenderer() { return false; }
        @Override public TextureAtlasSprite getParticleIcon() { return particle; }
        @Override public ItemTransforms getTransforms() { return transforms; }
        @Override public ItemOverrides getOverrides() { return overrides; }

        public static Builder builder(IGeometryBakingContext context, TextureAtlasSprite particle, ItemOverrides overrides, ItemTransforms transforms) {
            return new Builder(context.useBlockLight(), particle, overrides, transforms);
        }

        public static class Builder {
            private final List<BakedQuad> quads = new ArrayList<>();
            private final boolean usesBlockLight;
            private final TextureAtlasSprite particle;
            private final ItemOverrides overrides;
            private final ItemTransforms transforms;

            public Builder(boolean usesBlockLight, TextureAtlasSprite particle, ItemOverrides overrides, ItemTransforms transforms) {
                this.usesBlockLight = usesBlockLight;
                this.particle = particle;
                this.overrides = overrides;
                this.transforms = transforms;
            }

            public Builder addQuads(RenderTypeGroup renderTypes, List<BakedQuad> quads) {
                this.quads.addAll(quads);
                return this;
            }

            public Baked build() {
                return new Baked(quads, usesBlockLight, particle, overrides, transforms);
            }
        }
    }
}

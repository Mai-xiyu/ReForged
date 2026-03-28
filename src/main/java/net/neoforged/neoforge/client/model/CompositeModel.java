package net.neoforged.neoforge.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.common.util.ConcatenatedListView;

import javax.annotation.Nullable;

/**
 * A model composed of several named children.
 * <p>
 * NeoForge shim — mirrors the Forge CompositeModel structure so that NeoForge mods
 * can instantiate {@link Baked} with the 8-parameter constructor they expect.
 */
public class CompositeModel {

    /**
     * A model data container which stores data for child components.
     */
    public static class Data {
        public static final ModelProperty<Data> PROPERTY = new ModelProperty<>();

        private final Map<String, ModelData> partData;

        private Data(Map<String, ModelData> partData) {
            this.partData = partData;
        }

        @Nullable
        public ModelData get(String name) {
            return partData.get(name);
        }

        public static ModelData resolve(ModelData modelData, String name) {
            var compositeData = modelData.get(PROPERTY);
            if (compositeData == null)
                return modelData;
            var partData = compositeData.get(name);
            return partData != null ? partData : modelData;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            private final Map<String, ModelData> partData = new IdentityHashMap<>();

            public Builder with(String name, ModelData data) {
                partData.put(name, data);
                return this;
            }

            public Data build() {
                return new Data(partData);
            }
        }
    }

    public static class Baked implements IDynamicBakedModel {
        private final boolean isAmbientOcclusion;
        private final boolean isGui3d;
        private final boolean isSideLit;
        private final TextureAtlasSprite particle;
        private final ItemOverrides overrides;
        private final ItemTransforms transforms;
        private final ImmutableMap<String, BakedModel> children;
        private final ImmutableList<BakedModel> itemPasses;

        public Baked(boolean isGui3d, boolean isSideLit, boolean isAmbientOcclusion,
                     TextureAtlasSprite particle, ItemTransforms transforms, ItemOverrides overrides,
                     ImmutableMap<String, BakedModel> children, ImmutableList<BakedModel> itemPasses) {
            this.children = children;
            this.isAmbientOcclusion = isAmbientOcclusion;
            this.isGui3d = isGui3d;
            this.isSideLit = isSideLit;
            this.particle = particle;
            this.overrides = overrides;
            this.transforms = transforms;
            this.itemPasses = itemPasses;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                        RandomSource rand, ModelData data, @Nullable RenderType renderType) {
            List<List<BakedQuad>> quadLists = new ArrayList<>();
            for (Map.Entry<String, BakedModel> entry : children.entrySet()) {
                quadLists.add(entry.getValue().getQuads(state, side, rand));
            }
            return ConcatenatedListView.of(quadLists);
        }

        @Override
        public boolean useAmbientOcclusion() {
            return isAmbientOcclusion;
        }

        @Override
        public boolean isGui3d() {
            return isGui3d;
        }

        @Override
        public boolean usesBlockLight() {
            return isSideLit;
        }

        @Override
        public boolean isCustomRenderer() {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return particle;
        }

        @Override
        public ItemOverrides getOverrides() {
            return overrides;
        }

        @Override
        public ItemTransforms getTransforms() {
            return transforms;
        }

        public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
            return itemPasses;
        }

        @Nullable
        public BakedModel getPart(String name) {
            return children.get(name);
        }

        public static Builder builder(IGeometryBakingContext owner, TextureAtlasSprite particle,
                                       ItemOverrides overrides, ItemTransforms cameraTransforms) {
            return builder(owner.useAmbientOcclusion(), owner.isGui3d(), owner.useBlockLight(),
                          particle, overrides, cameraTransforms);
        }

        public static Builder builder(boolean isAmbientOcclusion, boolean isGui3d, boolean isSideLit,
                                       TextureAtlasSprite particle, ItemOverrides overrides,
                                       ItemTransforms cameraTransforms) {
            return new Builder(isAmbientOcclusion, isGui3d, isSideLit, particle, overrides, cameraTransforms);
        }

        public static class Builder {
            private final boolean isAmbientOcclusion;
            private final boolean isGui3d;
            private final boolean isSideLit;
            private final List<BakedModel> children = new ArrayList<>();
            private final List<BakedQuad> quads = new ArrayList<>();
            private final ItemOverrides overrides;
            private final ItemTransforms transforms;
            private TextureAtlasSprite particle;
            private RenderTypeGroup lastRenderTypes = RenderTypeGroup.EMPTY;

            private Builder(boolean isAmbientOcclusion, boolean isGui3d, boolean isSideLit,
                           TextureAtlasSprite particle, ItemOverrides overrides, ItemTransforms transforms) {
                this.isAmbientOcclusion = isAmbientOcclusion;
                this.isGui3d = isGui3d;
                this.isSideLit = isSideLit;
                this.particle = particle;
                this.overrides = overrides;
                this.transforms = transforms;
            }

            public void addLayer(BakedModel model) {
                flushQuads(RenderTypeGroup.EMPTY);
                children.add(model);
            }

            private void flushQuads(RenderTypeGroup renderTypes) {
                if (!Objects.equals(renderTypes, lastRenderTypes)) {
                    if (!quads.isEmpty()) {
                        var modelBuilder = IModelBuilder.of(isAmbientOcclusion, isSideLit, isGui3d,
                                transforms, overrides, particle, lastRenderTypes);
                        quads.forEach(modelBuilder::addUnculledFace);
                        children.add(modelBuilder.build());
                        quads.clear();
                    }
                    lastRenderTypes = renderTypes;
                }
            }

            public Builder setParticle(TextureAtlasSprite particleSprite) {
                this.particle = particleSprite;
                return this;
            }

            public Builder addQuads(RenderTypeGroup renderTypes, BakedQuad... quadsToAdd) {
                flushQuads(renderTypes);
                Collections.addAll(quads, quadsToAdd);
                return this;
            }

            public Builder addQuads(RenderTypeGroup renderTypes, Collection<BakedQuad> quadsToAdd) {
                flushQuads(renderTypes);
                quads.addAll(quadsToAdd);
                return this;
            }

            public Builder addQuads(RenderTypeGroup renderTypes, List<BakedQuad> quadsToAdd) {
                flushQuads(renderTypes);
                quads.addAll(quadsToAdd);
                return this;
            }

            public BakedModel build() {
                if (!quads.isEmpty()) {
                    var modelBuilder = IModelBuilder.of(isAmbientOcclusion, isSideLit, isGui3d,
                            transforms, overrides, particle, lastRenderTypes);
                    quads.forEach(modelBuilder::addUnculledFace);
                    children.add(modelBuilder.build());
                }
                var childrenBuilder = ImmutableMap.<String, BakedModel>builder();
                var itemPassesBuilder = ImmutableList.<BakedModel>builder();
                int i = 0;
                for (var model : this.children) {
                    childrenBuilder.put("model_" + (i++), model);
                    itemPassesBuilder.add(model);
                }
                return new Baked(isGui3d, isSideLit, isAmbientOcclusion, particle, transforms,
                                overrides, childrenBuilder.build(), itemPassesBuilder.build());
            }
        }
    }
}

package net.neoforged.neoforge.client.model.geometry;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.RenderTypeGroup;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Standalone context for baking unbaked geometries outside of a model loading context.
 */
public class StandaloneGeometryBakingContext implements IGeometryBakingContext {
    private final String modelName;
    private final Map<String, Material> textures;
    private final boolean useAmbientOcclusion;
    private final boolean isGui3d;
    private final boolean useBlockLight;
    private final ItemTransforms transforms;

    private StandaloneGeometryBakingContext(String modelName, Map<String, Material> textures,
            boolean useAmbientOcclusion, boolean isGui3d, boolean useBlockLight, ItemTransforms transforms) {
        this.modelName = modelName;
        this.textures = textures;
        this.useAmbientOcclusion = useAmbientOcclusion;
        this.isGui3d = isGui3d;
        this.useBlockLight = useBlockLight;
        this.transforms = transforms;
    }

    @Override public String getModelName() { return modelName; }
    @Override public boolean hasMaterial(String name) { return textures.containsKey(name); }
    @Override public Material getMaterial(String name) { return textures.get(name); }
    @Override public boolean useAmbientOcclusion() { return useAmbientOcclusion; }
    @Override public boolean isGui3d() { return isGui3d; }
    @Override public boolean useBlockLight() { return useBlockLight; }
    @Override public ItemTransforms getTransforms() { return transforms; }
    @Override public Transformation getRootTransform() { return Transformation.identity(); }
    @Override public @Nullable ResourceLocation getRenderTypeHint() { return null; }
    @Override public boolean isComponentVisible(String component, boolean fallback) { return fallback; }
    @Override public RenderTypeGroup getRenderType(@Nullable ResourceLocation name) { return RenderTypeGroup.EMPTY; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String modelName = "standalone";
        private final Map<String, Material> textures = new HashMap<>();
        private boolean useAmbientOcclusion = true;
        private boolean isGui3d = false;
        private boolean useBlockLight = false;
        private ItemTransforms transforms = ItemTransforms.NO_TRANSFORMS;

        public Builder withModelName(String name) { this.modelName = name; return this; }
        public Builder withTexture(String name, Material mat) { this.textures.put(name, mat); return this; }
        public Builder withAmbientOcclusion(boolean ao) { this.useAmbientOcclusion = ao; return this; }
        public Builder withGui3d(boolean gui3d) { this.isGui3d = gui3d; return this; }
        public Builder withBlockLight(boolean bl) { this.useBlockLight = bl; return this; }
        public Builder withTransforms(ItemTransforms t) { this.transforms = t; return this; }

        public StandaloneGeometryBakingContext build() {
            return new StandaloneGeometryBakingContext(modelName, textures, useAmbientOcclusion, isGui3d, useBlockLight, transforms);
        }
    }
}

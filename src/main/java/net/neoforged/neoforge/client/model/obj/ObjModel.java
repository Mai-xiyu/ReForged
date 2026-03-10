package net.neoforged.neoforge.client.model.obj;

import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;

import java.util.function.Function;

/**
 * Represents an OBJ model geometry.
 * Wraps Forge's ObjModel and delegates baking.
 */
public class ObjModel implements IUnbakedGeometry<ObjModel> {
    private final net.minecraftforge.client.model.obj.ObjModel forgeModel;

    /**
     * Creates a NeoForge ObjModel wrapping a Forge ObjModel.
     */
    public ObjModel(net.minecraftforge.client.model.obj.ObjModel forgeModel) {
        this.forgeModel = forgeModel;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
        // Create a Forge IGeometryBakingContext adapter from NeoForge context
        net.minecraftforge.client.model.geometry.IGeometryBakingContext forgeCtx = adaptToForge(context);
        return forgeModel.bake(forgeCtx, baker, spriteGetter, modelState, overrides);
    }

    /**
     * Returns the underlying Forge ObjModel.
     */
    public net.minecraftforge.client.model.obj.ObjModel getForgeModel() {
        return forgeModel;
    }

    /**
     * Adapt a NeoForge IGeometryBakingContext to a Forge IGeometryBakingContext.
     */
    private static net.minecraftforge.client.model.geometry.IGeometryBakingContext adaptToForge(IGeometryBakingContext neoCtx) {
        return new net.minecraftforge.client.model.geometry.IGeometryBakingContext() {
            @Override public String getModelName() { return neoCtx.getModelName(); }
            @Override public boolean hasMaterial(String name) { return neoCtx.hasMaterial(name); }
            @Override public Material getMaterial(String name) { return neoCtx.getMaterial(name); }
            @Override public boolean isGui3d() { return neoCtx.isGui3d(); }
            @Override public boolean useBlockLight() { return neoCtx.useBlockLight(); }
            @Override public boolean useAmbientOcclusion() { return neoCtx.useAmbientOcclusion(); }
            @Override public net.minecraft.client.renderer.block.model.ItemTransforms getTransforms() { return neoCtx.getTransforms(); }
            @Override public com.mojang.math.Transformation getRootTransform() { return neoCtx.getRootTransform(); }
            @Override public net.minecraft.resources.ResourceLocation getRenderTypeHint() { return neoCtx.getRenderTypeHint(); }
            @Override public boolean isComponentVisible(String component, boolean fallback) { return neoCtx.isComponentVisible(component, fallback); }
        };
    }
}

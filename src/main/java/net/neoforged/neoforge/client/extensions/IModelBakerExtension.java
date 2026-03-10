package net.neoforged.neoforge.client.extensions;

import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Extension interface for {@link ModelBaker}.
 */
public interface IModelBakerExtension {

    /**
     * Returns the top-level unbaked model at the given location.
     */
    @Nullable
    UnbakedModel getTopLevelModel(ModelResourceLocation location);

    /**
     * Bakes a model at the given resource location with caching.
     */
    @Nullable
    BakedModel bake(ResourceLocation location, ModelState state, Function<Material, TextureAtlasSprite> sprites);

    /**
     * Bakes a model without caching.
     */
    @Nullable
    BakedModel bakeUncached(UnbakedModel model, ModelState state, Function<Material, TextureAtlasSprite> sprites);

    /**
     * Returns the texture getter for this baker.
     */
    Function<Material, TextureAtlasSprite> getModelTextureGetter();
}

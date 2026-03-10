package net.neoforged.neoforge.client.model.geometry;

import java.util.function.Function;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.neoforged.neoforge.client.model.IModelBuilder;

/**
 * Base class for unbaked geometries that only need to provide quads to an {@link IModelBuilder}.
 */
public abstract class SimpleUnbakedGeometry<T extends SimpleUnbakedGeometry<T>> implements IUnbakedGeometry<T> {

    protected abstract void addQuads(IGeometryBakingContext owner, IModelBuilder<?> modelBuilder, ModelBaker baker,
                                     Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform);

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides) {
        TextureAtlasSprite particle = spriteGetter.apply(context.getMaterial("particle"));
        var builder = IModelBuilder.of(
                context.useAmbientOcclusion(), context.useBlockLight(), context.isGui3d(),
                context.getTransforms(), overrides, particle, context.getRenderType(null));
        addQuads(context, builder, baker, spriteGetter, modelState);
        return builder.build();
    }
}

package net.neoforged.neoforge.client.model.geometry;

import com.mojang.math.Transformation;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelState;
import net.neoforged.neoforge.client.model.ExtraFaceData;
import net.neoforged.neoforge.client.model.SimpleModelState;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Helper methods for unbaked model geometry operations.
 */
public final class UnbakedGeometryHelper {
    private UnbakedGeometryHelper() {}

    /**
     * Creates unbaked item elements for a specific layer from a sprite.
     */
    public static List<BlockElement> createUnbakedItemElements(int layerIndex, TextureAtlasSprite sprite, @Nullable ExtraFaceData layerData) {
        return List.of();
    }

    /**
     * Bakes a list of block elements into quads.
     */
    public static List<BakedQuad> bakeElements(List<BlockElement> elements, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState) {
        return List.of();
    }

    /**
     * Compose a root transform into a ModelState.
     */
    public static ModelState composeRootTransformIntoModelState(ModelState modelState, Transformation rootTransform) {
        return new SimpleModelState(
                rootTransform.compose(modelState.getRotation()),
                modelState.isUvLocked()
        );
    }
}

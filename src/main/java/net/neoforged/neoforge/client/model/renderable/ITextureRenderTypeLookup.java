package net.neoforged.neoforge.client.model.renderable;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * A function interface to look up render types for textures.
 */
@FunctionalInterface
public interface ITextureRenderTypeLookup {
    @Nullable
    RenderType get(ResourceLocation texture);
}

package net.createmod.ponder.mixin.client.accessor;

import net.minecraft.client.renderer.texture.TextureManager;

/**
 * Accessor interface matching Ponder's ItemRendererAccessor mixin.
 * Implemented via {@link org.xiyu.reforged.mixin.ItemRendererAccessorMixin}.
 */
public interface ItemRendererAccessor {
    TextureManager catnip$getTextureManager();
}

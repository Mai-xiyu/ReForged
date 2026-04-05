package org.xiyu.reforged.mixin;

import net.createmod.ponder.mixin.client.accessor.ItemRendererAccessor;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Makes ItemRenderer implement Ponder's ItemRendererAccessor interface,
 * exposing the textureManager field.
 */
@Mixin(value = ItemRenderer.class, remap = false)
public abstract class ItemRendererAccessorMixin implements ItemRendererAccessor {

    @Shadow @Final
    public TextureManager textureManager;

    @Override
    public TextureManager catnip$getTextureManager() {
        return this.textureManager;
    }
}

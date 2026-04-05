package org.xiyu.reforged.mixin;

import net.createmod.ponder.mixin.client.accessor.GameRendererAccessor;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Makes GameRenderer implement Ponder's GameRendererAccessor,
 * invoking the private getFov method.
 */
@Mixin(value = GameRenderer.class, remap = false)
public abstract class GameRendererAccessorMixin implements GameRendererAccessor {

    @Shadow
    protected abstract double getFov(Camera camera, float partialTicks, boolean useFovSetting);

    @Override
    public double catnip$callGetFov(Camera camera, float partialTicks, boolean useFovSetting) {
        return this.getFov(camera, partialTicks, useFovSetting);
    }
}

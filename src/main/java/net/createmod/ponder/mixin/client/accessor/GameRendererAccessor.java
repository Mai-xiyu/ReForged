package net.createmod.ponder.mixin.client.accessor;

import net.minecraft.client.Camera;

/**
 * Accessor interface matching Ponder's GameRendererAccessor mixin.
 * Implemented via {@link org.xiyu.reforged.mixin.GameRendererAccessorMixin}.
 */
public interface GameRendererAccessor {
    double catnip$callGetFov(Camera camera, float partialTicks, boolean useFovSetting);
}

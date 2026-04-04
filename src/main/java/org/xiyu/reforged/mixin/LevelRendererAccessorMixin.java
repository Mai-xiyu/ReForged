package org.xiyu.reforged.mixin;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererAccessorMixin {
    @Shadow private Frustum cullingFrustum;
    @Shadow private Frustum capturedFrustum;
    @Shadow(remap = false) private int ticks;

    public Frustum create$getCullingFrustum() {
        return this.cullingFrustum;
    }

    public Frustum create$getCapturedFrustum() {
        return this.capturedFrustum;
    }

    /** Flywheel's LevelRendererAccessor.flywheel$getTicks() */
    public int flywheel$getTicks() {
        return this.ticks;
    }
}

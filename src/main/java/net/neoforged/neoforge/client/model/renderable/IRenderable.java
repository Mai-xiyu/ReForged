package net.neoforged.neoforge.client.model.renderable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Interface for objects that can render themselves.
 */
public interface IRenderable<T> {
    void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick, T context);
}

package org.xiyu.reforged.mixin;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Fixes an off-by-one bug in Forge 51.0.33's QuadBakingVertexConsumer.
 *
 * <p>In 1.21, {@code addVertex()} calls {@code endVertex()} first to "finish"
 * the previous vertex. However, {@code endVertex()} unconditionally increments
 * {@code vertexIndex} when it's not equal to 4. This means:</p>
 * <ul>
 *   <li>The first {@code addVertex()} increments vertexIndex from 0 → 1 before writing any data</li>
 *   <li>Vertex data for vertex 0 is written at offset 1*STRIDE instead of 0*STRIDE</li>
 *   <li>By the 4th {@code addVertex()}, vertexIndex becomes 4 and offset = 4*8 = 32,
 *       causing {@code ArrayIndexOutOfBoundsException: Index 32 out of bounds for length 32}</li>
 * </ul>
 *
 * <p>Fix: Remove the {@code endVertex()} call from {@code addVertex()}, and instead
 * advance the vertex index in {@code setNormal()} (the last attribute per vertex),
 * which triggers the bake when all 4 vertices are complete.</p>
 */
@Mixin(value = QuadBakingVertexConsumer.class, remap = false)
public abstract class QuadBakingVertexConsumerMixin {

    @Shadow
    int vertexIndex;

    @Shadow
    private int[] quadData;

    @Shadow
    abstract void endVertex();

    /**
     * Suppress the endVertex() call inside addVertex() — the call that causes
     * premature vertexIndex increment before position data is written.
     */
    @Redirect(method = "addVertex(FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
              at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/model/pipeline/QuadBakingVertexConsumer;endVertex()V"))
    private void reforged$suppressEndVertexInAddVertex(QuadBakingVertexConsumer self) {
        // No-op: don't call endVertex here. Vertex advancement is handled in setNormal.
    }

    /**
     * After setNormal writes the normal data (the last vertex attribute),
     * advance the vertex index. When all 4 vertices are complete, trigger bake.
     */
    @Inject(method = "setNormal(FFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;",
            at = @At("TAIL"))
    private void reforged$advanceVertexAfterNormal(float x, float y, float z,
                                                    CallbackInfoReturnable<VertexConsumer> cir) {
        this.vertexIndex++;
        if (this.vertexIndex >= 4) {
            this.endVertex();
        }
    }
}

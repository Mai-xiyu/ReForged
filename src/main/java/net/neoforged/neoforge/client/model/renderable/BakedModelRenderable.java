package net.neoforged.neoforge.client.model.renderable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

/**
 * A renderable baked model — wraps a BakedModel and renders it directly.
 */
public class BakedModelRenderable implements IRenderable<Void> {
    private final BakedModel model;

    public BakedModelRenderable(BakedModel model) {
        this.model = model;
    }

    public static BakedModelRenderable of(BakedModel model) {
        return new BakedModelRenderable(model);
    }

    public BakedModel getModel() {
        return model;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, float partialTick, Void context) {
        var buffer = bufferSource.getBuffer(RenderType.solid());
        var random = RandomSource.create();
        // Render face-culled quads
        for (Direction dir : Direction.values()) {
            random.setSeed(42L);
            for (BakedQuad quad : model.getQuads(null, dir, random)) {
                buffer.putBulkData(poseStack.last(), quad, 1.0f, 1.0f, 1.0f, 1.0f, packedLight, packedOverlay);
            }
        }
        // Render non-culled quads (null direction)
        random.setSeed(42L);
        for (BakedQuad quad : model.getQuads(null, null, random)) {
            buffer.putBulkData(poseStack.last(), quad, 1.0f, 1.0f, 1.0f, 1.0f, packedLight, packedOverlay);
        }
    }
}

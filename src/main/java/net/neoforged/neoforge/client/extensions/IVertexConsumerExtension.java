package net.neoforged.neoforge.client.extensions;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.block.model.BakedQuad;

/**
 * Extension interface for {@link VertexConsumer}.
 * Provides NeoForge-specific vertex pipeline hooks.
 */
public interface IVertexConsumerExtension {

    /**
     * Consumes raw data for a miscellaneous vertex format element.
     * Default is a no-op returning self.
     */
    default VertexConsumer misc(VertexFormatElement element, int... rawData) {
        return (VertexConsumer) this;
    }

    /**
     * Writes bulk vertex data from a baked quad with the given parameters.
     */
    default void putBulkData(PoseStack.Pose pose, BakedQuad bakedQuad,
                             float red, float green, float blue, float alpha,
                             int packedLight, int packedOverlay, boolean readExistingColor) {
        ((VertexConsumer) this).putBulkData(pose, bakedQuad, red, green, blue, alpha, packedLight, packedOverlay, readExistingColor);
    }
}

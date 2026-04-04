package org.xiyu.reforged.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

/**
 * Injects Flywheel's ModelPartAccessor methods:
 * flywheel$children() and flywheel$compile()
 * Used by Flywheel to traverse model part hierarchy and compile vertex data.
 */
@Mixin(value = ModelPart.class, remap = false)
public abstract class FlywheelModelPartAccessorMixin {

    @Shadow(remap = false) @Final
    public Map<String, ModelPart> children;

    @Shadow(remap = false)
    private void compile(PoseStack.Pose pose, VertexConsumer vertexConsumer,
                         int packedLight, int packedOverlay, int color) {}

    public Map<String, ModelPart> flywheel$children() {
        return this.children;
    }

    public void flywheel$compile(PoseStack.Pose pose, VertexConsumer vertexConsumer,
                                  int packedLight, int packedOverlay, int color) {
        this.compile(pose, vertexConsumer, packedLight, packedOverlay, color);
    }
}

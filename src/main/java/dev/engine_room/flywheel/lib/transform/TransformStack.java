package dev.engine_room.flywheel.lib.transform;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.impl.extension.PoseStackExtension;

public interface TransformStack<Self extends TransformStack<Self>> extends Transform<Self> {
    static PoseTransformStack of(PoseStack poseStack) {
        return ((PoseStackExtension) poseStack).flywheel$transformStack();
    }

    Self pushPose();

    Self popPose();
}

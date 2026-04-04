package dev.engine_room.flywheel.lib.transform;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;

public final class PoseTransformStack implements TransformStack<PoseTransformStack> {
    private final PoseStack stack;

    public PoseTransformStack(PoseStack stack) {
        this.stack = stack;
    }

    @Override
    public PoseTransformStack pushPose() {
        stack.pushPose();
        return this;
    }

    @Override
    public PoseTransformStack popPose() {
        stack.popPose();
        return this;
    }

    @Override
    public PoseTransformStack mulPose(Matrix4fc pose) {
        stack.last().pose().mul(pose);
        return this;
    }

    @Override
    public PoseTransformStack mulNormal(Matrix3fc normal) {
        stack.last().normal().mul(normal);
        return this;
    }

    @Override
    public PoseTransformStack rotateAround(Quaternionfc q, float x, float y, float z) {
        PoseStack.Pose pose = stack.last();
        pose.pose().rotateAround(q, x, y, z);
        pose.normal().rotate(q);
        return this;
    }

    @Override
    public PoseTransformStack translate(float x, float y, float z) {
        stack.translate(x, y, z);
        return this;
    }

    @Override
    public PoseTransformStack rotate(Quaternionfc q) {
        PoseStack.Pose pose = stack.last();
        pose.pose().rotate(q);
        pose.normal().rotate(q);
        return this;
    }

    @Override
    public PoseTransformStack scale(float x, float y, float z) {
        stack.scale(x, y, z);
        return this;
    }

    public PoseStack unwrap() {
        return stack;
    }
}

package org.xiyu.reforged.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.impl.extension.PoseStackExtension;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Deque;

@Mixin(PoseStack.class)
public abstract class PoseStackFlywheelMixin implements PoseStackExtension {
    @Shadow(remap = false) @Final
    private Deque<PoseStack.Pose> poseStack;

    @Unique
    private PoseTransformStack reforged$transformStack;

    @Override
    public PoseTransformStack flywheel$transformStack() {
        if (reforged$transformStack == null) {
            reforged$transformStack = new PoseTransformStack((PoseStack) (Object) this);
        }
        return reforged$transformStack;
    }

    /** Flywheel's PoseStackAccessor.flywheel$getPoseStack() */
    public Deque<PoseStack.Pose> flywheel$getPoseStack() {
        return this.poseStack;
    }
}

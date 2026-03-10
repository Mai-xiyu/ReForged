package net.neoforged.neoforge.client.extensions;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;

/**
 * Extension interface for {@link PoseStack} to support NeoForge convenience methods.
 */
public interface IPoseStackExtension {

    /**
     * Pushes a pose and applies the given transformation (translation, rotations, scale).
     */
    default void pushTransformation(Transformation transformation) {
        PoseStack self = (PoseStack) this;
        self.pushPose();
        org.joml.Vector3f trans = transformation.getTranslation();
        self.translate(trans.x(), trans.y(), trans.z());
        self.mulPose(transformation.getLeftRotation());
        org.joml.Vector3f scale = transformation.getScale();
        self.scale(scale.x(), scale.y(), scale.z());
        self.mulPose(transformation.getRightRotation());
    }
}

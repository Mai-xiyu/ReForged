package dev.engine_room.flywheel.lib.transform;

import com.mojang.math.Axis;
import net.minecraft.core.Direction;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

public interface Affine<Self extends Affine<Self>> extends Translate<Self>, Rotate<Self>, Scale<Self> {
    default Self rotateAround(Quaternionfc q, float x, float y, float z) {
        return translate(x, y, z).rotate(q).translateBack(x, y, z);
    }

    default Self rotateAround(Quaternionfc q, Vector3fc pivot) {
        return rotateAround(q, pivot.x(), pivot.y(), pivot.z());
    }

    default Self rotateCentered(Quaternionfc q) {
        return rotateAround(q, 0.5f, 0.5f, 0.5f);
    }

    default Self rotateCentered(float radians, float x, float y, float z) {
        if (radians == 0) return self();
        return rotateCentered(new Quaternionf().setAngleAxis(radians, x, y, z));
    }

    default Self rotateCentered(float radians, Axis axis) {
        if (radians == 0) return self();
        return rotateCentered(axis.rotation(radians));
    }

    default Self rotateCentered(float radians, Vector3fc axis) {
        return rotateCentered(radians, axis.x(), axis.y(), axis.z());
    }

    default Self rotateCentered(float radians, Direction.Axis axis) {
        return rotateCentered(radians, Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE));
    }

    default Self rotateCentered(float radians, Direction direction) {
        return rotateCentered(radians, (float) direction.getStepX(), (float) direction.getStepY(), (float) direction.getStepZ());
    }

    default Self rotateCenteredDegrees(float degrees, float x, float y, float z) {
        return rotateCentered(0.017453292f * degrees, x, y, z);
    }

    default Self rotateCenteredDegrees(float degrees, Axis axis) {
        return rotateCentered(0.017453292f * degrees, axis);
    }

    default Self rotateCenteredDegrees(float degrees, Vector3fc axis) {
        return rotateCentered(0.017453292f * degrees, axis);
    }

    default Self rotateCenteredDegrees(float degrees, Direction direction) {
        return rotateCentered(0.017453292f * degrees, direction);
    }

    default Self rotateCenteredDegrees(float degrees, Direction.Axis axis) {
        return rotateCentered(0.017453292f * degrees, axis);
    }

    default Self rotateXCentered(float radians) {
        return rotateCentered(radians, Axis.XP);
    }

    default Self rotateYCentered(float radians) {
        return rotateCentered(radians, Axis.YP);
    }

    default Self rotateZCentered(float radians) {
        return rotateCentered(radians, Axis.ZP);
    }

    default Self rotateXCenteredDegrees(float degrees) {
        return rotateXCentered(0.017453292f * degrees);
    }

    default Self rotateYCenteredDegrees(float degrees) {
        return rotateYCentered(0.017453292f * degrees);
    }

    default Self rotateZCenteredDegrees(float degrees) {
        return rotateZCentered(0.017453292f * degrees);
    }
}

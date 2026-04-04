package dev.engine_room.flywheel.lib.transform;

import com.mojang.math.Axis;
import net.minecraft.core.Direction;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

public interface Rotate<Self extends Rotate<Self>> {
    Self rotate(Quaternionfc rotation);

    default Self rotate(AxisAngle4f axisAngle) {
        return rotate(new Quaternionf(axisAngle));
    }

    default Self rotate(float radians, float x, float y, float z) {
        if (radians == 0) return self();
        return rotate(new Quaternionf().setAngleAxis(radians, x, y, z));
    }

    default Self rotate(float radians, Axis axis) {
        if (radians == 0) return self();
        return rotate(axis.rotation(radians));
    }

    default Self rotate(float radians, Vector3fc axis) {
        return rotate(radians, axis.x(), axis.y(), axis.z());
    }

    default Self rotate(float radians, Direction direction) {
        return rotate(radians, (float) direction.getStepX(), (float) direction.getStepY(), (float) direction.getStepZ());
    }

    default Self rotate(float radians, Direction.Axis axis) {
        return rotate(radians, Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE));
    }

    default Self rotateDegrees(float degrees, float x, float y, float z) {
        return rotate(0.017453292f * degrees, x, y, z);
    }

    default Self rotateDegrees(float degrees, Axis axis) {
        return rotate(0.017453292f * degrees, axis);
    }

    default Self rotateDegrees(float degrees, Vector3fc axis) {
        return rotate(0.017453292f * degrees, axis);
    }

    default Self rotateDegrees(float degrees, Direction direction) {
        return rotate(0.017453292f * degrees, direction);
    }

    default Self rotateDegrees(float degrees, Direction.Axis axis) {
        return rotate(0.017453292f * degrees, axis);
    }

    default Self rotateX(float radians) {
        return rotate(radians, Axis.XP);
    }

    default Self rotateY(float radians) {
        return rotate(radians, Axis.YP);
    }

    default Self rotateZ(float radians) {
        return rotate(radians, Axis.ZP);
    }

    default Self rotateXDegrees(float degrees) {
        return rotateX(0.017453292f * degrees);
    }

    default Self rotateYDegrees(float degrees) {
        return rotateY(0.017453292f * degrees);
    }

    default Self rotateZDegrees(float degrees) {
        return rotateZ(0.017453292f * degrees);
    }

    default Self rotateToFace(Direction direction) {
        return switch (direction) {
            case DOWN -> rotateXDegrees(-90f);
            case UP -> rotateXDegrees(90f);
            case NORTH -> self();
            case SOUTH -> rotateYDegrees(180f);
            case WEST -> rotateYDegrees(90f);
            case EAST -> rotateYDegrees(270f);
        };
    }

    default Self rotateTo(float fromX, float fromY, float fromZ, float toX, float toY, float toZ) {
        return rotate(new Quaternionf().rotationTo(fromX, fromY, fromZ, toX, toY, toZ));
    }

    default Self rotateTo(Vector3fc from, Vector3fc to) {
        return rotateTo(from.x(), from.y(), from.z(), to.x(), to.y(), to.z());
    }

    default Self rotateTo(Direction from, Direction to) {
        return rotateTo(
                (float) from.getStepX(), (float) from.getStepY(), (float) from.getStepZ(),
                (float) to.getStepX(), (float) to.getStepY(), (float) to.getStepZ()
        );
    }

    @SuppressWarnings("unchecked")
    default Self self() {
        return (Self) this;
    }
}

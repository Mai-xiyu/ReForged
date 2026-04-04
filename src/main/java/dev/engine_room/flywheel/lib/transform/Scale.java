package dev.engine_room.flywheel.lib.transform;

import org.joml.Vector3fc;

public interface Scale<Self extends Scale<Self>> {
    Self scale(float x, float y, float z);

    default Self scale(float s) {
        return scale(s, s, s);
    }

    default Self scaleX(float x) {
        return scale(x, 1, 1);
    }

    default Self scaleY(float y) {
        return scale(1, y, 1);
    }

    default Self scaleZ(float z) {
        return scale(1, 1, z);
    }

    default Self scale(Vector3fc vec) {
        return scale(vec.x(), vec.y(), vec.z());
    }
}

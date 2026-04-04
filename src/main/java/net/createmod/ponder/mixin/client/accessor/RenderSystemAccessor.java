package net.createmod.ponder.mixin.client.accessor;

import org.joml.Vector3f;
import java.lang.reflect.Field;

/**
 * Replacement for Ponder's @Accessor mixin on RenderSystem.
 * Since the original Mixin isn't applied in Forge, we use cached reflection.
 */
public interface RenderSystemAccessor {

    Field SHADER_LIGHT_DIRECTIONS = findField();

    static Field findField() {
        try {
            Field f = com.mojang.blaze3d.systems.RenderSystem.class.getDeclaredField("shaderLightDirections");
            f.setAccessible(true);
            return f;
        } catch (Exception e) {
            throw new RuntimeException("[ReForged] Failed to find RenderSystem.shaderLightDirections", e);
        }
    }

    static Vector3f[] catnip$getShaderLightDirections() {
        try {
            return (Vector3f[]) SHADER_LIGHT_DIRECTIONS.get(null);
        } catch (Exception e) {
            throw new RuntimeException("[ReForged] Failed to access RenderSystem.shaderLightDirections", e);
        }
    }
}

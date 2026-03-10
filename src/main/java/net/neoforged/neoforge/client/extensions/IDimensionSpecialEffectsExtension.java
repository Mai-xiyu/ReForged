package net.neoforged.neoforge.client.extensions;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Extension interface for {@link net.minecraft.client.renderer.DimensionSpecialEffects}.
 * Provides hooks for custom dimension rendering.
 */
public interface IDimensionSpecialEffectsExtension {

    /**
     * Custom cloud rendering. Return true to cancel vanilla cloud rendering.
     */
    default boolean renderClouds(ClientLevel level, int ticks, float partialTick,
                                 com.mojang.blaze3d.vertex.PoseStack poseStack,
                                 double camX, double camY, double camZ,
                                 Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
        return false;
    }

    /**
     * Custom sky rendering. Return true to cancel vanilla sky rendering.
     */
    default boolean renderSky(ClientLevel level, int ticks, float partialTick,
                              Matrix4f modelViewMatrix, Camera camera,
                              Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        return false;
    }

    /**
     * Custom snow and rain rendering. Return true to cancel vanilla rendering.
     */
    default boolean renderSnowAndRain(ClientLevel level, int ticks, float partialTick,
                                      net.minecraft.client.renderer.LightTexture lightTexture,
                                      double camX, double camY, double camZ) {
        return false;
    }

    /**
     * Custom rain tick. Return true to cancel vanilla rain tick.
     */
    default boolean tickRain(ClientLevel level, int ticks, Camera camera) {
        return false;
    }

    /**
     * Adjust lightmap colors per pixel.
     */
    default void adjustLightmapColors(ClientLevel level, float partialTicks, float skyDarken,
                                      float blockLightRedFlicker, float skyLight,
                                      int pixelX, int pixelY, Vector3f colors) {
    }
}

package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.shaders.FogShape;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.level.material.FogType;
import net.neoforged.neoforge.event.entity.EntityEvent;

/**
 * Events related to viewport rendering (camera, fog, etc.).
 */
public abstract class ViewportEvent extends net.neoforged.bus.api.Event {
    private final GameRenderer renderer;
    private final Camera camera;
    private final double partialTick;

    protected ViewportEvent(GameRenderer renderer, Camera camera, double partialTick) {
        this.renderer = renderer;
        this.camera = camera;
        this.partialTick = partialTick;
    }

    public GameRenderer getRenderer() { return renderer; }
    public Camera getCamera() { return camera; }
    public double getPartialTick() { return partialTick; }

    /** Fired to allow modification of the field of view. */
    public static class ComputeFov extends ViewportEvent {
        private double fov;
        private final boolean usedConfiguredFov;

        public ComputeFov(GameRenderer renderer, Camera camera, double partialTick, double fov, boolean usedConfiguredFov) {
            super(renderer, camera, partialTick);
            this.fov = fov;
            this.usedConfiguredFov = usedConfiguredFov;
        }

        public double getFOV() { return fov; }
        public void setFOV(double fov) { this.fov = fov; }
        public boolean usedConfiguredFov() { return usedConfiguredFov; }
    }

    /** Fired to allow modification of camera angles (yaw, pitch, roll). */
    public static class ComputeCameraAngles extends ViewportEvent {
        private float yaw;
        private float pitch;
        private float roll;

        public ComputeCameraAngles(GameRenderer renderer, Camera camera, double partialTick,
                float yaw, float pitch, float roll) {
            super(renderer, camera, partialTick);
            this.yaw = yaw;
            this.pitch = pitch;
            this.roll = roll;
        }

        /** Wrapper constructor for EventBusAdapter bridging. */
        public ComputeCameraAngles(net.minecraftforge.client.event.ViewportEvent.ComputeCameraAngles forge) {
            this(forge.getRenderer(), forge.getCamera(), forge.getPartialTick(),
                    forge.getYaw(), forge.getPitch(), forge.getRoll());
        }

        public float getYaw() { return yaw; }
        public void setYaw(float yaw) { this.yaw = yaw; }
        public float getPitch() { return pitch; }
        public void setPitch(float pitch) { this.pitch = pitch; }
        public float getRoll() { return roll; }
        public void setRoll(float roll) { this.roll = roll; }
    }

    /** Fired to allow modification of fog rendering. */
    public static class RenderFog extends ViewportEvent implements net.neoforged.bus.api.ICancellableEvent {
        private final FogRenderer.FogMode mode;
        private final FogType type;
        private float farPlaneDistance;
        private float nearPlaneDistance;
        private FogShape fogShape;

        public RenderFog(FogRenderer.FogMode mode, FogType type,
                Camera camera, float partialTick, float nearPlaneDistance, float farPlaneDistance,
                FogShape fogShape, GameRenderer renderer) {
            super(renderer, camera, partialTick);
            this.mode = mode;
            this.type = type;
            this.farPlaneDistance = farPlaneDistance;
            this.nearPlaneDistance = nearPlaneDistance;
            this.fogShape = fogShape;
        }

        /** Wrapper constructor for EventBusAdapter bridging. */
        public RenderFog(net.minecraftforge.client.event.ViewportEvent.RenderFog forge) {
            this(forge.getMode(), forge.getType(), forge.getCamera(), (float) forge.getPartialTick(),
                    forge.getNearPlaneDistance(), forge.getFarPlaneDistance(), forge.getFogShape(),
                    forge.getRenderer());
        }

        public FogRenderer.FogMode getMode() { return mode; }
        public FogType getType() { return type; }
        public float getFarPlaneDistance() { return farPlaneDistance; }
        public void setFarPlaneDistance(float farPlaneDistance) { this.farPlaneDistance = farPlaneDistance; }
        public float getNearPlaneDistance() { return nearPlaneDistance; }
        public void setNearPlaneDistance(float nearPlaneDistance) { this.nearPlaneDistance = nearPlaneDistance; }
        public FogShape getFogShape() { return fogShape; }
        public void setFogShape(FogShape fogShape) { this.fogShape = fogShape; }
        public void scaleFarPlaneDistance(float factor) { this.farPlaneDistance *= factor; }
        public void scaleNearPlaneDistance(float factor) { this.nearPlaneDistance *= factor; }
    }
}

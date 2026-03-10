package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * Fired at various stages during level rendering, allowing injection of custom rendering.
 */
public class RenderLevelStageEvent extends net.neoforged.bus.api.Event {
    private final Stage stage;
    private final LevelRenderer levelRenderer;
    @Nullable
    private final PoseStack poseStack;
    private final Matrix4f modelViewMatrix;
    private final Matrix4f projectionMatrix;
    private final int renderTick;
    private final DeltaTracker partialTick;
    private final Camera camera;
    private final Frustum frustum;

    public RenderLevelStageEvent(Stage stage, LevelRenderer levelRenderer, @Nullable PoseStack poseStack,
            Matrix4f modelViewMatrix, Matrix4f projectionMatrix, int renderTick,
            DeltaTracker partialTick, Camera camera, Frustum frustum) {
        this.stage = stage;
        this.levelRenderer = levelRenderer;
        this.poseStack = poseStack;
        this.modelViewMatrix = modelViewMatrix;
        this.projectionMatrix = projectionMatrix;
        this.renderTick = renderTick;
        this.partialTick = partialTick;
        this.camera = camera;
        this.frustum = frustum;
    }

    public Stage getStage() { return stage; }
    public LevelRenderer getLevelRenderer() { return levelRenderer; }
    @Nullable
    public PoseStack getPoseStack() { return poseStack; }
    public Matrix4f getModelViewMatrix() { return modelViewMatrix; }
    public Matrix4f getProjectionMatrix() { return projectionMatrix; }
    public int getRenderTick() { return renderTick; }
    public DeltaTracker getPartialTick() { return partialTick; }
    public Camera getCamera() { return camera; }
    public Frustum getFrustum() { return frustum; }

    /**
     * Enum of rendering stages at which this event can be fired.
     */
    public static class Stage {
        public static final Stage AFTER_SKY = new Stage();
        public static final Stage AFTER_SOLID_BLOCKS = new Stage();
        public static final Stage AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS = new Stage();
        public static final Stage AFTER_CUTOUT_BLOCKS = new Stage();
        public static final Stage AFTER_ENTITIES = new Stage();
        public static final Stage AFTER_BLOCK_ENTITIES = new Stage();
        public static final Stage AFTER_TRANSLUCENT_BLOCKS = new Stage();
        public static final Stage AFTER_TRIPWIRE_BLOCKS = new Stage();
        public static final Stage AFTER_PARTICLES = new Stage();
        public static final Stage AFTER_WEATHER = new Stage();
        public static final Stage AFTER_LEVEL = new Stage();

        private Stage() {}
    }
}

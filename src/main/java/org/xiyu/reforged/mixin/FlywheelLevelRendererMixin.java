package org.xiyu.reforged.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.xiyu.reforged.bridge.FlywheelRenderBridge;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.SortedSet;

/**
 * Replicates Flywheel's LevelRendererMixin injection points to enable Flywheel's
 * GPU-accelerated instanced rendering of kinetic blocks (shafts, gears, etc.).
 *
 * <p>Flywheel's own mixins cannot be applied because Flywheel is loaded by NeoModClassLoader
 * (a child classloader), which is invisible to the Mixin framework. This mixin provides
 * the same hooks, calling Flywheel APIs through {@link FlywheelRenderBridge} via reflection.</p>
 */
@Mixin(value = LevelRenderer.class, priority = 990, remap = false)
public abstract class FlywheelLevelRendererMixin {

    @Shadow(remap = false) private ClientLevel level;
    @Shadow(remap = false) @Final private RenderBuffers renderBuffers;
    @Shadow(remap = false) @Final private Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress;

    /**
     * Hook: start of rendering — create Flywheel's RenderContext and signal frame start.
     * Original: flywheel$beginRender in Flywheel's LevelRendererMixin.
     * Injection point: after LevelLightEngine.runLightUpdates() call in renderLevel().
     *
     * <p>Note: In Forge 1.21-51.0.33, the return value of runLightUpdates() is discarded
     * (pop instruction) rather than stored, so INVOKE_ASSIGN does not match.
     * Using INVOKE + AFTER instead.</p>
     */
    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/lighting/LevelLightEngine;runLightUpdates()I",
                    shift = At.Shift.AFTER
            ),
            remap = false
    )
    private void reforged$flywheelBeginRender(DeltaTracker deltaTracker, boolean renderBlockOutline,
                                               Camera camera, GameRenderer gameRenderer,
                                               LightTexture lightTexture, Matrix4f modelMatrix,
                                               Matrix4f projectionMatrix, CallbackInfo ci) {
        try {
            float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
            FlywheelRenderBridge.beginRender(
                    (LevelRenderer) (Object) this,
                    this.level,
                    this.renderBuffers,
                    modelMatrix, projectionMatrix,
                    camera, partialTick);
        } catch (Throwable ignored) {
        }
    }

    /**
     * Hook: before block entity rendering — Flywheel submits its instanced draw calls here.
     * Original: flywheel$beforeBlockEntities in Flywheel's LevelRendererMixin.
     * Injection point: before ProfilerFiller.popPush("blockentities") in renderLevel().
     */
    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE_STRING",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
                    args = "ldc=blockentities"
            ),
            remap = false
    )
    private void reforged$flywheelBeforeBlockEntities(DeltaTracker deltaTracker, boolean renderBlockOutline,
                                                       Camera camera, GameRenderer gameRenderer,
                                                       LightTexture lightTexture, Matrix4f modelMatrix,
                                                       Matrix4f projectionMatrix, CallbackInfo ci) {
        try {
            FlywheelRenderBridge.beforeBlockEntities(this.level);
        } catch (Throwable ignored) {
        }
    }

    /**
     * Hook: before crumbling overlay rendering.
     * Original: flywheel$beforeRenderCrumbling in Flywheel's LevelRendererMixin.
     * Injection point: before ProfilerFiller.popPush("destroyProgress") in renderLevel().
     */
    @Inject(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE_STRING",
                    target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
                    args = "ldc=destroyProgress"
            ),
            remap = false
    )
    private void reforged$flywheelBeforeRenderCrumbling(DeltaTracker deltaTracker, boolean renderBlockOutline,
                                                         Camera camera, GameRenderer gameRenderer,
                                                         LightTexture lightTexture, Matrix4f modelMatrix,
                                                         Matrix4f projectionMatrix, CallbackInfo ci) {
        try {
            FlywheelRenderBridge.beforeRenderCrumbling(this.level, this.destructionProgress);
        } catch (Throwable ignored) {
        }
    }

    /**
     * Hook: end of rendering — cleanup per-frame state.
     * Original: flywheel$endRender in Flywheel's LevelRendererMixin.
     * Injection point: RETURN of renderLevel().
     */
    @Inject(method = "renderLevel", at = @At("RETURN"), remap = false)
    private void reforged$flywheelEndRender(DeltaTracker deltaTracker, boolean renderBlockOutline,
                                             Camera camera, GameRenderer gameRenderer,
                                             LightTexture lightTexture, Matrix4f modelMatrix,
                                             Matrix4f projectionMatrix, CallbackInfo ci) {
        FlywheelRenderBridge.endRender();
    }

    /**
     * Hook: level renderer reload — fire ReloadLevelRendererEvent.
     * Original: flywheel$reload in Flywheel's LevelRendererMixin.
     * Injection point: TAIL of allChanged() (after vanilla reload logic completes).
     */
    @Inject(method = "allChanged", at = @At("TAIL"), remap = false)
    private void reforged$flywheelReload(CallbackInfo ci) {
        try {
            if (this.level != null) {
                FlywheelRenderBridge.fireReloadLevelRendererEvent(this.level);
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * Hook: entity rendering decision — skip vanilla rendering for entities managed by Flywheel.
     * Original: flywheel$decideNotToRenderEntity in Flywheel's LevelRendererMixin.
     * Injection point: HEAD of renderEntity(...), cancellable.
     */
    @Inject(
            method = "renderEntity",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void reforged$flywheelSkipEntity(Entity entity, double camX, double camY, double camZ,
                                              float partialTick, PoseStack poseStack,
                                              net.minecraft.client.renderer.MultiBufferSource bufferSource,
                                              CallbackInfo ci) {
        try {
            if (FlywheelRenderBridge.shouldSkipVanillaRender(entity)) {
                ci.cancel();
            }
        } catch (Throwable ignored) {
        }
    }
}

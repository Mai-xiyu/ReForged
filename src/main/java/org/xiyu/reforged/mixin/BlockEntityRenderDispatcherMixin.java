package org.xiyu.reforged.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prevents block entity render errors (e.g. NeoForge mod config not loaded)
 * from crashing the game. Uses two injection points:
 * <ol>
 *   <li>{@code render()} HEAD — captures PoseStack reference + depth into a ThreadLocal</li>
 *   <li>{@code tryRender()} Runnable.run() — catches exceptions and restores PoseStack depth</li>
 * </ol>
 * This prevents both the original crash AND the secondary "Pose stack not empty" crash.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {

    private static final Logger REFORGED_LOGGER = LogUtils.getLogger();
    private static final Set<String> LOGGED_ERRORS = ConcurrentHashMap.newKeySet();
    private static final int MAX_LOGGED = 32;

    /** ThreadLocal to pass PoseStack context from render() into tryRender()'s redirect. */
    private static final ThreadLocal<PoseStack> CURRENT_POSE_STACK = new ThreadLocal<>();
    private static final ThreadLocal<Integer> POSE_DEPTH_BEFORE = ThreadLocal.withInitial(() -> 0);

    /**
     * Capture the PoseStack and its depth at the start of render().
     * render() pushes the PoseStack and calls tryRender() which calls setupAndRender().
     */
    @Inject(
            method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V",
            at = @At("HEAD"),
            remap = false
    )
    private void reforged$captureRenderContext(BlockEntity blockEntity, float partialTick,
                                               PoseStack poseStack, MultiBufferSource bufferSource, CallbackInfo ci) {
        CURRENT_POSE_STACK.set(poseStack);
        POSE_DEPTH_BEFORE.set(getPoseStackDepth(poseStack));
    }

    /**
     * Redirect the Runnable.run() call inside tryRender() to catch exceptions
     * and restore PoseStack depth, preventing "Pose stack not empty" crashes.
     */
    @Redirect(
            method = "tryRender",
            at = @At(value = "INVOKE", target = "Ljava/lang/Runnable;run()V"),
            remap = false
    )
    private static void reforged$suppressRenderCrash(Runnable renderAction) {
        try {
            renderAction.run();
        } catch (Throwable t) {
            // Restore PoseStack to pre-render depth
            PoseStack poseStack = CURRENT_POSE_STACK.get();
            if (poseStack != null) {
                int target = POSE_DEPTH_BEFORE.get();
                int current = getPoseStackDepth(poseStack);
                while (current > target) {
                    try { poseStack.popPose(); } catch (Throwable ignored) { break; }
                    current--;
                }
            }

            String key = t.getClass().getName() + ":" +
                    (t.getMessage() != null ? t.getMessage().substring(0, Math.min(60, t.getMessage().length())) : "");
            if (LOGGED_ERRORS.size() < MAX_LOGGED && LOGGED_ERRORS.add(key)) {
                // Log root cause with full stack trace for diagnosis
                Throwable root = t;
                while (root.getCause() != null) root = root.getCause();
                REFORGED_LOGGER.error("[ReForged] Suppressed block entity render crash ({}): {}",
                        t.getClass().getSimpleName(), t.getMessage(), root);
            }
        }
    }

    /**
     * Get the current depth of a PoseStack by reflecting on its internal poseStack deque.
     */
    private static int getPoseStackDepth(PoseStack poseStack) {
        try {
            for (java.lang.reflect.Field f : PoseStack.class.getDeclaredFields()) {
                if (java.util.Deque.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    return ((java.util.Deque<?>) f.get(poseStack)).size();
                }
            }
        } catch (Throwable ignored) {}
        return 0;
    }
}

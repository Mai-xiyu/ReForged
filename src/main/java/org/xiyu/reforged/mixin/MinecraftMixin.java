package org.xiyu.reforged.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.xiyu.reforged.bridge.FlywheelRenderBridge;
import org.xiyu.reforged.shim.NeoForgeShim;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

/**
 * Injects NeoForge lifecycle events into Minecraft's main loop.
 *
 * <ul>
 *   <li>{@link RenderFrameEvent.Pre} / {@link RenderFrameEvent.Post} — fired around frame rendering</li>
 * </ul>
 *
 * <p>ClientTickEvent.Pre/Post are bridged automatically via wrapper constructors on
 * Forge's TickEvent.ClientTickEvent, so they do not need explicit injection here.</p>
 */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow(remap = false)
    private DeltaTracker.Timer timer;

    @Shadow @Final
    private ReloadableResourceManager resourceManager;

    @Unique
    private boolean reforged$flywheelInitialReloadFired = false;

    /**
     * Fire RenderFrameEvent.Pre before the game renderer processes a frame.
     * Injected at the start of the render call in runTick().
     */
    @Inject(
        method = "runTick(Z)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;render(Lnet/minecraft/client/DeltaTracker;Z)V"
        ),
        remap = false
    )
    private void reforged$onRenderFramePre(boolean renderLevel, CallbackInfo ci) {
        try {
            NeoForgeShim.EVENT_BUS_SHIM.postUntyped(new RenderFrameEvent.Pre(this.timer));
        } catch (Throwable ignored) {
            // Don't crash the game if event dispatch fails
        }
    }

    /**
     * Fire RenderFrameEvent.Post after the game renderer finishes a frame.
     * Injected right after GameRenderer.render() returns.
     */
    @Inject(
        method = "runTick(Z)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/GameRenderer;render(Lnet/minecraft/client/DeltaTracker;Z)V",
            shift = At.Shift.AFTER
        ),
        remap = false
    )
    private void reforged$onRenderFramePost(boolean renderLevel, CallbackInfo ci) {
        try {
            NeoForgeShim.EVENT_BUS_SHIM.postUntyped(new RenderFrameEvent.Post(this.timer));
        } catch (Throwable ignored) {
            // Don't crash the game if event dispatch fails
        }
    }

    // =====================================================================
    // Flywheel rendering bridge: fire EndClientResourceReloadEvent
    // =====================================================================

    /**
     * After initial game load finishes (resources loaded, screens built),
     * fire Flywheel's EndClientResourceReloadEvent to initialize its rendering backend.
     */
    @Inject(method = "onGameLoadFinished", at = @At("HEAD"), remap = false)
    private void reforged$flywheelOnGameLoadFinished(@Coerce Object gameLoadCookie, CallbackInfo ci) {
        if (reforged$flywheelInitialReloadFired) return;
        reforged$flywheelInitialReloadFired = true;
        try {
            FlywheelRenderBridge.freezeRegistries();
            FlywheelRenderBridge.fireEndClientResourceReloadEvent(
                    (Minecraft) (Object) this,
                    this.resourceManager,
                    true,
                    Optional.empty());
        } catch (Throwable ignored) {
        }
    }
}

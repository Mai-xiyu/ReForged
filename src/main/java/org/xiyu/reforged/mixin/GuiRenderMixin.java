package org.xiyu.reforged.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraftforge.common.MinecraftForge;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.xiyu.reforged.bridge.NeoForgeEventBusAdapter;
import org.xiyu.reforged.shim.NeoForgeShim;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects into Gui.render() to:
 * <ul>
 *   <li>Apply deferred NeoForge GUI layers (from {@link RegisterGuiLayersEvent}) on first render</li>
 *   <li>Fire {@link RenderGuiEvent.Post} so NeoForge mods like Jade can render HUD overlays</li>
 * </ul>
 */
@Mixin(Gui.class)
public class GuiRenderMixin {

    @Shadow @Final private LayeredDraw layers;

    @Unique
    private boolean reforged$layersApplied = false;

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At("HEAD"), remap = false)
    private void reforged$applyNeoForgeLayers(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!reforged$layersApplied) {
            reforged$layersApplied = true;
            RegisterGuiLayersEvent event = RegisterGuiLayersEvent.getInstance();
            if (event != null) {
                event.applyTo(this.layers);
            }
        }
    }

    /**
     * Fire RenderGuiEvent.Post after all GUI layers have been rendered.
     * This allows NeoForge mods (Jade, Create, etc.) to render their HUD overlays.
     *
     * <p>We dispatch to THREE targets to ensure all listener registration paths are covered:
     * <ol>
     *   <li>{@code MinecraftForge.EVENT_BUS} — direct {@code addListener(RenderGuiEvent.Post, ...)}
     *       registrations end up here (e.g. Jade's overlay)</li>
     *   <li>{@code NeoForgeEventBusAdapter.dispatchFallback()} — listeners that failed Forge bus
     *       registration end up here (e.g. Flywheel custom events)</li>
     *   <li>{@code EVENT_BUS_SHIM} — local shim bus for any direct shim registrations</li>
     * </ol>
     */
    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V", at = @At("RETURN"), remap = false)
    private void reforged$onRenderGuiPost(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        RenderGuiEvent.Post event = new RenderGuiEvent.Post(guiGraphics, deltaTracker);

        // 1. Post to Forge EVENT_BUS — Jade and Create handlers registered via the adapter proxy
        try {
            MinecraftForge.EVENT_BUS.post(event);
        } catch (Throwable ignored) {
            // Silently handle — event may not have proper ListenerList setup
        }

        // 2. Dispatch to fallback listeners (handlers that couldn't register on Forge bus)
        NeoForgeEventBusAdapter.dispatchFallback(event);

        // 3. Post to the shim bus for any direct shim registrations
        NeoForgeShim.EVENT_BUS_SHIM.post(event);
    }
}

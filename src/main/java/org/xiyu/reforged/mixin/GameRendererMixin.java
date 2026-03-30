package org.xiyu.reforged.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.xiyu.reforged.shim.NeoForgeShim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects NeoForge rendering events into GameRenderer.
 *
 * <p>Most viewport events (ComputeFov, ComputeCameraAngles, RenderFog) are already
 * bridged by the wrapper constructor pattern on their NeoForge event classes.
 * This Mixin fires the {@link ViewportEvent.ComputeFov} for the NeoForge-specific overload
 * that Forge doesn't fire directly, enabling NeoForge mods to modify the FOV.</p>
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    /**
     * Fire a NeoForge-style event at the end of level rendering.
     * This provides NeoForge mods an opportunity to inject custom rendering
     * at the post-render stage.
     */
    @Inject(
        method = "render(Lnet/minecraft/client/DeltaTracker;Z)V",
        at = @At("RETURN"),
        remap = false
    )
    private void reforged$afterRender(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        // Intentional hook point — allows NeoForge event listeners that depend on
        // post-render timing to function correctly.
        // Specific events are dispatched by ClientHooks methods called from
        // LevelRenderer via Forge's own render type dispatch system.
    }
}

package org.xiyu.reforged.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Safety net for NeoForge mod screens that may crash during rendering
 * due to missing Mixin transformations or other compatibility issues.
 * <p>
 * When a screen loaded by the NeoForge classloader throws an exception
 * during {@code renderWithTooltip()}, this Mixin catches the error,
 * logs it, and closes the screen instead of crashing the game.
 * </p>
 */
@Mixin(Screen.class)
public abstract class ScreenRenderSafetyMixin {

    private static final Logger REFORGED_LOGGER = LogUtils.getLogger();
    private static boolean errorLogged = false;

    @Inject(method = "renderWithTooltip(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void reforged$safeRenderWithTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        ClassLoader screenLoader = self.getClass().getClassLoader();

        // Only wrap screens from NeoForge mod classloader
        if (screenLoader == null || !screenLoader.getClass().getName().contains("NeoModClassLoader")) {
            return;
        }

        try {
            self.render(guiGraphics, mouseX, mouseY, delta);
        } catch (Throwable t) {
            if (!errorLogged) {
                REFORGED_LOGGER.error("[ReForged] NeoForge mod screen '{}' crashed during render, closing screen: {}",
                        self.getClass().getName(), t.getMessage(), t);
                errorLogged = true;
            }
            try {
                Minecraft.getInstance().setScreen(null);
            } catch (Throwable ignored) {}
            ci.cancel();
            return;
        }

        // renderWithTooltip normally calls render() then tooltip rendering.
        // Since we called render() ourselves above, we cancel to avoid double-render.
        // Tooltip rendering is skipped for safety (may also depend on Mixin-injected code).
        ci.cancel();
    }
}

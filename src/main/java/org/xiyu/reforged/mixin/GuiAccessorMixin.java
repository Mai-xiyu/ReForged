package org.xiyu.reforged.mixin;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Gui.class, remap = false)
public abstract class GuiAccessorMixin {
    @Shadow @Final private SubtitleOverlay subtitleOverlay;
    @Shadow private int toolHighlightTimer;

    @Shadow
    private void renderTextureOverlay(GuiGraphics guiGraphics, ResourceLocation texture, float alpha) {
        throw new AssertionError();
    }

    public SubtitleOverlay create$getSubtitleOverlay() {
        return this.subtitleOverlay;
    }

    public int create$getToolHighlightTimer() {
        return this.toolHighlightTimer;
    }

    public void create$renderTextureOverlay(GuiGraphics guiGraphics, ResourceLocation texture, float alpha) {
        this.renderTextureOverlay(guiGraphics, texture, alpha);
    }
}

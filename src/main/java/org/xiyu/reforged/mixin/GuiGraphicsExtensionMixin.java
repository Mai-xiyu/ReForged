package org.xiyu.reforged.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.IGuiGraphicsExtension;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Makes GuiGraphics implement NeoForge's IGuiGraphicsExtension interface,
 * enabling 9-slice rendering and inscribed blit for NeoForge mods.
 */
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsExtensionMixin implements IGuiGraphicsExtension {

	@Override
	public void blitWithBorder(ResourceLocation texture, int x, int y, int u, int v,
							   int width, int height, int textureWidth, int textureHeight, int borderSize) {
		IGuiGraphicsExtension.super.blitWithBorder(texture, x, y, u, v, width, height, textureWidth, textureHeight, borderSize);
	}

	@Override
	public void blitWithBorder(ResourceLocation texture, int x, int y, int u, int v,
							   int width, int height, int textureWidth, int textureHeight,
							   int topBorder, int bottomBorder, int leftBorder, int rightBorder) {
		IGuiGraphicsExtension.super.blitWithBorder(
				texture, x, y, u, v, width, height, textureWidth, textureHeight,
				topBorder, bottomBorder, leftBorder, rightBorder);
	}

	@Override
	public void blitInscribed(ResourceLocation texture, int x, int y,
							  int boundsWidth, int boundsHeight, int rectWidth, int rectHeight) {
		IGuiGraphicsExtension.super.blitInscribed(texture, x, y, boundsWidth, boundsHeight, rectWidth, rectHeight);
	}

	@Override
	public void blitInscribed(ResourceLocation texture, int x, int y,
							  int boundsWidth, int boundsHeight, int rectWidth, int rectHeight,
							  boolean centerX, boolean centerY) {
		IGuiGraphicsExtension.super.blitInscribed(
				texture, x, y, boundsWidth, boundsHeight, rectWidth, rectHeight, centerX, centerY);
	}
}

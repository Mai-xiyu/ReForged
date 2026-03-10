package net.neoforged.neoforge.client.extensions;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Extension interface for {@link net.minecraft.client.gui.GuiGraphics}.
 * Provides NeoForge's additional rendering utilities.
 */
public interface IGuiGraphicsExtension {

    int DEFAULT_BACKGROUND_COLOR = 0xF0100010;
    int DEFAULT_BORDER_COLOR_START = 0x505000FF;
    int DEFAULT_BORDER_COLOR_END = 0x5028007F;

    /**
     * Draws a texture with a nine-sliced border.
     */
    default void blitWithBorder(ResourceLocation texture, int x, int y, int u, int v,
                                int width, int height, int textureWidth, int textureHeight, int borderSize) {
        blitWithBorder(texture, x, y, u, v, width, height, textureWidth, textureHeight,
                borderSize, borderSize, borderSize, borderSize);
    }

    /**
     * Draws a texture with a nine-sliced border with separate border sizes.
     */
    default void blitWithBorder(ResourceLocation texture, int x, int y, int u, int v,
                                int width, int height, int textureWidth, int textureHeight,
                                int topBorder, int bottomBorder, int leftBorder, int rightBorder) {
        // Default no-op; actual implementation requires GuiGraphics blit access
    }

    /**
     * Draws a texture inscribed within the given bounds, maintaining aspect ratio.
     */
    default void blitInscribed(ResourceLocation texture, int x, int y,
                               int boundsWidth, int boundsHeight, int rectWidth, int rectHeight) {
        blitInscribed(texture, x, y, boundsWidth, boundsHeight, rectWidth, rectHeight, true, true);
    }

    /**
     * Draws a texture inscribed within the given bounds, maintaining aspect ratio.
     */
    default void blitInscribed(ResourceLocation texture, int x, int y,
                               int boundsWidth, int boundsHeight, int rectWidth, int rectHeight,
                               boolean centerX, boolean centerY) {
        // Default no-op
    }
}

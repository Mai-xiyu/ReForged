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
        if (!(this instanceof net.minecraft.client.gui.GuiGraphics gg)) return;

        int fillerWidth = textureWidth - leftBorder - rightBorder;
        int fillerHeight = textureHeight - topBorder - bottomBorder;
        int canvasWidth = width - leftBorder - rightBorder;
        int canvasHeight = height - topBorder - bottomBorder;
        int xPasses = canvasWidth / fillerWidth;
        int remainderWidth = canvasWidth % fillerWidth;
        int yPasses = canvasHeight / fillerHeight;
        int remainderHeight = canvasHeight % fillerHeight;

        // Top-left corner
        gg.blit(texture, x, y, u, v, leftBorder, topBorder);
        // Top-right corner
        gg.blit(texture, x + leftBorder + canvasWidth, y, u + leftBorder + fillerWidth, v, rightBorder, topBorder);
        // Bottom-left corner
        gg.blit(texture, x, y + topBorder + canvasHeight, u, v + topBorder + fillerHeight, leftBorder, bottomBorder);
        // Bottom-right corner
        gg.blit(texture, x + leftBorder + canvasWidth, y + topBorder + canvasHeight,
                u + leftBorder + fillerWidth, v + topBorder + fillerHeight, rightBorder, bottomBorder);

        // Top and bottom edges
        for (int i = 0; i < xPasses + (remainderWidth > 0 ? 1 : 0); i++) {
            int drawWidth = (i == xPasses) ? remainderWidth : fillerWidth;
            gg.blit(texture, x + leftBorder + (i * fillerWidth), y,
                    u + leftBorder, v, drawWidth, topBorder);
            gg.blit(texture, x + leftBorder + (i * fillerWidth), y + topBorder + canvasHeight,
                    u + leftBorder, v + topBorder + fillerHeight, drawWidth, bottomBorder);
        }

        // Left and right edges
        for (int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); j++) {
            int drawHeight = (j == yPasses) ? remainderHeight : fillerHeight;
            gg.blit(texture, x, y + topBorder + (j * fillerHeight),
                    u, v + topBorder, leftBorder, drawHeight);
            gg.blit(texture, x + leftBorder + canvasWidth, y + topBorder + (j * fillerHeight),
                    u + leftBorder + fillerWidth, v + topBorder, rightBorder, drawHeight);
        }

        // Center fill
        for (int i = 0; i < xPasses + (remainderWidth > 0 ? 1 : 0); i++) {
            int drawWidth = (i == xPasses) ? remainderWidth : fillerWidth;
            for (int j = 0; j < yPasses + (remainderHeight > 0 ? 1 : 0); j++) {
                int drawHeight = (j == yPasses) ? remainderHeight : fillerHeight;
                gg.blit(texture, x + leftBorder + (i * fillerWidth), y + topBorder + (j * fillerHeight),
                        u + leftBorder, v + topBorder, drawWidth, drawHeight);
            }
        }
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
        if (!(this instanceof net.minecraft.client.gui.GuiGraphics gg)) return;

        if (rectWidth * boundsHeight > rectHeight * boundsWidth) {
            int actualHeight = boundsWidth * rectHeight / rectWidth;
            int yOffset = centerY ? (boundsHeight - actualHeight) / 2 : 0;
            gg.blit(texture, x, y + yOffset, 0, 0, boundsWidth, actualHeight, boundsWidth, actualHeight);
        } else {
            int actualWidth = boundsHeight * rectWidth / rectHeight;
            int xOffset = centerX ? (boundsWidth - actualWidth) / 2 : 0;
            gg.blit(texture, x + xOffset, y, 0, 0, actualWidth, boundsHeight, actualWidth, boundsHeight);
        }
    }
}

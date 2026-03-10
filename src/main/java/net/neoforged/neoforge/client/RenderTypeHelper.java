package net.neoforged.neoforge.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;

/**
 * Provides utility methods related to {@link RenderType} for entity and block rendering.
 */
public final class RenderTypeHelper {
    private RenderTypeHelper() {}

    /**
     * Gets the entity render type for a given chunk render type, used for block entity rendering.
     * @param chunkRenderType the chunk render type (e.g., solid, cutout, translucent)
     * @param cull whether to cull back faces
     * @return the appropriate entity render type for rendering in the chunk layer
     */
    public static RenderType getEntityRenderType(RenderType chunkRenderType, boolean cull) {
        if (chunkRenderType == RenderType.translucent()) {
            return cull ? Sheets.translucentCullBlockSheet() : Sheets.translucentItemSheet();
        }
        return Sheets.cutoutBlockSheet();
    }

    /**
     * Gets the render type for moving/breaking-animation blocks.
     * @param chunkRenderType the original chunk render type
     * @return the corresponding render type for the destruction overlay
     */
    public static RenderType getMovingBlockRenderType(RenderType chunkRenderType) {
        if (chunkRenderType == RenderType.translucent()) {
            return RenderType.translucentMovingBlock();
        }
        return chunkRenderType;
    }

    /**
     * Gets the fallback item render type when a model doesn't specify one.
     * @return the translucent item render type as fallback
     */
    public static RenderType getFallbackItemRenderType() {
        return NeoForgeRenderTypes.ITEM_LAYERED_TRANSLUCENT.get();
    }
}

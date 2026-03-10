package net.neoforged.neoforge.client.gui.map;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

/**
 * Custom map decoration renderer interface.
 * Return true from render to cancel vanilla rendering.
 */
public interface IMapDecorationRenderer {

    /**
     * Renders a map decoration. Return true to cancel vanilla rendering.
     */
    boolean render(MapDecoration decoration, PoseStack poseStack, MultiBufferSource bufferSource,
                   MapItemSavedData mapData, boolean inItemFrame, int packedLight, int index);
}

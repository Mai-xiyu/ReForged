package net.neoforged.neoforge.client.event;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.util.Map;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.RenderTypeGroup;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Stub: Fired to register named render types.
 */
public class RegisterNamedRenderTypesEvent extends net.neoforged.bus.api.Event implements IModBusEvent {
    private final Map<ResourceLocation, RenderTypeGroup> renderTypes;

    public RegisterNamedRenderTypesEvent(Map<ResourceLocation, RenderTypeGroup> renderTypes) {
        this.renderTypes = renderTypes;
    }

    public void register(ResourceLocation key, RenderType blockRenderType, RenderType entityRenderType) {
        register(key, blockRenderType, entityRenderType, entityRenderType);
    }

    public void register(ResourceLocation key, RenderType blockRenderType, RenderType entityRenderType, RenderType fabulousEntityRenderType) {
        Preconditions.checkArgument(!renderTypes.containsKey(key), "Render type already registered: " + key);
        Preconditions.checkArgument(blockRenderType.format() == DefaultVertexFormat.BLOCK, "The block render type must use the BLOCK vertex format.");
        Preconditions.checkArgument(blockRenderType.getChunkLayerId() >= 0, "Only chunk render types can be used for block rendering.");
        Preconditions.checkArgument(entityRenderType.format() == DefaultVertexFormat.NEW_ENTITY, "The entity render type must use the NEW_ENTITY vertex format.");
        Preconditions.checkArgument(fabulousEntityRenderType.format() == DefaultVertexFormat.NEW_ENTITY, "The fabulous entity render type must use the NEW_ENTITY vertex format.");
        renderTypes.put(key, new RenderTypeGroup(blockRenderType, entityRenderType, fabulousEntityRenderType));
    }
}

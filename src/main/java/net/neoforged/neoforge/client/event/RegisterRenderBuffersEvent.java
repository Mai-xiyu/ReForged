package net.neoforged.neoforge.client.event;

import java.util.SequencedMap;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Fired to allow registration of custom render buffers.
 */
public class RegisterRenderBuffersEvent extends net.neoforged.bus.api.Event implements IModBusEvent {
    private final SequencedMap<RenderType, ByteBufferBuilder> renderBuffers;

    public RegisterRenderBuffersEvent(SequencedMap<RenderType, ByteBufferBuilder> renderBuffers) {
        this.renderBuffers = renderBuffers;
    }

    public void registerRenderBuffer(RenderType renderType) {
        renderBuffers.computeIfAbsent(renderType, k -> new ByteBufferBuilder(k.bufferSize()));
    }
}

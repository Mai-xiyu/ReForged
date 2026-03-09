package net.neoforged.neoforge.common.extensions;

import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.Nullable;

/**
 * Server-side packet listener extension surface.
 */
public interface IServerCommonPacketListenerExtension extends ICommonPacketListener {
    @Override
    default void send(CustomPacketPayload payload) {
        this.send(new ClientboundCustomPayloadPacket(payload));
    }

    void send(Packet<?> packet, @Nullable PacketSendListener listener);

    default void send(CustomPacketPayload payload, @Nullable PacketSendListener listener) {
        this.send(new ClientboundCustomPayloadPacket(payload), listener);
    }
}

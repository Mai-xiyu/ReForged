package net.neoforged.neoforge.common.extensions;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

/**
 * Common packet listener extension surface used by NeoForge APIs.
 */
public interface ICommonPacketListener extends PacketListener {
    void send(Packet<?> packet);

    void send(CustomPacketPayload payload);

    void disconnect(Component reason);

    Connection getConnection();

    ReentrantBlockableEventLoop<?> getMainThreadEventLoop();

    default boolean hasChannel(final ResourceLocation payloadId) {
        return NetworkRegistry.hasChannel(this.getConnection(), this.protocol(), payloadId);
    }

    default boolean hasChannel(final CustomPacketPayload.Type<?> type) {
        return hasChannel(type.id());
    }

    default boolean hasChannel(final CustomPacketPayload payload) {
        return hasChannel(payload.type());
    }

    ConnectionType getConnectionType();
}

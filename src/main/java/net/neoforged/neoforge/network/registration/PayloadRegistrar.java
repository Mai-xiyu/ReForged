package net.neoforged.neoforge.network.registration;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

/**
 * Proxy: NeoForge's PayloadRegistrar for network packet registration.
 * Delegates to the shim PayloadRegistrar which bridges to Forge's SimpleChannel.
 */
public class PayloadRegistrar {
    private final org.xiyu.reforged.shim.network.PayloadRegistrar delegate;

    public PayloadRegistrar(String version) {
        this.delegate = new org.xiyu.reforged.shim.network.PayloadRegistrar(version);
    }

    public String getVersion() { return delegate.getVersion(); }

    public <T extends CustomPacketPayload> PayloadRegistrar playToServer(
            CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, IPayloadHandler<T> handler) {
        delegate.playToServer(type, codec, handler);
        return this;
    }

    public <T extends CustomPacketPayload> PayloadRegistrar playToClient(
            CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, IPayloadHandler<T> handler) {
        delegate.playToClient(type, codec, handler);
        return this;
    }

    public <T extends CustomPacketPayload> PayloadRegistrar playBidirectional(
            CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, IPayloadHandler<T> handler) {
        delegate.playBidirectional(type, codec, handler);
        return this;
    }

    public <T extends CustomPacketPayload> PayloadRegistrar configurationToClient(
            CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec, IPayloadHandler<T> handler) {
        delegate.configurationToClient(type, codec, handler);
        return this;
    }

    public <T extends CustomPacketPayload> PayloadRegistrar configurationToServer(
            CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec, IPayloadHandler<T> handler) {
        delegate.configurationToServer(type, codec, handler);
        return this;
    }

    public <T extends CustomPacketPayload> PayloadRegistrar configurationBidirectional(
            CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec, IPayloadHandler<T> handler) {
        delegate.configurationBidirectional(type, codec, handler);
        return this;
    }

    public <T extends CustomPacketPayload> PayloadRegistrar commonToClient(
            CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec, IPayloadHandler<T> handler) {
        delegate.commonToClient(type, codec, handler);
        return this;
    }

    public <T extends CustomPacketPayload> PayloadRegistrar commonToServer(
            CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec, IPayloadHandler<T> handler) {
        delegate.commonToServer(type, codec, handler);
        return this;
    }

    public <T extends CustomPacketPayload> PayloadRegistrar commonBidirectional(
            CustomPacketPayload.Type<T> type, StreamCodec<? super FriendlyByteBuf, T> codec, IPayloadHandler<T> handler) {
        delegate.commonBidirectional(type, codec, handler);
        return this;
    }

    // Fallback for Object parameters (ASM-rewritten code)
    public <T> PayloadRegistrar playToServer(Object type, Object codec, Object handler) {
        delegate.playToServer(type, codec, handler);
        return this;
    }
    public <T> PayloadRegistrar playToClient(Object type, Object codec, Object handler) {
        delegate.playToClient(type, codec, handler);
        return this;
    }
    public <T> PayloadRegistrar playBidirectional(Object type, Object codec, Object handler) {
        delegate.playBidirectional(type, codec, handler);
        return this;
    }

    public PayloadRegistrar optional() {
        delegate.optional();
        return this;
    }

    public PayloadRegistrar versioned(String version) {
        return new PayloadRegistrar(version);
    }

    public PayloadRegistrar executesOn(Object thread) {
        delegate.executesOn(thread);
        return this;
    }

    public PayloadRegistrar executesOn(HandlerThread thread) {
        delegate.executesOn(thread);
        return this;
    }
}

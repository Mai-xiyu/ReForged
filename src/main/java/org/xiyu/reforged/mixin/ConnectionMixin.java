package org.xiyu.reforged.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.xiyu.reforged.shim.network.PayloadChannelRegistry;

/**
 * Intercepts NeoForge custom payloads being sent through the vanilla codec path.
 *
 * <p>When NeoForge mods construct {@link ClientboundCustomPayloadPacket} or
 * {@link ServerboundCustomPayloadPacket} containing NeoForge-registered payloads
 * and send them through {@link Connection#send(Packet)}, the vanilla codec
 * doesn't know how to encode them and throws a ClassCastException (trying to cast
 * to DiscardedPayload). This mixin redirects such packets through our
 * SimpleChannel-based bridge instead.</p>
 *
 * <p>We inject at both {@code send(Packet, PacketSendListener)} and
 * {@code doSendPacket(Packet, PacketSendListener, boolean)} as a safety net,
 * since some code paths may bypass the higher-level {@code send} method.</p>
 */
@Mixin(Connection.class)
public class ConnectionMixin {

    private static final Logger REFORGED_LOGGER = LogUtils.getLogger();
    private static final boolean JADE_DIAGNOSTICS_ENABLED = Boolean.getBoolean("reforged.jadeDiagnostics");

    /**
     * Primary interception point: the 2-arg send method that all public send() calls route through.
     */
    @Inject(
        method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void reforged$interceptNeoForgePayload(Packet<?> packet, PacketSendListener listener, CallbackInfo ci) {
        if (reforged$tryIntercept(packet, "send2")) {
            ci.cancel();
        }
    }

    /**
     * Safety-net interception: the 3-arg send method (send includes flush flag).
     */
    @Inject(
        method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;Z)V",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void reforged$interceptNeoForgePayload3(Packet<?> packet, PacketSendListener listener, boolean flush, CallbackInfo ci) {
        if (reforged$tryIntercept(packet, "send3")) {
            ci.cancel();
        }
    }

    /**
     * Last-resort interception: doSendPacket writes directly to the Netty channel.
     * If a packet slips past the send() methods, catch it here before the codec fails.
     */
    @Inject(
        method = "doSendPacket",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void reforged$interceptDoSendPacket(Packet<?> packet, PacketSendListener listener, boolean flush, CallbackInfo ci) {
        if (reforged$tryIntercept(packet, "doSendPacket")) {
            ci.cancel();
        }
    }

    /**
     * Common logic for intercepting NeoForge payloads wrapped in vanilla custom payload packets.
     *
     * @return true if the packet was intercepted and should be cancelled
     */
    @Unique
    private boolean reforged$tryIntercept(Packet<?> packet, String source) {
        // Intercept clientbound custom payload packets (server → client)
        if (packet instanceof ClientboundCustomPayloadPacket cppp) {
            CustomPacketPayload payload = cppp.payload();
            if (payload != null) {
                ResourceLocation payloadId = payload.type().id();
                if (PayloadChannelRegistry.getEntry(payloadId) != null) {
                    if (JADE_DIAGNOSTICS_ENABLED && "jade".equals(payloadId.getNamespace())) {
                        REFORGED_LOGGER.debug("[ReForged] Intercepting clientbound NeoForge payload via {}: {}", source, payloadId);
                    }
                    PayloadChannelRegistry.sendViaConnection((Connection) (Object) this, payload);
                    return true;
                }
            }
        }

        // Intercept serverbound custom payload packets (client → server)
        if (packet instanceof ServerboundCustomPayloadPacket sppp) {
            CustomPacketPayload payload = sppp.payload();
            if (payload != null) {
                ResourceLocation payloadId = payload.type().id();
                if (PayloadChannelRegistry.getEntry(payloadId) != null) {
                    if (JADE_DIAGNOSTICS_ENABLED && "jade".equals(payloadId.getNamespace())) {
                        REFORGED_LOGGER.debug("[ReForged] Intercepting serverbound NeoForge payload via {}: {}", source, payloadId);
                    }
                    PayloadChannelRegistry.sendToServer(payload);
                    return true;
                }
            }
        }

        return false;
    }
}

package org.xiyu.reforged.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
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
 */
@Mixin(Connection.class)
public class ConnectionMixin {

    private static final Logger REFORGED_LOGGER = LogUtils.getLogger();

    @Inject(
        method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;)V",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void reforged$interceptNeoForgePayload(Packet<?> packet, PacketSendListener listener, CallbackInfo ci) {
        // Intercept clientbound custom payload packets (server → client)
        if (packet instanceof ClientboundCustomPayloadPacket cppp) {
            CustomPacketPayload payload = cppp.payload();
            if (payload != null && PayloadChannelRegistry.getEntry(payload.type().id()) != null) {
                REFORGED_LOGGER.debug("[ReForged] Intercepting clientbound NeoForge payload: {}", payload.type().id());
                PayloadChannelRegistry.sendViaConnection((Connection) (Object) this, payload);
                ci.cancel();
                return;
            }
        }

        // Intercept serverbound custom payload packets (client → server)
        if (packet instanceof ServerboundCustomPayloadPacket sppp) {
            CustomPacketPayload payload = sppp.payload();
            if (payload != null && PayloadChannelRegistry.getEntry(payload.type().id()) != null) {
                REFORGED_LOGGER.debug("[ReForged] Intercepting serverbound NeoForge payload: {}", payload.type().id());
                PayloadChannelRegistry.sendToServer(payload);
                ci.cancel();
                return;
            }
        }
    }
}

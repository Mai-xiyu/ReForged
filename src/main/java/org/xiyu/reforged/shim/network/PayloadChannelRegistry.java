package org.xiyu.reforged.shim.network;

import com.mojang.logging.LogUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Central registry that bridges NeoForge's payload-based networking to Forge's SimpleChannel.
 *
 * <p>This registry manages:
 * <ul>
 *   <li>A pool of SimpleChannel instances keyed by channel name (typically modId)</li>
 *   <li>An index from CustomPacketPayload.Type → channel + discriminator</li>
 *   <li>StreamCodec adaptation from NeoForge's codec to Forge's encoder/decoder</li>
 * </ul>
 */
public final class PayloadChannelRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean JADE_DIAGNOSTICS_ENABLED = Boolean.getBoolean("reforged.jadeDiagnostics");

    /** Channel name → SimpleChannel instance */
    private static final Map<String, SimpleChannel> CHANNELS = new ConcurrentHashMap<>();

    /** Channel name → next available discriminator */
    private static final Map<String, AtomicInteger> DISCRIMINATORS = new ConcurrentHashMap<>();

    /** Channel name → wrapper message already registered */
    private static final Map<String, Boolean> CHANNEL_WRAPPER_REGISTERED = new ConcurrentHashMap<>();

    /** Payload type ResourceLocation → ChannelEntry (the channel + discriminator + handler info) */
    private static final Map<ResourceLocation, ChannelEntry<?>> PAYLOAD_INDEX = new ConcurrentHashMap<>();

    private static final Set<ResourceLocation> JADE_DIAGNOSTIC_PAYLOADS = Set.of(
            ResourceLocation.fromNamespaceAndPath("jade", "show_overlay"),
            ResourceLocation.fromNamespaceAndPath("jade", "receive_data")
    );

    private PayloadChannelRegistry() {}

    /**
     * Get (or create) a SimpleChannel for the given channel name.
     * Channel names are derived from the payload type's namespace.
     */
    public static SimpleChannel getOrCreateChannel(String channelName) {
        return CHANNELS.computeIfAbsent(channelName, name -> {
            ResourceLocation rl = ResourceLocation.tryBuild(name, "reforged_net");
            if (rl == null) {
                rl = ResourceLocation.fromNamespaceAndPath("reforged", name.replace(':', '_'));
            }
            LOGGER.info("[ReForged] Creating Forge SimpleChannel: {}", rl);
            return ChannelBuilder.named(rl)
                    .optional()
                    .simpleChannel();
        });
    }

    /**
     * Register a NeoForge payload type, bridging it to a Forge SimpleChannel message.
     *
     * @param type    The NeoForge payload type
     * @param codec   The NeoForge StreamCodec for encoding/decoding
     * @param handler The NeoForge handler
     * @param flow    The packet flow direction (null = bidirectional)
     * @param <T>     The payload type
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends CustomPacketPayload> void registerPayload(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super FriendlyByteBuf, T> codec,
            IPayloadHandler<T> handler,
            PacketFlow flow) {

        ResourceLocation payloadId = type.id();
        String channelName = payloadId.getNamespace();
        SimpleChannel channel = getOrCreateChannel(channelName);
        int discriminator = DISCRIMINATORS.computeIfAbsent(channelName, k -> new AtomicInteger(0)).getAndIncrement();

        LOGGER.info("[ReForged] Registering payload: {} (channel={}, disc={}, flow={})",
            payloadId, channelName, discriminator, flow);

        // Create a wrapper message class for this specific payload
        // We use a single wrapper type that carries the original payload
        ChannelEntry<T> entry = new ChannelEntry<>(channel, discriminator, type, codec, handler, flow);
        PAYLOAD_INDEX.put(payloadId, entry);

        // Register one wrapper message per channel with Forge's SimpleChannel.
        // SimpleChannel indexes outbound messages by concrete class; registering
        // the same wrapper class multiple times causes dispatch conflicts.
        if (Boolean.TRUE.equals(CHANNEL_WRAPPER_REGISTERED.putIfAbsent(channelName, true))) {
            LOGGER.debug("[ReForged] Channel wrapper already registered for {}", channelName);
            return;
        }

        // Register with Forge's SimpleChannel
        try {
            SimpleChannel.MessageBuilder<PayloadWrapper, FriendlyByteBuf> builder =
                    channel.messageBuilder(PayloadWrapper.class, 0);

            // encoder: write the payload ID then use the NeoForge codec
            builder.encoder((wrapper, buf) -> {
                buf.writeResourceLocation(wrapper.payloadId);
                ChannelEntry<?> e = PAYLOAD_INDEX.get(wrapper.payloadId);
                if (e == null) {
                    LOGGER.error("[ReForged] Unknown payload ID during encode: {}", wrapper.payloadId);
                    return;
                }
                try {
                    if (isJadeDiagnosticPayload(wrapper.payloadId)) {
                        LOGGER.debug("[ReForged][JadeDiag] encode payload={} payloadClass={} bufClass={}",
                                wrapper.payloadId,
                                wrapper.payload != null ? wrapper.payload.getClass().getName() : "null",
                                buf.getClass().getName());
                    }
                    ((StreamCodec) e.codec()).encode(buf, wrapper.payload);
                } catch (Throwable ex) {
                    LOGGER.error("[ReForged] Failed to encode payload: {}", wrapper.payloadId, ex);
                }
            });

            // decoder: read the payload ID then use the NeoForge codec
            builder.decoder(buf -> {
                ResourceLocation id = buf.readResourceLocation();
                ChannelEntry<?> e = PAYLOAD_INDEX.get(id);
                if (e == null) {
                    LOGGER.error("[ReForged] Unknown payload ID during decode: {}", id);
                    return new PayloadWrapper<>(id, null);
                }
                try {
                    Object decoded = ((StreamCodec) e.codec()).decode(buf);
                    if (isJadeDiagnosticPayload(id)) {
                        LOGGER.debug("[ReForged][JadeDiag] decode payload={} decodedClass={} bufClass={}",
                                id,
                                decoded != null ? decoded.getClass().getName() : "null",
                                buf.getClass().getName());
                    }
                    return new PayloadWrapper<>(id, decoded);
                } catch (Throwable ex) {
                    LOGGER.error("[ReForged] Failed to decode payload: {}", id, ex);
                    return new PayloadWrapper<>(id, null);
                }
            });

            // consumer: dispatch to the NeoForge handler with a bridged context
            builder.consumerMainThread((wrapper, ctx) -> {
                if (wrapper.payload == null) {
                    LOGGER.warn("[ReForged] Null payload received for: {}", wrapper.payloadId);
                    ctx.setPacketHandled(true);
                    return;
                }
                ChannelEntry<?> e = PAYLOAD_INDEX.get(wrapper.payloadId);
                if (e == null) {
                    LOGGER.warn("[ReForged] No handler found for payload: {}", wrapper.payloadId);
                    ctx.setPacketHandled(true);
                    return;
                }
                try {
                    IPayloadContext neoCtx = new ReForgedPayloadContext(ctx);
                    if (isJadeDiagnosticPayload(wrapper.payloadId)) {
                        String packetFlow = neoCtx.flow() != null ? neoCtx.flow().name() : "unknown";
                        String sender = ctx.getSender() != null ? ctx.getSender().getGameProfile().getName() : "client/local";
                        LOGGER.debug("[ReForged][JadeDiag] handle payload={} flow={} sender={} payloadClass={}",
                                wrapper.payloadId,
                                packetFlow,
                                sender,
                                wrapper.payload.getClass().getName());
                    }
                    ((IPayloadHandler) e.handler()).handle((CustomPacketPayload) wrapper.payload, neoCtx);
                } catch (Throwable ex) {
                    LOGGER.error("[ReForged] Error handling payload: {}", wrapper.payloadId, ex);
                }
                ctx.setPacketHandled(true);
            });

            builder.add();
            LOGGER.info("[ReForged] Successfully registered payload bridge channel: {}", channelName);
        } catch (Throwable ex) {
            CHANNEL_WRAPPER_REGISTERED.remove(channelName);
            LOGGER.error("[ReForged] Failed to register payload bridge channel: {}", channelName, ex);
        }
    }

    /**
     * Send a payload to a specific player using the registered channel.
     */
    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ChannelEntry<?> entry = PAYLOAD_INDEX.get(payload.type().id());
        if (entry == null) {
            LOGGER.warn("[ReForged] Cannot send payload {}: not registered", payload.type().id());
            return;
        }
        if (isJadeDiagnosticPayload(payload.type().id())) {
            LOGGER.debug("[ReForged][JadeDiag] sendToPlayer payload={} target={} payloadClass={}",
                    payload.type().id(),
                    player.getGameProfile().getName(),
                    payload.getClass().getName());
        }
        PayloadWrapper<CustomPacketPayload> wrapper = new PayloadWrapper<>(payload.type().id(), payload);
        entry.channel().send(wrapper, PacketDistributor.PLAYER.with(player));
    }

    /**
     * Send a payload to the server (client → server).
     */
    public static void sendToServer(CustomPacketPayload payload) {
        ChannelEntry<?> entry = PAYLOAD_INDEX.get(payload.type().id());
        if (entry == null) {
            LOGGER.warn("[ReForged] Cannot send payload {}: not registered", payload.type().id());
            return;
        }
        if (isJadeDiagnosticPayload(payload.type().id())) {
            LOGGER.debug("[ReForged][JadeDiag] sendToServer payload={} payloadClass={}",
                    payload.type().id(), payload.getClass().getName());
        }
        PayloadWrapper<CustomPacketPayload> wrapper = new PayloadWrapper<>(payload.type().id(), payload);
        entry.channel().send(wrapper, PacketDistributor.SERVER.noArg());
    }

    /**
     * Send a payload to all players.
     */
    public static void sendToAllPlayers(CustomPacketPayload payload) {
        ChannelEntry<?> entry = PAYLOAD_INDEX.get(payload.type().id());
        if (entry == null) {
            LOGGER.warn("[ReForged] Cannot send payload {}: not registered", payload.type().id());
            return;
        }
        if (isJadeDiagnosticPayload(payload.type().id())) {
            LOGGER.debug("[ReForged][JadeDiag] sendToAllPlayers payload={} payloadClass={}",
                    payload.type().id(), payload.getClass().getName());
        }
        PayloadWrapper<CustomPacketPayload> wrapper = new PayloadWrapper<>(payload.type().id(), payload);
        entry.channel().send(wrapper, PacketDistributor.ALL.noArg());
    }

    /**
     * Send a payload through a specific connection (used for reply()).
     */
    public static void sendViaConnection(Connection connection, CustomPacketPayload payload) {
        ChannelEntry<?> entry = PAYLOAD_INDEX.get(payload.type().id());
        if (entry == null) {
            LOGGER.warn("[ReForged] Cannot reply with payload {}: not registered", payload.type().id());
            return;
        }
        if (isJadeDiagnosticPayload(payload.type().id())) {
            LOGGER.debug("[ReForged][JadeDiag] sendViaConnection payload={} flow={} payloadClass={}",
                    payload.type().id(),
                    connection.getReceiving() == PacketFlow.CLIENTBOUND ? "clientbound" : "serverbound",
                    payload.getClass().getName());
        }
        // Use the NMLIST distributor with a single connection
        PayloadWrapper<CustomPacketPayload> wrapper = new PayloadWrapper<>(payload.type().id(), payload);
        entry.channel().send(wrapper, PacketDistributor.NMLIST.with(java.util.List.of(connection)));
    }

    private static boolean isJadeDiagnosticPayload(ResourceLocation payloadId) {
        return JADE_DIAGNOSTICS_ENABLED
            && payloadId != null
            && "jade".equals(payloadId.getNamespace())
                && JADE_DIAGNOSTIC_PAYLOADS.contains(payloadId);
    }

    /**
     * Get the channel entry for a given payload type.
     */
    public static ChannelEntry<?> getEntry(ResourceLocation payloadId) {
        return PAYLOAD_INDEX.get(payloadId);
    }

    /**
     * Internal wrapper message class used to carry NeoForge payloads through Forge's SimpleChannel.
     */
    public static class PayloadWrapper<T> {
        final ResourceLocation payloadId;
        final T payload;

        public PayloadWrapper(ResourceLocation payloadId, T payload) {
            this.payloadId = payloadId;
            this.payload = payload;
        }
    }

    /**
     * Record holding all information about a registered payload bridge.
     */
    public record ChannelEntry<T extends CustomPacketPayload>(
            SimpleChannel channel,
            int discriminator,
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super FriendlyByteBuf, T> codec,
            IPayloadHandler<T> handler,
            PacketFlow flow
    ) {}
}

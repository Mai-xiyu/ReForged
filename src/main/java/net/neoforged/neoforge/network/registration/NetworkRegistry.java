package net.neoforged.neoforge.network.registration;

import com.mojang.logging.LogUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.slf4j.Logger;
import org.xiyu.reforged.core.NeoForgeModLoader;
import org.xiyu.reforged.shim.network.PayloadChannelRegistry;

import java.util.List;
import java.util.Optional;

/**
 * Stub: Network registry for tracking registered channels.
 */
public class NetworkRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile boolean setup;

    private NetworkRegistry() {}

    public static synchronized void setup() {
        if (setup) {
            return;
        }
        NeoForgeModLoader.dispatchNeoForgeModEvent(new RegisterPayloadHandlersEvent());
        setup = true;
        LOGGER.info("[ReForged] NetworkRegistry setup complete");
    }

    public static <T extends CustomPacketPayload, B extends FriendlyByteBuf> void register(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super B, T> codec,
            IPayloadHandler<T> handler,
            List<ConnectionProtocol> protocols,
            Optional<PacketFlow> flow,
            String version,
            boolean optional) {
        @SuppressWarnings("unchecked")
        StreamCodec<? super FriendlyByteBuf, T> castCodec = (StreamCodec<? super FriendlyByteBuf, T>) codec;
        PayloadChannelRegistry.registerPayload(type, castCodec, handler, flow.orElse(null));
    }

    public static boolean hasChannel(ResourceLocation channel) {
        return PayloadChannelRegistry.getEntry(channel) != null;
    }

    public static boolean hasChannel(Connection connection, ConnectionProtocol protocol, ResourceLocation payloadId) {
        return hasChannel(payloadId);
    }
}

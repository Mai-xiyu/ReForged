package net.neoforged.neoforge.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Payload for server → client screen opening with additional data.
 * <p>Used by NeoForge mods to open MenuType-based screens with extra NBT/buffer data
 * (similar to Forge's NetworkHooks.openScreen).</p>
 */
public record AdvancedOpenScreenPayload(int windowId, ResourceLocation menuType, net.minecraft.network.chat.Component title, FriendlyByteBuf additionalData) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<AdvancedOpenScreenPayload> TYPE =
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("neoforge", "open_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AdvancedOpenScreenPayload> STREAM_CODEC =
        StreamCodec.of(AdvancedOpenScreenPayload::encode, AdvancedOpenScreenPayload::decode);

    private static void encode(RegistryFriendlyByteBuf buf, AdvancedOpenScreenPayload payload) {
        buf.writeVarInt(payload.windowId);
        buf.writeResourceLocation(payload.menuType);
        ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, payload.title);
        // Write additional data as raw bytes
        if (payload.additionalData != null && payload.additionalData.readableBytes() > 0) {
            buf.writeBytes(payload.additionalData.copy());
        }
    }

    private static AdvancedOpenScreenPayload decode(RegistryFriendlyByteBuf buf) {
        int windowId = buf.readVarInt();
        ResourceLocation menuType = buf.readResourceLocation();
        net.minecraft.network.chat.Component title = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf);
        FriendlyByteBuf additionalData = new FriendlyByteBuf(buf.readBytes(buf.readableBytes()));
        return new AdvancedOpenScreenPayload(windowId, menuType, title, additionalData);
    }

    @Override
    public CustomPacketPayload.Type<AdvancedOpenScreenPayload> type() {
        return TYPE;
    }
}

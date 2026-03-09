package net.neoforged.neoforge.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

/**
 * Generic NeoForge config-file payload used for server config synchronization.
 */
public record ConfigFilePayload(String fileName, byte[] contents) implements CustomPacketPayload {
    public static final Type<ConfigFilePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("neoforge", "config_file"));

    public static final StreamCodec<FriendlyByteBuf, ConfigFilePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            ConfigFilePayload::fileName,
            NeoForgeStreamCodecs.UNBOUNDED_BYTE_ARRAY,
            ConfigFilePayload::contents,
            ConfigFilePayload::new);

    @Override
    public Type<ConfigFilePayload> type() {
        return TYPE;
    }
}
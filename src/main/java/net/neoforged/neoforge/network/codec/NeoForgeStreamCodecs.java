package net.neoforged.neoforge.network.codec;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * NeoForge-specific stream codecs used by payload shims.
 */
public class NeoForgeStreamCodecs {
    public static final StreamCodec<FriendlyByteBuf, byte[]> UNBOUNDED_BYTE_ARRAY = new StreamCodec<>() {
        @Override
        public byte[] decode(FriendlyByteBuf buffer) {
            return buffer.readByteArray();
        }

        @Override
        public void encode(FriendlyByteBuf buffer, byte[] value) {
            buffer.writeByteArray(value);
        }
    };

    private NeoForgeStreamCodecs() {}
}

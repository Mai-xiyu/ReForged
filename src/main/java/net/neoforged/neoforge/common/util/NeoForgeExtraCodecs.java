package net.neoforged.neoforge.common.util;

/** Proxy: NeoForge's NeoForgeExtraCodecs */
public final class NeoForgeExtraCodecs {
    private NeoForgeExtraCodecs() {}

    /**
     * Creates a codec for a Set.
     * NeoForge likely implements this as listOf(codec).xmap(Set::copyOf, List::copyOf)
     */
    public static <E> com.mojang.serialization.Codec<java.util.Set<E>> setOf(com.mojang.serialization.Codec<E> elementCodec) {
        return elementCodec.listOf().xmap(java.util.HashSet::new, java.util.ArrayList::new);
    }
}

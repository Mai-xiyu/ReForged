package net.neoforged.neoforge.registries;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a data map type that can be attached to registry entries.
 */
public class DataMapType<R, T> {
    private final ResourceKey<Registry<R>> registryKey;
    private final ResourceLocation id;
    private final Codec<T> codec;
    @Nullable
    private final Codec<T> networkCodec;
    private final boolean mandatorySync;

    protected DataMapType(ResourceKey<Registry<R>> registryKey, ResourceLocation id, Codec<T> codec,
                          @Nullable Codec<T> networkCodec, boolean mandatorySync) {
        this.registryKey = registryKey;
        this.id = id;
        this.codec = codec;
        this.networkCodec = networkCodec;
        this.mandatorySync = mandatorySync;
    }

    public ResourceKey<Registry<R>> registryKey() {
        return registryKey;
    }

    public ResourceLocation id() {
        return id;
    }

    public Codec<T> codec() {
        return codec;
    }

    @Nullable
    public Codec<T> networkCodec() {
        return networkCodec;
    }

    public boolean mandatorySync() {
        return mandatorySync;
    }

    public static <T, R> Builder<T, R> builder(ResourceLocation id, ResourceKey<Registry<R>> registry, Codec<T> codec) {
        return new Builder<>(id, registry, codec);
    }

    public static class Builder<T, R> {
        private final ResourceLocation id;
        private final ResourceKey<Registry<R>> registryKey;
        private final Codec<T> codec;
        @Nullable
        private Codec<T> networkCodec;
        private boolean mandatorySync;

        private Builder(ResourceLocation id, ResourceKey<Registry<R>> registryKey, Codec<T> codec) {
            this.id = id;
            this.registryKey = registryKey;
            this.codec = codec;
        }

        public Builder<T, R> synced(Codec<T> networkCodec, boolean mandatorySync) {
            this.networkCodec = networkCodec;
            this.mandatorySync = mandatorySync;
            return this;
        }

        public DataMapType<R, T> build() {
            return new DataMapType<>(registryKey, id, codec, networkCodec, mandatorySync);
        }
    }
}

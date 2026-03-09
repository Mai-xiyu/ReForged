package net.neoforged.neoforge.attachment;

import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Supplier;

/** Proxy: NeoForge's AttachmentType for entity data attachments */
public class AttachmentType<T> {
    final Function<IAttachmentHolder, T> defaultValueSupplier;
    @Nullable
    final IAttachmentSerializer<?, T> serializer;
    final boolean copyOnDeath;
    @Nullable
    final IAttachmentCopyHandler<T> copyHandler;
    @Nullable
    final AttachmentSyncHandler<T> syncHandler;

    private AttachmentType(Builder<T> builder) {
        this.defaultValueSupplier = builder.defaultValueSupplier;
        this.serializer = builder.serializer;
        this.copyOnDeath = builder.copyOnDeath;
        this.copyHandler = builder.copyHandler != null ? builder.copyHandler : (attachment, holder, provider) -> attachment;
        this.syncHandler = builder.syncHandler;
    }

    public Supplier<T> defaultValueSupplier() { return () -> defaultValueSupplier.apply(null); }

    public T createDefaultValue(IAttachmentHolder holder) {
        return defaultValueSupplier.apply(holder);
    }

    public boolean copyOnDeath() {
        return copyOnDeath;
    }

    public static <T> Builder<T> builder(Supplier<T> defaultValue) {
        return new Builder<>(holder -> defaultValue.get());
    }

    public static <T> Builder<T> builder(Function<IAttachmentHolder, T> defaultValueConstructor) {
        return new Builder<>(defaultValueConstructor);
    }

    /** NeoForge: serializable(Supplier) — creates a builder for INBTSerializable attachment types */
    public static <S, T> Builder<T> serializable(Supplier<T> defaultValueSupplier) {
        return serializable(holder -> defaultValueSupplier.get());
    }

    /** NeoForge: serializable(Function) — creates a builder capturing the holder reference */
    public static <S, T> Builder<T> serializable(Function<IAttachmentHolder, T> defaultValueConstructor) {
        return builder(defaultValueConstructor).serialize(new IAttachmentSerializer<S, T>() {
            @Override
            public T read(IAttachmentHolder holder, S serializedData, HolderLookup.Provider provider) {
                return defaultValueConstructor.apply(holder);
            }

            @Override
            public S write(T data, HolderLookup.Provider provider) {
                return null;
            }
        });
    }

    public static class Builder<T> {
        private final Function<IAttachmentHolder, T> defaultValueSupplier;
        @Nullable
        private IAttachmentSerializer<?, T> serializer;
        private boolean copyOnDeath;
        @Nullable
        private IAttachmentCopyHandler<T> copyHandler;
        @Nullable
        private AttachmentSyncHandler<T> syncHandler;

        public Builder(Function<IAttachmentHolder, T> defaultValueSupplier) {
            this.defaultValueSupplier = defaultValueSupplier;
        }

        @SuppressWarnings("unchecked")
        public Builder<T> serialize(Object serializer) {
            if (serializer instanceof IAttachmentSerializer<?, ?> attachmentSerializer) {
                this.serializer = (IAttachmentSerializer<?, T>) attachmentSerializer;
            }
            return this;
        }

        public Builder<T> copyOnDeath() {
            this.copyOnDeath = true;
            return this;
        }

        @SuppressWarnings("unchecked")
        public Builder<T> copyHandler(Object handler) {
            if (handler instanceof IAttachmentCopyHandler<?> copyHandler) {
                this.copyHandler = (IAttachmentCopyHandler<T>) copyHandler;
            }
            return this;
        }

        public Builder<T> sync(AttachmentSyncHandler<T> syncHandler) {
            this.syncHandler = Objects.requireNonNull(syncHandler);
            return this;
        }

        public Builder<T> sync(Object streamCodec) {
            return this;
        }

        public Builder<T> sync(BiPredicate<IAttachmentHolder, net.minecraft.server.level.ServerPlayer> sendToPlayer, Object streamCodec) {
            return this;
        }

        public AttachmentType<T> build() {
            return new AttachmentType<>(this);
        }
    }
}

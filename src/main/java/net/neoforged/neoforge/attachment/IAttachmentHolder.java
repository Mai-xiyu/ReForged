package net.neoforged.neoforge.attachment;

import java.util.Optional;
import java.util.function.Supplier;
import org.jetbrains.annotations.Nullable;

/** Proxy: NeoForge's IAttachmentHolder — marks objects that can hold data attachments */
public interface IAttachmentHolder {
    boolean hasAttachments();

    boolean hasData(AttachmentType<?> type);

    <T> T getData(AttachmentType<T> type);

    default <T> Optional<T> getExistingData(AttachmentType<T> type) {
        return Optional.ofNullable(getExistingDataOrNull(type));
    }

    @Nullable
    default <T> T getExistingDataOrNull(AttachmentType<T> type) {
        return getExistingData(type).orElse(null);
    }

    <T> @Nullable T setData(AttachmentType<T> type, T value);

    <T> @Nullable T removeData(AttachmentType<T> type);

    default void syncData(AttachmentType<?> type) {}

    // Supplier overloads used by NeoForge mods
    default <T> T getData(Supplier<AttachmentType<T>> type) { return getData(type.get()); }
    default <T> boolean hasData(Supplier<AttachmentType<T>> type) { return hasData(type.get()); }
    default <T> T setData(Supplier<AttachmentType<T>> type, T value) { return setData(type.get(), value); }
    default <T> T removeData(Supplier<AttachmentType<T>> type) { return removeData(type.get()); }
    default <T> Optional<T> getExistingData(Supplier<AttachmentType<T>> type) { return getExistingData(type.get()); }
    @Nullable
    default <T> T getExistingDataOrNull(Supplier<AttachmentType<T>> type) { return getExistingDataOrNull(type.get()); }
    default void syncData(Supplier<? extends AttachmentType<?>> type) { syncData(type.get()); }
}

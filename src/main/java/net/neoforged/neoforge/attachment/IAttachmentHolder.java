package net.neoforged.neoforge.attachment;

/** Proxy: NeoForge's IAttachmentHolder — marks objects that can hold data attachments */
public interface IAttachmentHolder {
    default <T> T getData(AttachmentType<T> type) { return type.defaultValueSupplier().get(); }
    default <T> boolean hasData(AttachmentType<T> type) { return false; }
    default <T> T setData(AttachmentType<T> type, T value) { return value; }
    default <T> T removeData(AttachmentType<T> type) { return type.defaultValueSupplier().get(); }
}

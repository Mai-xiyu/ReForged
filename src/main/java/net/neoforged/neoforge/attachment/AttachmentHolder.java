package net.neoforged.neoforge.attachment;

import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public abstract class AttachmentHolder implements IAttachmentHolder {
    @Nullable
    private Map<AttachmentType<?>, Object> attachments;

    final Map<AttachmentType<?>, Object> getAttachmentMap() {
        if (attachments == null) {
            attachments = new IdentityHashMap<>(4);
        }
        return attachments;
    }

    IAttachmentHolder getExposedHolder() {
        return this;
    }

    @Override
    public final boolean hasAttachments() {
        return attachments != null && !attachments.isEmpty();
    }

    @Override
    public final boolean hasData(AttachmentType<?> type) {
        return attachments != null && attachments.containsKey(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final <T> T getData(AttachmentType<T> type) {
        T current = attachments == null ? null : (T) attachments.get(type);
        if (current == null) {
            current = type.createDefaultValue(getExposedHolder());
            getAttachmentMap().put(type, current);
        }
        return current;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getExistingDataOrNull(AttachmentType<T> type) {
        if (attachments == null) {
            return null;
        }
        return (T) attachments.get(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T setData(AttachmentType<T> type, T data) {
        Objects.requireNonNull(data);
        T previous = (T) getAttachmentMap().put(type, data);
        syncData(type);
        return previous;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T removeData(AttachmentType<T> type) {
        if (attachments == null) {
            return null;
        }
        T previous = (T) attachments.remove(type);
        syncData(type);
        return previous;
    }

    @SuppressWarnings("unchecked")
    final void copyAttachmentsTo(HolderLookup.Provider provider, AttachmentHolder to, Predicate<AttachmentType<?>> filter) {
        if (attachments == null) {
            return;
        }

        for (Map.Entry<AttachmentType<?>, Object> entry : attachments.entrySet()) {
            AttachmentType<?> type = entry.getKey();
            if (!filter.test(type)) {
                continue;
            }

            Object copy = entry.getValue();
            if (type.copyHandler != null) {
                copy = ((IAttachmentCopyHandler<Object>) type.copyHandler).copy(entry.getValue(), to.getExposedHolder(), provider);
            }

            if (copy != null) {
                to.getAttachmentMap().put(type, copy);
            }
        }
    }

    public static class AsField extends AttachmentHolder {
        private final IAttachmentHolder exposedHolder;

        public AsField(IAttachmentHolder exposedHolder) {
            this.exposedHolder = exposedHolder;
        }

        @Override
        IAttachmentHolder getExposedHolder() {
            return exposedHolder;
        }

        @Override
        public void syncData(AttachmentType<?> type) {
            exposedHolder.syncData(type);
        }
    }
}
package org.xiyu.reforged.shim.attachment;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * IAttachmentHolder — Shim for NeoForge's Data Attachment system.
 *
 * <h3>NeoForge API</h3>
 * <p>NeoForge 1.21.1 replaced Forge's Capability system with "Data Attachments":</p>
 * <pre>
 * // Attach data to an entity
 * entity.getData(MY_ATTACHMENT);
 * entity.setData(MY_ATTACHMENT, newValue);
 * entity.hasData(MY_ATTACHMENT);
 * </pre>
 *
 * <h3>Bridge Strategy</h3>
 * <p>Store attachment data in a per-holder map. For common types like IItemHandler,
 * IFluidHandler, etc., bridge to Forge's Capability system at runtime.</p>
 */
public interface IAttachmentHolder {

    /**
     * Get attached data. Returns the default value if not set.
     */
    default <T> T getData(AttachmentType<T> type) {
        return AttachmentManager.getData(this, type);
    }

    /**
     * Set attached data.
     */
    default <T> T setData(AttachmentType<T> type, T value) {
        return AttachmentManager.setData(this, type, value);
    }

    /**
     * Check if data is attached.
     */
    default <T> boolean hasData(AttachmentType<T> type) {
        return AttachmentManager.hasData(this, type);
    }

    /**
     * Remove attached data.
     */
    default <T> T removeData(AttachmentType<T> type) {
        return AttachmentManager.removeData(this, type);
    }

    // ─── Internal data manager ─────────────────────────────────

    /**
     * Centralized attachment data storage.
     * Uses a WeakHashMap-like approach keyed by holder identity + attachment type.
     */
    final class AttachmentManager {
        private static final Logger LOGGER = LogUtils.getLogger();
        private static final Map<Integer, Map<AttachmentType<?>, Object>> STORAGE = new ConcurrentHashMap<>();

        @SuppressWarnings("unchecked")
        static <T> T getData(IAttachmentHolder holder, AttachmentType<T> type) {
            Map<AttachmentType<?>, Object> holderData = STORAGE.get(System.identityHashCode(holder));
            if (holderData != null && holderData.containsKey(type)) {
                return (T) holderData.get(type);
            }
            // Return default value
            return type.defaultValueSupplier() != null ? type.defaultValueSupplier().get() : null;
        }

        @SuppressWarnings("unchecked")
        static <T> T setData(IAttachmentHolder holder, AttachmentType<T> type, T value) {
            Map<AttachmentType<?>, Object> holderData =
                    STORAGE.computeIfAbsent(System.identityHashCode(holder), k -> new ConcurrentHashMap<>());
            T old = (T) holderData.put(type, value);
            LOGGER.debug("[ReForged] AttachmentManager: set {} on holder {}", type.id(), holder.getClass().getSimpleName());
            return old;
        }

        static <T> boolean hasData(IAttachmentHolder holder, AttachmentType<T> type) {
            Map<AttachmentType<?>, Object> holderData = STORAGE.get(System.identityHashCode(holder));
            return holderData != null && holderData.containsKey(type);
        }

        @SuppressWarnings("unchecked")
        static <T> T removeData(IAttachmentHolder holder, AttachmentType<T> type) {
            Map<AttachmentType<?>, Object> holderData = STORAGE.get(System.identityHashCode(holder));
            if (holderData != null) {
                return (T) holderData.remove(type);
            }
            return null;
        }
    }
}

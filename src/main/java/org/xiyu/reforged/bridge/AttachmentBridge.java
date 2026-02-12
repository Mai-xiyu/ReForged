package org.xiyu.reforged.bridge;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * AttachmentBridge — Stub for bridging NeoForge Data Attachments ↔ Forge Capabilities.
 *
 * <h3>Background</h3>
 * <p>NeoForge 1.21.1 uses a "Data Attachment" system for attaching custom data to
 * entities, block entities, chunks, and levels. Forge 1.21.1 uses the older
 * "Capability" system ({@code ICapabilityProvider}, {@code LazyOptional}, etc.).</p>
 *
 * <p>The full bridge would need to:
 * <ol>
 *     <li>Intercept NeoForge {@code AttachmentType} registrations</li>
 *     <li>Create corresponding Forge Capability instances</li>
 *     <li>Wrap {@code LazyOptional<Cap>} as the NeoForge attachment data</li>
 *     <li>Sync changes bidirectionally</li>
 * </ol>
 *
 * <p><b>Skeleton phase:</b> This class is a stub. All methods log warnings and
 * return empty optionals. This documents the intended API contract for future
 * implementation.</p>
 */
public final class AttachmentBridge {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Registered attachment types (NeoForge attachment name → our internal tracker).
     */
    private static final Map<String, AttachmentEntry> attachments = new HashMap<>();

    /**
     * Register a NeoForge-style data attachment type.
     *
     * @param attachmentId the attachment's resource location (e.g. "mymod:my_data")
     * @param dataClass    the class of the attachment data
     * @param <T>          the data type
     * @return a handle for querying the attachment
     */
    public static <T> AttachmentHandle<T> registerAttachment(String attachmentId, Class<T> dataClass) {
        LOGGER.warn("[ReForged] AttachmentBridge: STUB — registerAttachment('{}', {}) — no Capability mapping yet",
                attachmentId, dataClass.getSimpleName());
        AttachmentEntry entry = new AttachmentEntry(attachmentId, dataClass);
        attachments.put(attachmentId, entry);
        return new AttachmentHandle<>(attachmentId, dataClass);
    }

    /**
     * Get an attachment value from a target object (entity, chunk, etc.).
     *
     * @param target       the object to query (Entity, LevelChunk, etc.)
     * @param handle       the attachment handle
     * @param <T>          the data type
     * @return the attachment data, or empty if not present
     */
    public static <T> Optional<T> getData(Object target, AttachmentHandle<T> handle) {
        LOGGER.warn("[ReForged] AttachmentBridge: STUB — getData() called for '{}' on {} — returning empty",
                handle.attachmentId(), target.getClass().getSimpleName());
        // TODO: Query Forge Capability providers on the target object and wrap the result
        return Optional.empty();
    }

    /**
     * Set an attachment value on a target object.
     *
     * @param target       the object to attach to
     * @param handle       the attachment handle
     * @param data         the data to attach
     * @param <T>          the data type
     */
    public static <T> void setData(Object target, AttachmentHandle<T> handle, T data) {
        LOGGER.warn("[ReForged] AttachmentBridge: STUB — setData() called for '{}' on {} — data discarded",
                handle.attachmentId(), target.getClass().getSimpleName());
        // TODO: Write into the corresponding Forge Capability on the target
    }

    // ─── Inner types ───────────────────────────────────────────────

    /**
     * Opaque handle for a registered attachment type.
     */
    public record AttachmentHandle<T>(String attachmentId, Class<T> dataClass) {}

    private record AttachmentEntry(String id, Class<?> dataClass) {}

    private AttachmentBridge() {}
}

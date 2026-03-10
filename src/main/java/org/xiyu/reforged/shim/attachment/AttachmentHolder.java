package org.xiyu.reforged.shim.attachment;

import com.mojang.logging.LogUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.slf4j.Logger;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * AttachmentHolder — Actual storage implementation for NeoForge's Data Attachment system.
 *
 * <p>This class holds an {@link IdentityHashMap} that maps {@link AttachmentType} to data values.
 * It replaces the previous global static map approach (which had identity hash collision and
 * memory leak issues).</p>
 *
 * <p>This class can be used in two ways:
 * <ol>
 *   <li>Extended by classes that need attachment support (rare in bridge context)</li>
 *   <li>Stored as a field via {@link AsField} in objects that cannot extend this class
 *       (the primary pattern — we inject this field via Mixin into Entity, LevelChunk, Level)</li>
 * </ol>
 */
public class AttachmentHolder implements IAttachmentHolder {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** The actual attachment data storage, lazily initialized. */
    private Map<AttachmentType<?>, Object> attachments = null;

    /**
     * Creates the attachment map if it does not yet exist.
     */
    final Map<AttachmentType<?>, Object> getAttachmentMap() {
        if (attachments == null) {
            attachments = new IdentityHashMap<>(4);
        }
        return attachments;
    }

    /**
     * Returns the holder that should be exposed to default value factories.
     * Override in {@link AsField} to return the actual holder (e.g., Entity).
     */
    IAttachmentHolder getExposedHolder() {
        return this;
    }

    @Override
    public boolean hasAttachments() {
        return attachments != null && !attachments.isEmpty();
    }

    @Override
    public <T> boolean hasData(AttachmentType<T> type) {
        Objects.requireNonNull(type);
        return attachments != null && attachments.containsKey(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getData(AttachmentType<T> type) {
        Objects.requireNonNull(type);
        T ret = (T) getAttachmentMap().get(type);
        if (ret == null) {
            ret = type.defaultValueSupplier() != null ? type.defaultValueSupplier().get() : null;
            if (ret != null) {
                attachments.put(type, ret);
            }
        }
        return ret;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T setData(AttachmentType<T> type, T value) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(value);
        return (T) getAttachmentMap().put(type, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T removeData(AttachmentType<T> type) {
        Objects.requireNonNull(type);
        if (attachments == null) {
            return null;
        }
        return (T) attachments.remove(type);
    }

    @Override
    public <T> Optional<T> getExistingData(AttachmentType<T> type) {
        if (!hasData(type)) {
            return Optional.empty();
        }
        return Optional.ofNullable(getData(type));
    }

    /**
     * Serializes all serializable attachments to NBT.
     * Uses each AttachmentType's Codec to encode values.
     */
    @SuppressWarnings("unchecked")
    public CompoundTag serializeAttachments(HolderLookup.Provider provider) {
        if (attachments == null || attachments.isEmpty()) {
            return null;
        }
        CompoundTag tag = new CompoundTag();
        for (var entry : attachments.entrySet()) {
            AttachmentType<?> type = entry.getKey();
            if (type.shouldSerialize() && type.id() != null && type.codec() != null) {
                try {
                    com.mojang.serialization.Codec<Object> codec = (com.mojang.serialization.Codec<Object>) type.codec();
                    var ops = provider.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);
                    var result = codec.encodeStart(ops, entry.getValue());
                    result.resultOrPartial(err ->
                            LOGGER.warn("[ReForged] Failed to serialize attachment '{}': {}", type.id(), err)
                    ).ifPresent(nbt -> tag.put(type.id(), nbt));
                } catch (Exception e) {
                    LOGGER.warn("[ReForged] Error serializing attachment '{}': {}", type.id(), e.getMessage());
                }
            }
        }
        return tag.isEmpty() ? null : tag;
    }

    /**
     * Deserializes attachments from NBT.
     * Requires an attachment type registry to resolve types by ID. Currently limited
     * to types that have been registered via the DeferredRegister system.
     */
    public void deserializeAttachments(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag == null || tag.isEmpty()) return;
        LOGGER.debug("[ReForged] deserializeAttachments called with {} keys", tag.getAllKeys().size());
        // Look up registered attachment types (they should be registered via NeoForge's DeferredRegister)
        for (String key : tag.getAllKeys()) {
            AttachmentType<?> type = AttachmentTypeRegistry.getById(key);
            if (type != null && type.codec() != null) {
                try {
                    var ops = provider.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);
                    var result = type.codec().parse(ops, tag.get(key));
                    result.resultOrPartial(err ->
                            LOGGER.warn("[ReForged] Failed to deserialize attachment '{}': {}", key, err)
                    ).ifPresent(value -> getAttachmentMap().put(type, value));
                } catch (Exception e) {
                    LOGGER.warn("[ReForged] Error deserializing attachment '{}': {}", key, e.getMessage());
                }
            } else {
                LOGGER.debug("[ReForged] Skipping deserialization for unknown attachment '{}'", key);
            }
        }
    }

    /**
     * Version of {@link AttachmentHolder} that can be stored as a field.
     * This is used when the class cannot extend AttachmentHolder directly.
     */
    public static class AsField extends AttachmentHolder {
        private final IAttachmentHolder exposedHolder;

        public AsField(IAttachmentHolder exposedHolder) {
            this.exposedHolder = exposedHolder;
        }

        @Override
        IAttachmentHolder getExposedHolder() {
            return exposedHolder;
        }

        public void deserializeInternal(HolderLookup.Provider provider, CompoundTag tag) {
            deserializeAttachments(provider, tag);
        }
    }
}

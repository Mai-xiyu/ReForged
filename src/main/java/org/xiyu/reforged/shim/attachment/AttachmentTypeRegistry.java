package org.xiyu.reforged.shim.attachment;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * DeferredRegister for attachment types — NeoForge-specific registry.
 *
 * <p>NeoForge mods use {@code DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID)}
 * to register attachment types. This shim captures those registrations.</p>
 */
public final class AttachmentTypeRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Global map of registered attachment types by id (namespace:name). */
    private static final ConcurrentHashMap<String, AttachmentType<?>> TYPES = new ConcurrentHashMap<>();

    /**
     * Look up a registered attachment type by its full id (e.g., "mymod:my_data").
     * Used during deserialization.
     */
    @Nullable
    public static AttachmentType<?> getById(String id) {
        return TYPES.get(id);
    }

    /**
     * Create a deferred register for attachment types.
     * This is a factory method that NeoForge mods call.
     */
    public static DeferredAttachmentRegister create(String modId) {
        return new DeferredAttachmentRegister(modId);
    }

    /**
     * Shim DeferredRegister specifically for attachment types.
     * Captures registrations and makes them available to the AttachmentManager.
     */
    public static final class DeferredAttachmentRegister {
        private final String modId;

        DeferredAttachmentRegister(String modId) {
            this.modId = modId;
            LOGGER.debug("[ReForged] AttachmentTypeRegistry created for mod '{}'", modId);
        }

        /**
         * Register an attachment type.
         */
        public <T> Supplier<AttachmentType<T>> register(String name, Supplier<AttachmentType.Builder<T>> builderSupplier) {
            String fullId = modId + ":" + name;
            AttachmentType<T> type = builderSupplier.get().id(fullId).build();
            TYPES.put(fullId, type);
            LOGGER.info("[ReForged] Registered attachment type: {}", fullId);
            return () -> type;
        }

        /**
         * Attach to mod event bus (no-op for attachments, but NeoForge mods call this).
         */
        public void register(Object modEventBus) {
            LOGGER.debug("[ReForged] AttachmentTypeRegistry.register(bus) called for mod '{}'", modId);
        }
    }

    private AttachmentTypeRegistry() {}
}

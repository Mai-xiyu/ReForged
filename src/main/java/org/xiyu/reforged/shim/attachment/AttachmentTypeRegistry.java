package org.xiyu.reforged.shim.attachment;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.function.Supplier;

/**
 * DeferredRegister for attachment types — NeoForge-specific registry.
 *
 * <p>NeoForge mods use {@code DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MODID)}
 * to register attachment types. This shim captures those registrations.</p>
 */
public final class AttachmentTypeRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();

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
            AttachmentType<T> type = builderSupplier.get().build();
            LOGGER.info("[ReForged] Registered attachment type: {}:{}", modId, name);
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

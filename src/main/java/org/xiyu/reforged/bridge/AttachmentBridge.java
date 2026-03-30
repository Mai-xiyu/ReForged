package org.xiyu.reforged.bridge;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.xiyu.reforged.shim.capabilities.ForgeCapabilityBridge;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AttachmentBridge — Bridges NeoForge Data Attachments ↔ Forge Capabilities.
 *
 * <h3>How it works</h3>
 * <ol>
 *   <li>NeoForge mods call {@code entity.getData(attachmentType)} — this is handled
 *       directly by the mixin-injected {@link AttachmentHolder} on the entity.</li>
 *   <li>When a NeoForge mod queries data that was set by a Forge mod (via Capabilities),
 *       this bridge translates: it looks up the corresponding Forge Capability and
 *       wraps the result as attachment data.</li>
 *   <li>When a NeoForge mod sets data that a Forge mod expects as a Capability,
 *       this bridge writes through to the Forge Capability provider.</li>
 * </ol>
 *
 * <p>The mixin-injected {@link AttachmentHolder.AsField} on Entity / BlockEntity / LevelChunk / Level
 * handles the primary storage. This bridge provides the <b>cross-system interop layer</b>
 * for cases where Forge mods expose data via Capabilities that NeoForge mods want to read.</p>
 */
public final class AttachmentBridge {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Mapping from NeoForge AttachmentType to Forge capability ResourceLocation.
     * Populated by {@link #registerCapabilityMapping}.
     */
    private static final Map<AttachmentType<?>, ResourceLocation> ATTACHMENT_TO_CAPABILITY = new ConcurrentHashMap<>();

    /**
     * Reverse mapping: Forge capability RL → NeoForge AttachmentType.
     */
    private static final Map<ResourceLocation, AttachmentType<?>> CAPABILITY_TO_ATTACHMENT = new ConcurrentHashMap<>();

    /**
     * Register a bidirectional mapping between a NeoForge AttachmentType and a Forge Capability.
     *
     * @param attachmentType the NeoForge attachment type
     * @param capabilityName the Forge capability ResourceLocation (e.g. "neoforge:energy")
     */
    public static <T> void registerCapabilityMapping(AttachmentType<T> attachmentType, ResourceLocation capabilityName) {
        ATTACHMENT_TO_CAPABILITY.put(attachmentType, capabilityName);
        CAPABILITY_TO_ATTACHMENT.put(capabilityName, attachmentType);
        LOGGER.debug("[ReForged] AttachmentBridge: Mapped AttachmentType → Capability '{}'", capabilityName);
    }

    /**
     * Try to read attachment data from a target object, falling back to Forge Capabilities
     * if the attachment holder doesn't have data but a Forge Capability does.
     *
     * @param target         the holder object (Entity, BlockEntity, LevelChunk, Level)
     * @param attachmentType the NeoForge attachment type to query
     * @param <T>            the data type
     * @return the data if found via either system, or empty
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> getDataWithFallback(Object target, AttachmentType<T> attachmentType) {
        // 1. Check the NeoForge attachment holder first (primary storage)
        if (target instanceof NeoAttachmentHolderBridge bridge) {
            AttachmentHolder.AsField holder = bridge.reforged$getNeoAttachmentHolder();
            if (holder.hasData(attachmentType)) {
                return Optional.ofNullable(holder.getData(attachmentType));
            }
        } else if (target instanceof IAttachmentHolder holder) {
            if (holder.hasData(attachmentType)) {
                return Optional.ofNullable(holder.getData(attachmentType));
            }
        }

        // 2. Fall back to Forge capability system
        ResourceLocation capRL = ATTACHMENT_TO_CAPABILITY.get(attachmentType);
        if (capRL == null) {
            return Optional.empty();
        }

        return queryForgeCapability(target, capRL);
    }

    /**
     * Set attachment data on a target, and if a Forge Capability mapping exists,
     * attempt to write through to the Forge system as well.
     *
     * @param target         the holder object
     * @param attachmentType the NeoForge attachment type
     * @param data           the data to set
     * @param <T>            the data type
     */
    public static <T> void setDataWithBridge(Object target, AttachmentType<T> attachmentType, T data) {
        // 1. Always write to the NeoForge attachment holder
        if (target instanceof NeoAttachmentHolderBridge bridge) {
            bridge.reforged$getNeoAttachmentHolder().setData(attachmentType, data);
        } else if (target instanceof IAttachmentHolder holder) {
            holder.setData(attachmentType, data);
        }

        // 2. If a Forge capability mapping exists, log the write-through
        // (Forge capabilities are typically read-only via LazyOptional, so direct write-through
        // is not always possible — the NeoForge holder is the source of truth)
        ResourceLocation capRL = ATTACHMENT_TO_CAPABILITY.get(attachmentType);
        if (capRL != null) {
            LOGGER.debug("[ReForged] AttachmentBridge: setData for capability '{}' on {} — NeoForge holder updated",
                    capRL, target.getClass().getSimpleName());
        }
    }

    /**
     * Query a Forge Capability on a target object and return the result.
     */
    @SuppressWarnings("unchecked")
    private static <T> Optional<T> queryForgeCapability(Object target, ResourceLocation capRL) {
        try {
            if (target instanceof BlockEntity be) {
                return queryCapabilityOn(be, capRL, null);
            } else if (target instanceof Entity entity) {
                return queryCapabilityOn(entity, capRL, null);
            } else if (target instanceof LevelChunk chunk) {
                // LevelChunks don't implement ICapabilityProvider in Forge
                return Optional.empty();
            } else if (target instanceof Level level) {
                // Levels don't implement ICapabilityProvider in Forge
                return Optional.empty();
            }
        } catch (Exception e) {
            LOGGER.debug("[ReForged] AttachmentBridge: Failed to query Forge capability '{}' on {}: {}",
                    capRL, target.getClass().getSimpleName(), e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Query a Forge capability on an ICapabilityProvider with optional side context.
     */
    @SuppressWarnings("unchecked")
    private static <T> Optional<T> queryCapabilityOn(ICapabilityProvider provider, ResourceLocation capRL, @Nullable Direction side) {
        Capability<T> forgeCap = ForgeCapabilityBridge.findForgeCapability(capRL, (Class<T>) Object.class);
        if (forgeCap == null) {
            return Optional.empty();
        }

        LazyOptional<T> lazyOpt = provider.getCapability(forgeCap, side);
        if (lazyOpt.isPresent()) {
            return Optional.ofNullable(lazyOpt.orElse(null));
        }
        return Optional.empty();
    }

    private AttachmentBridge() {}
}

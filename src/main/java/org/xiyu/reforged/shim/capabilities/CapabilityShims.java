package org.xiyu.reforged.shim.capabilities;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Capability shims for NeoForge 1.21.1's new capability system.
 *
 * <h3>NeoForge's New Capability API</h3>
 * <p>NeoForge 1.21.1 replaced the old {@code ICapabilityProvider} with:</p>
 * <ul>
 *     <li>{@code BlockCapability.create(ResourceLocation, Class)} — block-level queries</li>
 *     <li>{@code EntityCapability.create(ResourceLocation, Class)} — entity-level queries</li>
 *     <li>{@code ItemCapability.create(ResourceLocation, Class)} — item-level queries</li>
 * </ul>
 *
 * <h3>Bridge Strategy</h3>
 * <p>We provide the same API surface, delegating to Forge's {@code ICapabilityProvider}
 * where possible, and using our own storage for types not in Forge.</p>
 */
public final class CapabilityShims {

    private static final Logger LOGGER = LogUtils.getLogger();

    // ─── BlockCapability ──────────────────────────────────────────

    public static final class BlockCapability<T, C> {
        private final ResourceLocation name;
        private final Class<T> typeClass;
        private final Class<C> contextClass;

        private static final Map<ResourceLocation, BlockCapability<?, ?>> REGISTERED = new ConcurrentHashMap<>();

        private BlockCapability(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
            this.name = name;
            this.typeClass = typeClass;
            this.contextClass = contextClass;
        }

        /**
         * NeoForge: {@code BlockCapability.create(rl, IItemHandler.class, Direction.class)}
         */
        @SuppressWarnings("unchecked")
        public static <T, C> BlockCapability<T, C> create(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
            return (BlockCapability<T, C>) REGISTERED.computeIfAbsent(name,
                    k -> new BlockCapability<>(name, typeClass, contextClass));
        }

        /**
         * NeoForge: {@code BlockCapability.createVoid(rl, IItemHandler.class)}
         */
        public static <T> BlockCapability<T, Void> createVoid(ResourceLocation name, Class<T> typeClass) {
            return create(name, typeClass, Void.class);
        }

        /**
         * NeoForge: {@code BlockCapability.createSided(rl, IItemHandler.class)}
         * Convenience for Direction-context capabilities.
         */
        public static <T> BlockCapability<T, Direction> createSided(ResourceLocation name, Class<T> typeClass) {
            return create(name, typeClass, Direction.class);
        }

        public ResourceLocation name() { return name; }
        public Class<T> typeClass() { return typeClass; }
        public Class<C> contextClass() { return contextClass; }

        /**
         * Query a block entity for this capability.
         * Bridges to Forge's capability system where possible.
         */
        @Nullable
        @SuppressWarnings("unchecked")
        public T getCapability(BlockEntity blockEntity, @Nullable C context) {
            if (blockEntity == null) return null;
            // Try Forge's capability system
            Direction side = (context instanceof Direction d) ? d : null;
            try {
                // Use reflection to find Forge capability match
                var forgeCap = ForgeCapabilityBridge.findForgeCapability(name, typeClass);
                if (forgeCap != null) {
                    var lazyOpt = blockEntity.getCapability(forgeCap, side);
                    if (lazyOpt.isPresent()) {
                        return (T) lazyOpt.orElse(null);
                    }
                }
            } catch (Exception e) {
                LOGGER.debug("[ReForged] BlockCapability.getCapability fallback for {}", name);
            }
            return null;
        }
    }

    // ─── EntityCapability ─────────────────────────────────────────

    public static final class EntityCapability<T, C> {
        private final ResourceLocation name;
        private final Class<T> typeClass;
        private final Class<C> contextClass;

        private static final Map<ResourceLocation, EntityCapability<?, ?>> REGISTERED = new ConcurrentHashMap<>();

        private EntityCapability(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
            this.name = name;
            this.typeClass = typeClass;
            this.contextClass = contextClass;
        }

        @SuppressWarnings("unchecked")
        public static <T, C> EntityCapability<T, C> create(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
            return (EntityCapability<T, C>) REGISTERED.computeIfAbsent(name,
                    k -> new EntityCapability<>(name, typeClass, contextClass));
        }

        public static <T> EntityCapability<T, Void> createVoid(ResourceLocation name, Class<T> typeClass) {
            return create(name, typeClass, Void.class);
        }

        public ResourceLocation name() { return name; }

        @Nullable
        @SuppressWarnings("unchecked")
        public T getCapability(Entity entity, @Nullable C context) {
            if (entity == null) return null;
            try {
                var forgeCap = ForgeCapabilityBridge.findForgeCapability(name, typeClass);
                if (forgeCap != null) {
                    var lazyOpt = entity.getCapability(forgeCap);
                    if (lazyOpt.isPresent()) {
                        return (T) lazyOpt.orElse(null);
                    }
                }
            } catch (Exception e) {
                LOGGER.debug("[ReForged] EntityCapability.getCapability fallback for {}", name);
            }
            return null;
        }
    }

    // ─── ItemCapability ───────────────────────────────────────────

    public static final class ItemCapability<T, C> {
        private final ResourceLocation name;
        private final Class<T> typeClass;
        private final Class<C> contextClass;

        private static final Map<ResourceLocation, ItemCapability<?, ?>> REGISTERED = new ConcurrentHashMap<>();

        private ItemCapability(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
            this.name = name;
            this.typeClass = typeClass;
            this.contextClass = contextClass;
        }

        @SuppressWarnings("unchecked")
        public static <T, C> ItemCapability<T, C> create(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
            return (ItemCapability<T, C>) REGISTERED.computeIfAbsent(name,
                    k -> new ItemCapability<>(name, typeClass, contextClass));
        }

        public static <T> ItemCapability<T, Void> createVoid(ResourceLocation name, Class<T> typeClass) {
            return create(name, typeClass, Void.class);
        }

        public ResourceLocation name() { return name; }

        @Nullable
        public T getCapability(ItemStack stack, @Nullable C context) {
            // ItemStack capabilities in Forge are handled differently
            // Bridge where possible via ForgeCapabilityBridge
            return null;
        }
    }

    // ─── Capabilities built-in constants ──────────────────────────
    // NeoForge defines these in net.neoforged.neoforge.capabilities.Capabilities

    public static final class Capabilities {
        public static final class ItemHandler {
            public static final BlockCapability<?, Direction> BLOCK =
                    BlockCapability.createSided(ResourceLocation.parse("neoforge:item_handler"), Object.class);
            public static final EntityCapability<?, Void> ENTITY =
                    EntityCapability.createVoid(ResourceLocation.parse("neoforge:item_handler"), Object.class);
            public static final ItemCapability<?, Void> ITEM =
                    ItemCapability.createVoid(ResourceLocation.parse("neoforge:item_handler"), Object.class);
        }

        public static final class FluidHandler {
            public static final BlockCapability<?, Direction> BLOCK =
                    BlockCapability.createSided(ResourceLocation.parse("neoforge:fluid_handler"), Object.class);
            public static final EntityCapability<?, Void> ENTITY =
                    EntityCapability.createVoid(ResourceLocation.parse("neoforge:fluid_handler"), Object.class);
            public static final ItemCapability<?, Void> ITEM =
                    ItemCapability.createVoid(ResourceLocation.parse("neoforge:fluid_handler"), Object.class);
        }

        public static final class EnergyStorage {
            public static final BlockCapability<?, Direction> BLOCK =
                    BlockCapability.createSided(ResourceLocation.parse("neoforge:energy"), Object.class);
            public static final EntityCapability<?, Void> ENTITY =
                    EntityCapability.createVoid(ResourceLocation.parse("neoforge:energy"), Object.class);
            public static final ItemCapability<?, Void> ITEM =
                    ItemCapability.createVoid(ResourceLocation.parse("neoforge:energy"), Object.class);
        }
    }

    private CapabilityShims() {}
}

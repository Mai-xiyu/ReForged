package net.neoforged.neoforge.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import net.neoforged.neoforge.items.wrapper.EntityArmorInvWrapper;
import net.neoforged.neoforge.items.wrapper.EntityHandsInvWrapper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.PlayerInvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.xiyu.reforged.core.NeoForgeModLoader;
import org.xiyu.reforged.bridge.ServerLevelCapabilityBridge;

public final class CapabilityHooks {
    private static boolean initialized = false;

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        RegisterCapabilitiesEvent event = new RegisterCapabilitiesEvent();
        NeoForgeModLoader.dispatchNeoForgeModEvent(event);
        registerVanillaProviders(event);
        registerFallbackVanillaProviders(event);
        MinecraftForge.EVENT_BUS.addListener(CapabilityHooks::invalidateCapsOnChunkLoad);
        MinecraftForge.EVENT_BUS.addListener(CapabilityHooks::invalidateCapsOnChunkUnload);
        MinecraftForge.EVENT_BUS.addListener(CapabilityHooks::cleanCapabilityListenerReferencesOnTick);
    }

    public static void registerVanillaProviders(RegisterCapabilitiesEvent event) {
        event.registerBlock(Capabilities.ItemHandler.BLOCK, (level, pos, state, blockEntity, side) -> {
            if (state == null || !(state.getBlock() instanceof ChestBlock chestBlock)) {
                return null;
            }
            Container container = ChestBlock.getContainer(chestBlock, state, level, pos, true);
            return container != null ? new InvWrapper(container) : null;
        }, Blocks.CHEST, Blocks.TRAPPED_CHEST);

        registerWorldlyBlockEntity(event, BlockEntityType.HOPPER);
        registerWorldlyBlockEntity(event, BlockEntityType.BLAST_FURNACE);
        registerWorldlyBlockEntity(event, BlockEntityType.BREWING_STAND);
        registerWorldlyBlockEntity(event, BlockEntityType.FURNACE);
        registerWorldlyBlockEntity(event, BlockEntityType.SMOKER);
        registerWorldlyBlockEntity(event, BlockEntityType.SHULKER_BOX);

        registerContainerBlockEntity(event, BlockEntityType.BARREL);
        registerContainerBlockEntity(event, BlockEntityType.CHISELED_BOOKSHELF);
        registerContainerBlockEntity(event, BlockEntityType.DISPENSER);
        registerContainerBlockEntity(event, BlockEntityType.DROPPER);
        registerContainerBlockEntity(event, BlockEntityType.JUKEBOX);
        registerContainerBlockEntity(event, BlockEntityType.CRAFTER);
        registerContainerBlockEntity(event, BlockEntityType.DECORATED_POT);

        event.registerEntity(Capabilities.ItemHandler.ENTITY, EntityType.PLAYER, (player, ctx) -> new PlayerInvWrapper(player.getInventory()));
    }

    public static void registerFallbackVanillaProviders(RegisterCapabilitiesEvent event) {
        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            event.registerEntity(Capabilities.ItemHandler.ENTITY, entityType, (entity, ctx) -> wrapEntityInventory(entity));
            event.registerEntity(Capabilities.ItemHandler.ENTITY_AUTOMATION, entityType, (entity, ctx) -> wrapEntityInventory(entity));
        }
    }

    private static <BE extends BlockEntity> void registerWorldlyBlockEntity(RegisterCapabilitiesEvent event, BlockEntityType<BE> type) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, (blockEntity, side) -> {
            if (blockEntity instanceof WorldlyContainer worldly) {
                return new SidedInvWrapper(worldly, side instanceof Direction direction ? direction : null);
            }
            if (blockEntity instanceof Container container) {
                return new InvWrapper(container);
            }
            return null;
        });
    }

    private static <BE extends BlockEntity> void registerContainerBlockEntity(RegisterCapabilitiesEvent event, BlockEntityType<BE> type) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, (blockEntity, side) -> {
            if (blockEntity instanceof Container container) {
                return new InvWrapper(container);
            }
            return null;
        });
    }

    private static net.neoforged.neoforge.items.IItemHandler wrapEntityInventory(Object entity) {
        if (entity instanceof Player player) {
            return new PlayerInvWrapper(player.getInventory());
        }
        if (entity instanceof Container container) {
            return new InvWrapper(container);
        }
        if (entity instanceof LivingEntity livingEntity) {
            return new CombinedInvWrapper(new EntityHandsInvWrapper(livingEntity), new EntityArmorInvWrapper(livingEntity));
        }
        return null;
    }

    public static void invalidateCapsOnChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && (Object) serverLevel instanceof ServerLevelCapabilityBridge bridge) {
            bridge.reforged$invalidateCapabilities(event.getChunk().getPos());
        }
    }

    public static void invalidateCapsOnChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && (Object) serverLevel instanceof ServerLevelCapabilityBridge bridge) {
            bridge.reforged$invalidateCapabilities(event.getChunk().getPos());
        }
    }

    public static void cleanCapabilityListenerReferencesOnTick(TickEvent.LevelTickEvent.Post event) {
        if (event.level instanceof ServerLevel serverLevel && (Object) serverLevel instanceof ServerLevelCapabilityBridge bridge) {
            bridge.reforged$cleanCapabilityListenerReferences();
        }
    }

    private CapabilityHooks() {
    }
}
package net.neoforged.neoforge.event;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Fired to allow mods to add extra valid blocks to existing block entity types.
 */
public class BlockEntityTypeAddBlocksEvent extends Event implements IModBusEvent {

    public BlockEntityTypeAddBlocksEvent() {}

    /**
     * Adds additional valid blocks to the given block entity type.
     */
    public void modify(BlockEntityType<?> blockEntityType, Block... blocksToAdd) {
        if (blocksToAdd.length == 0) return;
        try {
            java.lang.reflect.Field field = BlockEntityType.class.getDeclaredField("validBlocks");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            Set<Block> currentBlocks = (Set<Block>) field.get(blockEntityType);
            Set<Block> mutable = new HashSet<>(currentBlocks);
            for (Block b : blocksToAdd) mutable.add(b);
            field.set(blockEntityType, mutable);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to modify BlockEntityType valid blocks", e);
        }
    }

    public void modify(ResourceKey<BlockEntityType<?>> key, Block... blocksToAdd) {
        BuiltInRegistries.BLOCK_ENTITY_TYPE.getOptional(key)
                .ifPresent(bet -> modify(bet, blocksToAdd));
    }
}

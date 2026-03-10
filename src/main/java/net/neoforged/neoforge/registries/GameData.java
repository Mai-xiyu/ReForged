package net.neoforged.neoforge.registries;

import net.minecraft.core.IdMapper;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import java.util.Map;
import java.util.Collections;

/**
 * Central registry data management.
 */
public class GameData {

    @SuppressWarnings("unchecked")
    public static Map<Block, Item> getBlockItemMap() {
        return (Map<Block, Item>) (Map<?, ?>) Item.BY_BLOCK;
    }

    public static IdMapper<BlockState> getBlockStateIDMap() {
        return Block.BLOCK_STATE_REGISTRY;
    }

    private GameData() {}
}

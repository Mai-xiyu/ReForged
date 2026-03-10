package net.neoforged.neoforge.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for items that can be planted at a position.
 */
public interface SpecialPlantable {

    /**
     * Whether this item can be planted at the given position.
     */
    boolean canPlacePlantAtPosition(ItemStack stack, LevelReader level, BlockPos pos, @Nullable Direction direction);

    /**
     * Spawns the plant at the given position.
     */
    void spawnPlantAtPosition(ItemStack stack, LevelAccessor level, BlockPos pos, @Nullable Direction direction);

    /**
     * Whether a villager can plant this item.
     */
    default boolean villagerCanPlantItem(Villager villager) {
        return false;
    }
}

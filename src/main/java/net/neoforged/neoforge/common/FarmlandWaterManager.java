package net.neoforged.neoforge.common;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Manages custom water sources for farmland.
 * Handles custom AABBs that count as water sources for nearby farmland.
 */
public class FarmlandWaterManager {
    private FarmlandWaterManager() {}

    /**
     * Checks whether there is a custom water source within range of the given farmland position.
     */
    public static boolean hasBlockWaterTicket(Level level, BlockPos pos) {
        return net.minecraftforge.common.FarmlandWaterManager.hasBlockWaterTicket(level, pos);
    }
}

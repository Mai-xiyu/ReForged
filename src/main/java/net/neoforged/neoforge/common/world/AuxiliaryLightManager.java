package net.neoforged.neoforge.common.world;

import net.minecraft.core.BlockPos;

/**
 * Manages auxiliary light values for custom light sources.
 * Allows mods to add dynamic light levels at specific block positions.
 */
public interface AuxiliaryLightManager {
    /**
     * Gets the light level at the given position.
     * @return the light level (0-15), or 0 if no auxiliary light is set
     */
    int getLightAt(BlockPos pos);

    /**
     * Sets the light level at the given position.
     * @param pos   the block position
     * @param value the light level (0-15)
     */
    void setLightAt(BlockPos pos, int value);

    /**
     * Removes any auxiliary light at the given position.
     */
    void removeLightAt(BlockPos pos);
}

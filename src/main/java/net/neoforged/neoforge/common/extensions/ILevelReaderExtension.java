package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;

/**
 * Extension interface for {@link LevelReader}.
 */
public interface ILevelReaderExtension {

    private LevelReader self() { return (LevelReader) this; }

    /**
     * Checks if a cubic area around center is loaded.
     */
    default boolean isAreaLoaded(BlockPos center, int range) {
        return self().hasChunksAt(center.offset(-range, -range, -range), center.offset(range, range, range));
    }
}

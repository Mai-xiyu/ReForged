package net.neoforged.neoforge.common.extensions;

import com.mojang.datafixers.util.Either;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for objects that own a spawner (BaseSpawner, TrialSpawner).
 * The owner is either a BlockEntity or an Entity.
 */
public interface IOwnedSpawner {

    /**
     * Returns the block entity or entity that owns this spawner.
     */
    @Nullable
    Either<BlockEntity, Entity> getOwner();
}

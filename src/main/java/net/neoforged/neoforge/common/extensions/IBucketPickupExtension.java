package net.neoforged.neoforge.common.extensions;

import java.util.Optional;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Extension interface for BucketPickup.
 */
public interface IBucketPickupExtension {

    /**
     * Returns the pickup sound for the given block state.
     */
    default Optional<SoundEvent> getPickupSound(BlockState state) {
        return ((BucketPickup) this).getPickupSound();
    }
}

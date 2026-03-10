package net.neoforged.neoforge.common.extensions;

import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * Extension interface for Boat.
 */
public interface IBoatExtension {

    /**
     * Returns whether this boat can float on the given fluid state.
     */
    default boolean canBoatInFluid(FluidState state) {
        return state.getFluidType().supportsBoating((Boat) this);
    }

    /**
     * Returns whether this boat can float on the given fluid type.
     */
    default boolean canBoatInFluid(FluidType type) {
        return type.supportsBoating((Boat) this);
    }
}

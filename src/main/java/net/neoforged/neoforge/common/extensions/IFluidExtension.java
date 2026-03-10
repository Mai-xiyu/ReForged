package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * Extension interface for {@link Fluid}.
 */
public interface IFluidExtension {

    default FluidType getFluidType() {
        return new FluidType(FluidType.Properties.create());
    }

    default float getExplosionResistance(FluidState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return state.getExplosionResistance();
    }
}

package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * Extension interface for FluidState.
 * In the NeoForge shim, these are API stubs; FluidState is not actually mixed.
 */
public interface IFluidStateExtension {

    default FluidType getFluidType() {
        return new FluidType(FluidType.Properties.create());
    }

    default float getExplosionResistance(BlockGetter level, BlockPos pos, Explosion explosion) {
        return 0.0f;
    }
}

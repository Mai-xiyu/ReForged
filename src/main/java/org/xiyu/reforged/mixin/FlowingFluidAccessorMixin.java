package org.xiyu.reforged.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = FlowingFluid.class, remap = false)
public abstract class FlowingFluidAccessorMixin {
    @Shadow
    protected abstract FluidState getNewLiquid(Level level, BlockPos pos, BlockState state);

    public FluidState create$getNewLiquid(Level level, BlockPos pos, BlockState state) {
        return this.getNewLiquid(level, pos, state);
    }
}

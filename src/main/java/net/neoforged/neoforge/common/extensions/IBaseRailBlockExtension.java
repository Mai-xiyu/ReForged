package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.jetbrains.annotations.Nullable;

/**
 * Extension interface for BaseRailBlock.
 */
public interface IBaseRailBlockExtension {

    boolean isFlexibleRail(BlockState state, BlockGetter level, BlockPos pos);

    default boolean canMakeSlopes(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    RailShape getRailDirection(BlockState state, BlockGetter level, BlockPos pos, @Nullable AbstractMinecart cart);

    default float getRailMaxSpeed(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
        return 0.4f;
    }

    default void onMinecartPass(BlockState state, Level level, BlockPos pos, AbstractMinecart cart) {
    }

    default boolean isValidRailShape(RailShape shape) {
        return true;
    }
}

package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.extensions.IForgeBlockState;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.enums.BubbleColumnDirection;
import org.jetbrains.annotations.Nullable;

public interface IBlockStateExtension extends IForgeBlockState {

    default BlockState self() {
        return (BlockState) this;
    }

    default boolean hasDynamicLightEmission() {
        return ((IBlockExtension) self().getBlock()).hasDynamicLightEmission(self());
    }

    default boolean ignitedByLava(BlockGetter level, BlockPos pos, Direction face) {
        return ((IBlockExtension) self().getBlock()).ignitedByLava(self(), level, pos, face);
    }

    default void onDestroyedByPushReaction(Level level, BlockPos pos, Direction pushDirection, FluidState fluid) {
        ((IBlockExtension) self().getBlock()).onDestroyedByPushReaction(self(), level, pos, pushDirection, fluid);
    }

    @Nullable
    default BlockState getToolModifiedState(UseOnContext context, ItemAbility itemAbility, boolean simulate) {
        return ((IBlockExtension) self().getBlock()).getToolModifiedState(self(), context, itemAbility, simulate);
    }

    default boolean canRedstoneConnectTo(BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return self().getBlock().canConnectRedstone(self(), level, pos, direction);
    }

    default boolean supportsExternalFaceHiding() {
        return self().getBlock().supportsExternalFaceHiding(self());
    }

    default void onBlockStateChange(LevelReader level, BlockPos pos, BlockState oldState) {
        self().getBlock().onBlockStateChange(level, pos, oldState, self());
    }

    default boolean canBeHydrated(BlockGetter getter, BlockPos pos, FluidState fluid, BlockPos fluidPos) {
        return self().getBlock().canBeHydrated(self(), getter, pos, fluid, fluidPos);
    }

    default BlockState getAppearance(BlockAndTintGetter level, BlockPos pos, Direction side, @Nullable BlockState queryState, @Nullable BlockPos queryPos) {
        return self().getBlock().getAppearance(self(), level, pos, side, queryState, queryPos);
    }

    default boolean isEmpty() {
        return ((IBlockExtension) self().getBlock()).isEmpty(self());
    }

    default BubbleColumnDirection getBubbleColumnDirection() {
        return ((IBlockExtension) self().getBlock()).getBubbleColumnDirection(self());
    }

    default boolean shouldHideAdjacentFluidFace(Direction selfFace, FluidState adjacentFluid) {
        return ((IBlockExtension) self().getBlock()).shouldHideAdjacentFluidFace(self(), selfFace, adjacentFluid);
    }
}

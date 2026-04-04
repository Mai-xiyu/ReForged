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
import net.minecraft.world.level.block.Blocks;
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
        var block = self().getBlock();
        if (block instanceof IBlockExtension ext) {
            return ext.hasDynamicLightEmission(self());
        }
        return false;
    }

    default boolean ignitedByLava(BlockGetter level, BlockPos pos, Direction face) {
        var block = self().getBlock();
        if (block instanceof IBlockExtension ext) {
            return ext.ignitedByLava(self(), level, pos, face);
        }
        return self().ignitedByLava();
    }

    default void onDestroyedByPushReaction(Level level, BlockPos pos, Direction pushDirection, FluidState fluid) {
        var block = self().getBlock();
        if (block instanceof IBlockExtension ext) {
            ext.onDestroyedByPushReaction(self(), level, pos, pushDirection, fluid);
        } else {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    @Nullable
    default BlockState getToolModifiedState(UseOnContext context, ItemAbility itemAbility, boolean simulate) {
        var block = self().getBlock();
        if (block instanceof IBlockExtension ext) {
            return ext.getToolModifiedState(self(), context, itemAbility, simulate);
        }
        return null;
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
        var block = self().getBlock();
        if (block instanceof IBlockExtension ext) {
            return ext.isEmpty(self());
        }
        return self().is(Blocks.AIR) || self().is(Blocks.CAVE_AIR) || self().is(Blocks.VOID_AIR);
    }

    default BubbleColumnDirection getBubbleColumnDirection() {
        var block = self().getBlock();
        if (block instanceof IBlockExtension ext) {
            return ext.getBubbleColumnDirection(self());
        }
        if (self().is(Blocks.SOUL_SAND)) return BubbleColumnDirection.UPWARD;
        if (self().is(Blocks.MAGMA_BLOCK)) return BubbleColumnDirection.DOWNWARD;
        return BubbleColumnDirection.NONE;
    }

    default boolean shouldHideAdjacentFluidFace(Direction selfFace, FluidState adjacentFluid) {
        var block = self().getBlock();
        if (block instanceof IBlockExtension ext) {
            return ext.shouldHideAdjacentFluidFace(self(), selfFace, adjacentFluid);
        }
        return self().getFluidState().getType().isSame(adjacentFluid.getType());
    }
}

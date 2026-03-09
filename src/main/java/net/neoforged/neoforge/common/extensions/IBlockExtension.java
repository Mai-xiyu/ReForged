package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.extensions.IForgeBlock;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.enums.BubbleColumnDirection;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;

public interface IBlockExtension extends IForgeBlock {

	private Block self() {
		return (Block) this;
	}

	default boolean hasDynamicLightEmission(BlockState state) {
		return false;
	}

	default boolean ignitedByLava(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return state.ignitedByLava();
	}

	default boolean makesOpenTrapdoorAboveClimbable(BlockState state, LevelReader level, BlockPos pos, BlockState trapdoorState) {
		return state.getBlock() instanceof LadderBlock && state.getValue(LadderBlock.FACING) == trapdoorState.getValue(TrapDoorBlock.FACING);
	}

	default void onDestroyedByPushReaction(BlockState state, Level level, BlockPos pos, Direction pushDirection, FluidState fluid) {
		level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_CLIENTS);
		level.gameEvent(net.minecraft.world.level.gameevent.GameEvent.BLOCK_DESTROY, pos, net.minecraft.world.level.gameevent.GameEvent.Context.of(state));
	}

	@Nullable
	default BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
		BlockState eventState = EventHooks.onToolUse(state, context, itemAbility, simulate);
		return eventState != state ? eventState : self().getToolModifiedState(state, context, ToolAction.get(itemAbility.name()), simulate);
	}

	@Nullable
	default PushReaction getPistonPushReaction(BlockState state) {
		return null;
	}

	default boolean isEmpty(BlockState state) {
		return state.is(Blocks.AIR) || state.is(Blocks.CAVE_AIR) || state.is(Blocks.VOID_AIR);
	}

	default BubbleColumnDirection getBubbleColumnDirection(BlockState state) {
		if (state.is(Blocks.SOUL_SAND)) {
			return BubbleColumnDirection.UPWARD;
		}
		if (state.is(Blocks.MAGMA_BLOCK)) {
			return BubbleColumnDirection.DOWNWARD;
		}
		return BubbleColumnDirection.NONE;
	}

	default boolean shouldHideAdjacentFluidFace(BlockState state, Direction selfFace, FluidState adjacentFluid) {
		return state.getFluidState().getType().isSame(adjacentFluid.getType());
	}
}

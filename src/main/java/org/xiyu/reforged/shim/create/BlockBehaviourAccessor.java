package org.xiyu.reforged.shim.create;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Loader-stable replacement for Create's mixin accessor interface.
 */
public interface BlockBehaviourAccessor {
    VoxelShape create$getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context);
}

package org.xiyu.reforged.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.xiyu.reforged.shim.create.BlockBehaviourAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourAccessorMixin implements BlockBehaviourAccessor {

    @Shadow
    protected abstract VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context);

    @Override
    public VoxelShape create$getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Call this block's own getShape directly — do NOT go through state.getShape()
        // because that dispatches to the *state's* block (which may be a different block
        // that called create$getShape on us, causing infinite recursion).
        return this.getShape(state, level, pos, context);
    }
}
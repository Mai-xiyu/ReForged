package org.xiyu.reforged.mixin;

import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IBlockStateExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockState.class)
public abstract class BlockStateInjectedInterfaceMixin implements IBlockStateExtension {
}
package org.xiyu.reforged.mixin;

import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Block.class)
public abstract class BlockInjectedInterfaceMixin implements IBlockExtension {
}
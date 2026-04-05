package org.xiyu.reforged.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ConcretePowderBlock;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ConcretePowderBlock.class, remap = false)
public abstract class ConcretePowderBlockAccessorMixin {
    @Shadow @Final private Block concrete;

    public Block create$getConcrete() {
        return this.concrete;
    }
}

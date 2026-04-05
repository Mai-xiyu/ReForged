package org.xiyu.reforged.mixin;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = UseOnContext.class, remap = false)
public abstract class UseOnContextAccessorMixin {
    @Shadow @Final private BlockHitResult hitResult;

    public BlockHitResult create$getHitResult() {
        return this.hitResult;
    }
}

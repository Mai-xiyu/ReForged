package org.xiyu.reforged.mixin;

import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = DispenserBlock.class, remap = false)
public abstract class DispenserBlockAccessorMixin {
    @Shadow
    protected abstract DispenseItemBehavior getDispenseMethod(Level level, ItemStack stack);

    public DispenseItemBehavior create$callGetDispenseMethod(Level level, ItemStack stack) {
        return this.getDispenseMethod(level, stack);
    }
}

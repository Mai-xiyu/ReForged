package org.xiyu.reforged.mixin;

import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemFrame.class)
public abstract class ItemFrameAccessorMixin {
    @Shadow
    protected abstract ItemStack getFrameItemStack();

    public ItemStack create$getFrameItemStack() {
        return this.getFrameItemStack();
    }
}

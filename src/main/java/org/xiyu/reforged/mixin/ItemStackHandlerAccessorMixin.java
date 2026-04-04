package org.xiyu.reforged.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ItemStackHandler.class, remap = false)
public abstract class ItemStackHandlerAccessorMixin {
    @Shadow protected NonNullList<ItemStack> stacks;

    public NonNullList<ItemStack> create$getStacks() {
        return this.stacks;
    }
}

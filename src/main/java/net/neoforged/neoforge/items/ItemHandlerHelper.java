package net.neoforged.neoforge.items;

import net.minecraft.world.item.ItemStack;

/** Proxy: NeoForge's ItemHandlerHelper */
public final class ItemHandlerHelper {
    private ItemHandlerHelper() {}

    public static ItemStack copyStackWithSize(ItemStack stack, int size) {
        if (size == 0) return ItemStack.EMPTY;
        ItemStack copy = stack.copy();
        copy.setCount(size);
        return copy;
    }
}

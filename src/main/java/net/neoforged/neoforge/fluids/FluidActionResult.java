package net.neoforged.neoforge.fluids;

import net.minecraft.world.item.ItemStack;

/**
 * Represents the result of a fluid action (fill/drain from a container).
 */
public class FluidActionResult {
    public static final FluidActionResult FAILURE = new FluidActionResult(ItemStack.EMPTY);

    private final ItemStack result;

    public FluidActionResult(ItemStack result) {
        this.result = result;
    }

    public boolean isSuccess() {
        return !result.isEmpty();
    }

    public ItemStack getResult() {
        return result;
    }
}

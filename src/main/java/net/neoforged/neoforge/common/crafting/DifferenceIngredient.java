package net.neoforged.neoforge.common.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * An ingredient that matches items in the base ingredient but NOT in the subtracted ingredient.
 */
public class DifferenceIngredient implements ICustomIngredient {
    private final Ingredient base;
    private final Ingredient subtracted;

    private DifferenceIngredient(Ingredient base, Ingredient subtracted) {
        this.base = base;
        this.subtracted = subtracted;
    }

    /**
     * Creates a difference ingredient: matches items in base but not in subtracted.
     */
    public static Ingredient of(Ingredient base, Ingredient subtracted) {
        return new DifferenceIngredient(base, subtracted).toVanilla();
    }

    @Override
    public boolean test(ItemStack stack) {
        return base.test(stack) && !subtracted.test(stack);
    }

    @Override
    public Stream<ItemStack> getItems() {
        return Arrays.stream(base.getItems())
                .filter(stack -> !subtracted.test(stack));
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return null;
    }
}

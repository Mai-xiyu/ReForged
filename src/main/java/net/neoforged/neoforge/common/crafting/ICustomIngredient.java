package net.neoforged.neoforge.common.crafting;

import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Interface for custom ingredient implementations.
 * <p>Implementations must override {@link #test}, {@link #getItems}, and {@link #isSimple}.
 */
public interface ICustomIngredient {
    /**
     * Tests if the given stack matches this ingredient.
     */
    boolean test(ItemStack stack);

    /**
     * Returns all stacks that match this ingredient, for display purposes.
     */
    Stream<ItemStack> getItems();

    /**
     * Returns true if this ingredient only performs simple item/tag comparisons.
     */
    boolean isSimple();

    /**
     * Returns the type of this custom ingredient.
     */
    IngredientType<?> getType();

    /**
     * Creates an {@link Ingredient} wrapping this custom ingredient.
     */
    default Ingredient toVanilla() {
        return Ingredient.of(getItems());
    }
}

package net.neoforged.neoforge.common.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

/**
 * Crafting helper utilities — provides methods for ingredient/condition handling.
 */
public class CraftingHelper {
    private CraftingHelper() {}

    /**
     * Creates a sized ingredient from an ingredient and a count.
     */
    public static SizedIngredient getSizedIngredient(Ingredient ingredient, int count) {
        return new SizedIngredient(ingredient, count);
    }

    /**
     * Returns the ingredient from a vanilla ingredient type.
     */
    public static Ingredient getIngredient(Ingredient ingredient) {
        return ingredient;
    }
}

package net.neoforged.neoforge.common.brewing;

import net.minecraft.world.item.ItemStack;

/**
 * Interface for custom brewing recipes.
 */
public interface IBrewingRecipe {
    /**
     * Returns true if the given item stack is a valid input for this recipe.
     */
    boolean isInput(ItemStack input);

    /**
     * Returns true if the given item stack is a valid ingredient (reagent) for this recipe.
     */
    boolean isIngredient(ItemStack ingredient);

    /**
     * Returns the output of this recipe given the input and ingredient.
     * Returns {@link ItemStack#EMPTY} if the combination is not valid.
     */
    ItemStack getOutput(ItemStack input, ItemStack ingredient);
}

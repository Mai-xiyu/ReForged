package net.neoforged.neoforge.common.brewing;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Standard implementation of {@link IBrewingRecipe} using Ingredients.
 */
public class BrewingRecipe implements IBrewingRecipe {
    private final Ingredient input;
    private final Ingredient ingredient;
    private final ItemStack output;

    public BrewingRecipe(Ingredient input, Ingredient ingredient, ItemStack output) {
        this.input = input;
        this.ingredient = ingredient;
        this.output = output;
    }

    @Override
    public boolean isInput(ItemStack stack) {
        return input.test(stack);
    }

    @Override
    public boolean isIngredient(ItemStack stack) {
        return ingredient.test(stack);
    }

    @Override
    public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
        if (isInput(input) && isIngredient(ingredient)) {
            return output.copy();
        }
        return ItemStack.EMPTY;
    }

    public Ingredient getInput() { return input; }
    public Ingredient getIngredient() { return ingredient; }
    public ItemStack getOutput() { return output; }
}

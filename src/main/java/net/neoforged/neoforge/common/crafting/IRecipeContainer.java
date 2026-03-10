package net.neoforged.neoforge.common.crafting;

import net.minecraft.world.item.crafting.RecipeManager;

/**
 * Interface for objects that hold or reference recipe data.
 * Typically implemented by objects that need access to the recipe manager.
 */
public interface IRecipeContainer {
    /**
     * Returns the recipe manager associated with this container.
     */
    RecipeManager getRecipeManager();
}

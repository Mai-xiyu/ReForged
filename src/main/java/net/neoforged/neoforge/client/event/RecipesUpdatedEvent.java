package net.neoforged.neoforge.client.event;

import net.minecraft.world.item.crafting.RecipeManager;

/**
 * Fired when the client receives updated recipes from the server.
 */
public class RecipesUpdatedEvent extends net.neoforged.bus.api.Event {
    private final RecipeManager recipeManager;

    public RecipesUpdatedEvent(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;
    }

    public RecipeManager getRecipeManager() { return recipeManager; }
}

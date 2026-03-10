package net.neoforged.neoforge.common.extensions;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.Nullable;

/**
 * Extension interface for {@link RecipeOutput}.
 */
public interface IRecipeOutputExtension {

    private RecipeOutput self() { return (RecipeOutput) this; }

    default void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
        // Default: ignore conditions and delegate to vanilla accept
        self().accept(id, recipe, advancement);
    }

    default RecipeOutput withConditions(ICondition... conditions) {
        return self();
    }
}

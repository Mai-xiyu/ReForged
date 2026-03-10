package net.neoforged.neoforge.common.crafting;

import com.google.gson.JsonElement;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.Nullable;

/**
 * A recipe output that wraps conditional recipes — recipes that are only loaded
 * when the specified conditions are met.
 */
public class ConditionalRecipeOutput implements RecipeOutput {
    private final RecipeOutput wrapped;
    private final ICondition[] conditions;

    public ConditionalRecipeOutput(RecipeOutput wrapped, ICondition... conditions) {
        this.wrapped = wrapped;
        this.conditions = conditions;
    }

    @Override
    public Advancement.Builder advancement() {
        return wrapped.advancement();
    }

    @Override
    public HolderLookup.Provider registry() {
        return wrapped.registry();
    }

    @Override
    public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable ResourceLocation advancementId, @Nullable JsonElement advancement) {
        // In a full implementation, conditions would be serialized alongside the recipe.
        // For the shim, just pass through to the wrapped output.
        wrapped.accept(id, recipe, advancementId, advancement);
    }
}

package net.neoforged.neoforge.client.event;

import com.google.common.collect.ImmutableList;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Fired to allow registration of recipe book categories.
 */
public class RegisterRecipeBookCategoriesEvent extends net.neoforged.bus.api.Event implements IModBusEvent {
    private final Map<RecipeBookCategories, ImmutableList<RecipeBookCategories>> aggregateCategories;
    private final Map<net.minecraft.world.inventory.RecipeBookType, ImmutableList<RecipeBookCategories>> typeCategories;
    private final Map<RecipeType<?>, Function<RecipeHolder<?>, RecipeBookCategories>> recipeCategoryLookups;

    public RegisterRecipeBookCategoriesEvent(
            Map<RecipeBookCategories, ImmutableList<RecipeBookCategories>> aggregateCategories,
            Map<net.minecraft.world.inventory.RecipeBookType, ImmutableList<RecipeBookCategories>> typeCategories,
            Map<RecipeType<?>, Function<RecipeHolder<?>, RecipeBookCategories>> recipeCategoryLookups) {
        this.aggregateCategories = aggregateCategories;
        this.typeCategories = typeCategories;
        this.recipeCategoryLookups = recipeCategoryLookups;
    }

    public void registerAggregateCategory(RecipeBookCategories category, ImmutableList<RecipeBookCategories> subCategories) {
        aggregateCategories.put(category, subCategories);
    }

    public void registerBookCategories(net.minecraft.world.inventory.RecipeBookType type, ImmutableList<RecipeBookCategories> categories) {
        typeCategories.put(type, categories);
    }

    public void registerRecipeCategoryFinder(RecipeType<?> type, Function<RecipeHolder<?>, RecipeBookCategories> lookup) {
        recipeCategoryLookups.put(type, lookup);
    }
}

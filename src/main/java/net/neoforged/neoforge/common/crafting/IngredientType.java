package net.neoforged.neoforge.common.crafting;

import com.mojang.serialization.MapCodec;

/**
 * Type descriptor for a custom ingredient, similar to RecipeSerializer.
 * @param <T> the custom ingredient implementation type
 */
public record IngredientType<T extends ICustomIngredient>(MapCodec<T> codec) {
}

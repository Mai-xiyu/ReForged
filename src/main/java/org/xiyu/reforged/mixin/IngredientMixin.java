package org.xiyu.reforged.mixin;

import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Adds NeoForge-specific methods to {@link Ingredient} that NeoForge mods expect.
 * <p>In NeoForge, {@code hasNoItems()} is added by {@code IIngredientExtension}.</p>
 */
@Mixin(Ingredient.class)
public abstract class IngredientMixin {

    /**
     * NeoForge's {@code IIngredientExtension.hasNoItems()} — returns true if this ingredient
     * matches no items at all. Used by Twilight Forest's NoTemplateSmithingRecipe.
     */
    public boolean hasNoItems() {
		return ((Ingredient) (Object) this).getItems().length == 0;
    }
}

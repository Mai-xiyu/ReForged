package net.neoforged.neoforge.common.crafting;

import java.util.stream.Stream;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

/**
 * An ingredient that matches items based on their data component values.
 */
public class DataComponentIngredient implements ICustomIngredient {
    private final Ingredient base;
    private final DataComponentType<?> componentType;
    private final Object value;
    private final boolean strict;

    public DataComponentIngredient(Ingredient base, DataComponentType<?> componentType, Object value, boolean strict) {
        this.base = base;
        this.componentType = componentType;
        this.value = value;
        this.strict = strict;
    }

    /**
     * Creates a DataComponentIngredient that matches any item from the base ingredient
     * that has the specified component with the specified value.
     */
    public static <T> Ingredient of(boolean strict, DataComponentType<T> type, T value, ItemLike... items) {
        return new DataComponentIngredient(Ingredient.of(items), type, value, strict).toVanilla();
    }

    /**
     * Creates a DataComponentIngredient that matches any item from the base ingredient
     * that has the specified component with the specified value.
     */
    public static <T> Ingredient of(boolean strict, DataComponentType<T> type, T value, Ingredient base) {
        return new DataComponentIngredient(base, type, value, strict).toVanilla();
    }

    @Override
    public boolean test(ItemStack stack) {
        if (!base.test(stack)) return false;
        Object actual = stack.get(componentType);
        if (actual == null) return value == null;
        return value != null && value.equals(actual);
    }

    @Override
    public Stream<ItemStack> getItems() {
        return Stream.of(base.getItems());
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return null; // Not registered in ReForged shim
    }
}

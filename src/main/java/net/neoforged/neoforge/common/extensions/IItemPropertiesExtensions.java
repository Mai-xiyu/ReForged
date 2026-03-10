package net.neoforged.neoforge.common.extensions;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;

/**
 * Extension interface for {@link Item.Properties} with extra NeoForge builder methods.
 */
public interface IItemPropertiesExtensions {

    /**
     * Sets a data component via a supplier-based component type.
     */
    @SuppressWarnings("unchecked")
    default <T> Item.Properties component(Supplier<? extends DataComponentType<T>> componentType, T value) {
        return ((Item.Properties) this).component(componentType.get(), value);
    }
}

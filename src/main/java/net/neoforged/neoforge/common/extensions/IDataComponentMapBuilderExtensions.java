package net.neoforged.neoforge.common.extensions;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import org.jetbrains.annotations.Nullable;

/**
 * Extension interface for DataComponentMap.Builder.
 */
public interface IDataComponentMapBuilderExtensions {

    /**
     * Sets a component type via a supplier.
     */
    @SuppressWarnings("unchecked")
    default <T> DataComponentMap.Builder set(Supplier<? extends DataComponentType<T>> componentType, @Nullable T value) {
        return ((DataComponentMap.Builder) this).set(componentType.get(), value);
    }
}

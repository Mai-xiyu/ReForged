package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.component.DataComponentType;
import org.jetbrains.annotations.Nullable;

/**
 * Extension interface for DataComponentHolder.
 */
public interface IDataComponentHolderExtension {

    @Nullable
    default <T> T getOrDefault(DataComponentType<? extends T> type, T defaultValue) {
        return defaultValue;
    }
}

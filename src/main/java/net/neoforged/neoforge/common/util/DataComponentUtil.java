package net.neoforged.neoforge.common.util;

import com.mojang.serialization.Codec;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

/**
 * Utility methods for data component encoding with improved error reporting.
 */
public class DataComponentUtil {
    private DataComponentUtil() {}

    public static <T extends DataComponentHolder> Tag wrapEncodingExceptions(T componentHolder, Codec<T> codec, HolderLookup.Provider provider, Tag tag) {
        try {
            return codec.encode(componentHolder, provider.createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow();
        } catch (Exception exception) {
            logDataComponentSaveError(componentHolder, exception, tag);
            throw exception;
        }
    }

    public static <T extends DataComponentHolder> Tag wrapEncodingExceptions(T componentHolder, Codec<T> codec, HolderLookup.Provider provider) {
        try {
            return codec.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), componentHolder).getOrThrow();
        } catch (Exception exception) {
            logDataComponentSaveError(componentHolder, exception, null);
            throw exception;
        }
    }

    public static void logDataComponentSaveError(DataComponentHolder componentHolder, Exception original, @Nullable Tag tag) {
        StringBuilder cause = new StringBuilder("Error saving [" + componentHolder + "]. Original cause: " + original);
        cause.append("\nWith components:\n{");
        componentHolder.getComponents().forEach((component) -> {
            cause.append("\n\t").append(component);
        });
        cause.append("\n}");
        if (tag != null) {
            cause.append("\nWith tag: ").append(tag);
        }
        Util.logAndPauseIfInIde(cause.toString());
    }
}

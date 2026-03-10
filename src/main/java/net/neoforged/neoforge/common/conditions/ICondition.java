package net.neoforged.neoforge.common.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * NeoForge's ICondition interface for conditional loading of data-driven content.
 */
public interface ICondition {
    /**
     * Codec for a single condition, dispatching by codec() on each implementation.
     */
    Codec<ICondition> CODEC = Codec.unit(() -> ctx -> true);

    /**
     * Codec for a list of conditions.
     */
    Codec<List<ICondition>> LIST_CODEC = CODEC.listOf();

    boolean test(IContext context);

    default MapCodec<? extends ICondition> codec() {
        return MapCodec.unit(this);
    }

    interface IContext {
        IContext EMPTY = new IContext() {};

        /**
         * Return all tags for the given registry.
         */
        default <T> Map<ResourceLocation, Collection<Holder<T>>> getAllTags(ResourceKey<? extends Registry<T>> registry) {
            return Collections.emptyMap();
        }
    }
}

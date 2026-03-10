package net.neoforged.neoforge.common.conditions;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Condition that tests whether a specific item tag is empty.
 */
public record TagEmptyCondition(ResourceLocation tag) implements ICondition {

    public TagEmptyCondition(TagKey<Item> tag) {
        this(tag.location());
    }

    @Override
    public boolean test(IContext context) {
        TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tag);
        return !BuiltInRegistries.ITEM.getTagOrEmpty(tagKey).iterator().hasNext();
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return MapCodec.unit(this);
    }
}

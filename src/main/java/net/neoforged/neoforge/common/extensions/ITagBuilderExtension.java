package net.neoforged.neoforge.common.extensions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;

/**
 * Extension interface for TagBuilder.
 */
public interface ITagBuilderExtension {

    default TagBuilder getRawBuilder() {
        return (TagBuilder) this;
    }

    default TagBuilder removeElement(ResourceLocation elementID) {
        // NeoForge supports tag removal; in Forge shim this is a no-op
        return getRawBuilder();
    }

    default TagBuilder removeTag(ResourceLocation tagID) {
        // NeoForge supports tag removal; in Forge shim this is a no-op
        return getRawBuilder();
    }
}

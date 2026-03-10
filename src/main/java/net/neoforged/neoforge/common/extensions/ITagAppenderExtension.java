package net.neoforged.neoforge.common.extensions;

import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

/**
 * Extension interface for {@link TagsProvider.TagAppender}.
 */
public interface ITagAppenderExtension<T> {

    @SuppressWarnings("unchecked")
    private TagsProvider.TagAppender<T> self() { return (TagsProvider.TagAppender<T>) this; }

    @SuppressWarnings("unchecked")
    default TagsProvider.TagAppender<T> addTags(TagKey<T>... values) {
        for (TagKey<T> value : values) self().addTag(value);
        return self();
    }

    default TagsProvider.TagAppender<T> addOptionalTag(TagKey<T> value) {
        return self().addOptionalTag(value.location());
    }

    @SuppressWarnings("unchecked")
    default TagsProvider.TagAppender<T> addOptionalTags(TagKey<T>... values) {
        for (TagKey<T> value : values) addOptionalTag(value);
        return self();
    }

    default TagsProvider.TagAppender<T> replace() {
        return replace(true);
    }

    default TagsProvider.TagAppender<T> replace(boolean value) {
        return self();
    }
}

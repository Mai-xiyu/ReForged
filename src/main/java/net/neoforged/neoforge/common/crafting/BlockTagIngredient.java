package net.neoforged.neoforge.common.crafting;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * An ingredient that matches items whose corresponding block belongs to a tag.
 */
public class BlockTagIngredient implements ICustomIngredient {
    private final TagKey<Block> tag;

    public BlockTagIngredient(TagKey<Block> tag) {
        this.tag = tag;
    }

    public TagKey<Block> getTag() {
        return tag;
    }

    @Override
    public boolean test(ItemStack stack) {
        Block block = Block.byItem(stack.getItem());
        return block.defaultBlockState().is(tag);
    }

    @Override
    public Stream<ItemStack> getItems() {
        return StreamSupport.stream(BuiltInRegistries.BLOCK.getTagOrEmpty(tag).spliterator(), false)
                .map(holder -> new ItemStack(holder.value().asItem()));
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public IngredientType<?> getType() {
        return null;
    }
}

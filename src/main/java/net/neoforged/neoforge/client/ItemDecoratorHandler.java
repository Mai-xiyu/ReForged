package net.neoforged.neoforge.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles custom item overlay decorators (NeoForge API).
 * Jade calls {@code ItemDecoratorHandler.of(stack)} to get a handler,
 * then {@code handler.render(...)} to draw extra item decorations.
 */
public final class ItemDecoratorHandler {
    private static final Map<Item, ItemDecoratorHandler> DECORATOR_LOOKUP = new ConcurrentHashMap<>();
    private static final ItemDecoratorHandler EMPTY = new ItemDecoratorHandler(List.of());

    private final List<IItemDecorator> itemDecorators;

    private ItemDecoratorHandler(List<IItemDecorator> itemDecorators) {
        this.itemDecorators = new java.util.ArrayList<>(itemDecorators);
    }

    public static void register(Item item, IItemDecorator decorator) {
        DECORATOR_LOOKUP.computeIfAbsent(item, k -> new ItemDecoratorHandler(List.of()))
                .itemDecorators.add(decorator);
    }

    /**
     * Returns the decorator handler for the given stack's item, or an empty handler if none registered.
     */
    public static ItemDecoratorHandler of(ItemStack stack) {
        return DECORATOR_LOOKUP.getOrDefault(stack.getItem(), EMPTY);
    }

    /**
     * Render all registered item decorators for this handler.
     */
    public void render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        if (itemDecorators.isEmpty()) {
            return;
        }
        for (IItemDecorator itemDecorator : itemDecorators) {
            itemDecorator.render(guiGraphics, font, stack, xOffset, yOffset);
        }
    }
}

package net.neoforged.neoforge.client.event;

import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

/**
 * Fired after attribute tooltip lines have been added to an item stack's tooltip.
 * Can be used to add additional tooltip lines adjacent to the attribute lines.
 */
public class AddAttributeTooltipsEvent extends Event {
    protected final ItemStack stack;
    protected final Consumer<Component> tooltip;
    protected final AttributeTooltipContext ctx;

    public AddAttributeTooltipsEvent(ItemStack stack, Consumer<Component> tooltip, AttributeTooltipContext ctx) {
        this.stack = stack;
        this.tooltip = tooltip;
        this.ctx = ctx;
    }

    public AttributeTooltipContext getContext() { return this.ctx; }
    public ItemStack getStack() { return this.stack; }

    public void addTooltipLines(Component... comps) {
        for (Component comp : comps) {
            this.tooltip.accept(comp);
        }
    }

    public boolean shouldShow() {
        return this.stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).showInTooltip();
    }
}

package net.neoforged.neoforge.event.enchanting;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * Fired to modify the effective enchantment level(s) on an item.
 * Handlers may modify the enchantments map to change levels.
 */
public class GetEnchantmentLevelEvent extends Event {
    private final ItemStack stack;
    private final Map<Holder<Enchantment>, Integer> enchantments;

    public GetEnchantmentLevelEvent(ItemStack stack, Map<Holder<Enchantment>, Integer> enchantments) {
        this.stack = stack;
        this.enchantments = enchantments;
    }

    public ItemStack getStack() { return stack; }
    public Map<Holder<Enchantment>, Integer> getEnchantments() { return enchantments; }
}

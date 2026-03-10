package net.neoforged.neoforge.event.entity.player;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

/**
 * Fired after a player enchants an item via the enchanting table.
 */
public class PlayerEnchantItemEvent extends PlayerEvent {
    private final ItemStack item;
    private final int enchantmentLevel;
    private final List<EnchantmentInstance> enchantments;

    public PlayerEnchantItemEvent(Player player, ItemStack item, int enchantmentLevel,
            List<EnchantmentInstance> enchantments) {
        super(player);
        this.item = item;
        this.enchantmentLevel = enchantmentLevel;
        this.enchantments = enchantments;
    }

    public ItemStack getItem() { return item; }
    public int getEnchantmentLevel() { return enchantmentLevel; }
    public List<EnchantmentInstance> getEnchantments() { return enchantments; }
}

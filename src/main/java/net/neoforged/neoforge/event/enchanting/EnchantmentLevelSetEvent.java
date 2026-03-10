package net.neoforged.neoforge.event.enchanting;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when an enchantment level is set in an enchanting table.
 */
public class EnchantmentLevelSetEvent extends Event {
    private final Level level;
    private final BlockPos pos;
    private final int enchantRow;
    private final ItemStack item;
    private final int originalLevel;
    private int enchantLevel;

    public EnchantmentLevelSetEvent(Level level, BlockPos pos, int enchantRow, int power, ItemStack item, int enchantLevel) {
        this.level = level;
        this.pos = pos;
        this.enchantRow = enchantRow;
        this.item = item;
        this.originalLevel = enchantLevel;
        this.enchantLevel = enchantLevel;
    }

    /** Forge wrapper constructor for automatic event bridging */
    public EnchantmentLevelSetEvent(net.minecraftforge.event.enchanting.EnchantmentLevelSetEvent delegate) {
        this(delegate.getLevel(), delegate.getPos(), delegate.getEnchantRow(), delegate.getPower(), delegate.getItem(), delegate.getEnchantLevel());
    }

    public Level getLevel() { return level; }
    public BlockPos getPos() { return pos; }
    public int getEnchantRow() { return enchantRow; }
    public ItemStack getItem() { return item; }
    public int getOriginalLevel() { return originalLevel; }
    public int getEnchantLevel() { return enchantLevel; }
    public void setEnchantLevel(int enchantLevel) { this.enchantLevel = enchantLevel; }
}

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Fired when the player removes a repaired item from the Anvil's output slot.
 */
public class AnvilRepairEvent extends PlayerEvent {
    private final ItemStack left;
    private final ItemStack right;
    private final ItemStack output;
    private float breakChance;

    public AnvilRepairEvent(Player player, ItemStack left, ItemStack right, ItemStack output) {
        super(player);
        this.left = left;
        this.right = right;
        this.output = output;
        this.breakChance = 0.12f;
    }

    /** Forge wrapper constructor for automatic event bridging */
    public AnvilRepairEvent(net.minecraftforge.event.entity.player.AnvilRepairEvent delegate) {
        this(delegate.getEntity(), delegate.getLeft(), delegate.getRight(), delegate.getOutput());
    }

    public ItemStack getOutput() { return output; }
    public ItemStack getLeft() { return left; }
    public ItemStack getRight() { return right; }
    public float getBreakChance() { return breakChance; }
    public void setBreakChance(float breakChance) { this.breakChance = breakChance; }
}

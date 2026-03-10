package net.neoforged.neoforge.event;

import net.minecraft.world.item.ItemStack;

/**
 * Fired when the inputs to an anvil are changed. Allows control of output,
 * experience cost, and material cost.
 */
public class AnvilUpdateEvent extends net.neoforged.bus.api.Event {
    private final ItemStack left;
    private final ItemStack right;
    private final String name;
    private ItemStack output = ItemStack.EMPTY;
    private int cost = 0;
    private int materialCost = 0;

    public AnvilUpdateEvent(ItemStack left, ItemStack right, String name, int cost) {
        this.left = left;
        this.right = right;
        this.name = name;
        this.cost = cost;
    }

    /** Forge wrapper constructor for automatic event bridging */
    public AnvilUpdateEvent(net.minecraftforge.event.AnvilUpdateEvent delegate) {
        this(delegate.getLeft(), delegate.getRight(), delegate.getName(), (int) delegate.getCost());
    }

    /** The left input (item being modified). */
    public ItemStack getLeft() { return left; }
    /** The right input (modifier item). */
    public ItemStack getRight() { return right; }
    /** The custom name typed into the anvil. */
    public String getName() { return name; }
    /** The output item. */
    public ItemStack getOutput() { return output; }
    /** Sets the output item. */
    public void setOutput(ItemStack output) { this.output = output; }
    /** The experience level cost. */
    public int getCost() { return cost; }
    /** Sets the experience level cost. */
    public void setCost(int cost) { this.cost = cost; }
    /** The material cost (number of items from right slot). */
    public int getMaterialCost() { return materialCost; }
    /** Sets the material cost. */
    public void setMaterialCost(int materialCost) { this.materialCost = materialCost; }
}

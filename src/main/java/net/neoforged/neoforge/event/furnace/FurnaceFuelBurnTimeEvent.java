package net.neoforged.neoforge.event.furnace;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired to determine the burn time of an item used as furnace fuel.
 */
public class FurnaceFuelBurnTimeEvent extends Event {
    private final ItemStack itemStack;
    private int burnTime;

    public FurnaceFuelBurnTimeEvent(ItemStack itemStack, int burnTime) {
        this.itemStack = itemStack;
        this.burnTime = burnTime;
    }

    /** Forge wrapper constructor for automatic event bridging */
    public FurnaceFuelBurnTimeEvent(net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent delegate) {
        this(delegate.getItemStack(), delegate.getBurnTime());
    }

    public ItemStack getItemStack() { return itemStack; }
    public int getBurnTime() { return burnTime; }
    public void setBurnTime(int burnTime) { this.burnTime = burnTime; }
}

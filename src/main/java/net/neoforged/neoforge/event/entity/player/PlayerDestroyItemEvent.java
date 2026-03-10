package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Stub: Fired when a player destroys an item (breaks/uses up). */
public class PlayerDestroyItemEvent extends PlayerEvent {
    private final ItemStack original;

    public PlayerDestroyItemEvent(Player player, ItemStack original) {
        super();
        this.original = original;
    }

    /** Forge wrapper constructor for automatic event bridging */
    public PlayerDestroyItemEvent(net.minecraftforge.event.entity.player.PlayerDestroyItemEvent delegate) {
        this(delegate.getEntity(), delegate.getOriginal());
    }

    public ItemStack getOriginal() { return original; }
}

package net.neoforged.neoforge.event.entity.player;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fired when a player fishes an item. Canceling prevents item drops but hook damage still applies.
 */
public class ItemFishedEvent extends PlayerEvent implements ICancellableEvent {
    private final NonNullList<ItemStack> stacks = NonNullList.create();
    private final FishingHook hook;
    private int rodDamage;

    public ItemFishedEvent(List<ItemStack> stacks, int rodDamage, FishingHook hook) {
        super(hook.getPlayerOwner());
        this.stacks.addAll(stacks);
        this.rodDamage = rodDamage;
        this.hook = hook;
    }

    /** Forge wrapper constructor for automatic event bridging */
    public ItemFishedEvent(net.minecraftforge.event.entity.player.ItemFishedEvent delegate) {
        this(delegate.getDrops(), delegate.getRodDamage(), delegate.getHookEntity());
    }

    public int getRodDamage() { return rodDamage; }
    public void damageRodBy(int rodDamage) { this.rodDamage = rodDamage; }
    public NonNullList<ItemStack> getDrops() { return stacks; }
    public FishingHook getHookEntity() { return hook; }
}

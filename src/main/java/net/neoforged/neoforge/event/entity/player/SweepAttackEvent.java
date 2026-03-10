package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fired when determining whether a player's attack should sweep.
 */
public class SweepAttackEvent extends PlayerEvent implements ICancellableEvent {
    private final Entity target;
    private final boolean isVanillaSweep;
    private boolean isSweeping;

    public SweepAttackEvent(Player player, Entity target, boolean isVanillaSweep) {
        super(player);
        this.target = target;
        this.isSweeping = this.isVanillaSweep = isVanillaSweep;
    }

    public Entity getTarget() { return target; }
    public boolean isVanillaSweep() { return isVanillaSweep; }
    public boolean isSweeping() { return isSweeping; }
    public void setSweeping(boolean sweep) { this.isSweeping = sweep; }
}

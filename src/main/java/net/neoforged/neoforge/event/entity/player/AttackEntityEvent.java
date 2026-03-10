package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fired when a player attacks an entity. If cancelled, the attack does not proceed.
 */
public class AttackEntityEvent extends Event implements ICancellableEvent {
    private final Player player;
    private final Entity target;

    public AttackEntityEvent(Player player, Entity target) {
        this.player = player;
        this.target = target;
    }

    public Player getEntity() { return player; }
    public Entity getTarget() { return target; }
}

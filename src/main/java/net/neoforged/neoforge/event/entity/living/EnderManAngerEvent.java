package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;

/**
 * Stub: Fired when an Enderman gets angry at a player.
 */
public class EnderManAngerEvent extends LivingEvent {
    private final Player player;

    public EnderManAngerEvent(EnderMan entity, Player player) {
        super(entity);
        this.player = player;
    }

    /** Forge wrapper constructor for automatic event bridging */
    public EnderManAngerEvent(net.minecraftforge.event.entity.living.EnderManAngerEvent delegate) {
        this((EnderMan) delegate.getEntity(), delegate.getPlayer());
    }

    public Player getPlayer() { return player; }
}

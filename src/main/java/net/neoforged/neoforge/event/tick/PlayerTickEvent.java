package net.neoforged.neoforge.event.tick;

import net.minecraft.world.entity.player.Player;

/**
 * NeoForge PlayerTickEvent with Forge wrapper constructors.
 */
public class PlayerTickEvent extends net.neoforged.bus.api.Event {
    private Player player;

    /** Required by Forge's EventListenerHelper */
    public PlayerTickEvent() {}

    public PlayerTickEvent(Player player) {
        this.player = player;
    }

    public Player getEntity() { return player; }

    public static class Pre extends PlayerTickEvent {
        /** Required by Forge's EventListenerHelper */
        public Pre() { super(); }

        public Pre(Player player) { super(player); }

        /** Wrapper constructor */
        public Pre(net.minecraftforge.event.TickEvent.PlayerTickEvent.Pre forge) {
            super(forge.player);
        }
    }

    public static class Post extends PlayerTickEvent {
        /** Required by Forge's EventListenerHelper */
        public Post() { super(); }

        public Post(Player player) { super(player); }

        /** Wrapper constructor */
        public Post(net.minecraftforge.event.TickEvent.PlayerTickEvent.Post forge) {
            super(forge.player);
        }
    }
}

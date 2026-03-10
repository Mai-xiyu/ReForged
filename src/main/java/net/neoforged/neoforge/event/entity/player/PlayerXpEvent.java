package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;

/**
 * Stub: Base event for player XP events.
 */
public abstract class PlayerXpEvent extends PlayerEvent {
    public PlayerXpEvent(Player player) {
        super();
    }

    public static class PickupXp extends PlayerXpEvent {
        private final ExperienceOrb orb;

        public PickupXp(Player player, ExperienceOrb orb) {
            super(player);
            this.orb = orb;
        }

        /** Forge wrapper constructor for automatic event bridging */
        public PickupXp(net.minecraftforge.event.entity.player.PlayerXpEvent.PickupXp delegate) {
            this(delegate.getEntity(), delegate.getOrb());
        }

        public ExperienceOrb getOrb() { return orb; }
    }

    public static class XpChange extends PlayerXpEvent {
        private int amount;

        public XpChange(Player player, int amount) {
            super(player);
            this.amount = amount;
        }

        /** Forge wrapper constructor for automatic event bridging */
        public XpChange(net.minecraftforge.event.entity.player.PlayerXpEvent.XpChange delegate) {
            this(delegate.getEntity(), delegate.getAmount());
        }

        public int getAmount() { return amount; }
        public void setAmount(int amount) { this.amount = amount; }
    }

    public static class LevelChange extends PlayerXpEvent {
        private int levels;

        public LevelChange(Player player, int levels) {
            super(player);
            this.levels = levels;
        }

        /** Forge wrapper constructor for automatic event bridging */
        public LevelChange(net.minecraftforge.event.entity.player.PlayerXpEvent.LevelChange delegate) {
            this(delegate.getEntity(), delegate.getLevels());
        }

        public int getLevels() { return levels; }
        public void setLevels(int levels) { this.levels = levels; }
    }
}

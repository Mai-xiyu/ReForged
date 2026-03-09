package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.player.Player;

public class PlayerWakeUpEvent extends PlayerEvent {
    private final boolean wakeImmediately;
    private final boolean updateLevel;

    public PlayerWakeUpEvent() {
        super();
        this.wakeImmediately = false;
        this.updateLevel = false;
    }

    public PlayerWakeUpEvent(Player player, boolean wakeImmediately, boolean updateLevel) {
        super(player);
        this.wakeImmediately = wakeImmediately;
        this.updateLevel = updateLevel;
    }

    public PlayerWakeUpEvent(net.minecraftforge.event.entity.player.PlayerWakeUpEvent forge) {
        super(forge);
        this.wakeImmediately = forge.wakeImmediately();
        this.updateLevel = forge.updateLevel();
    }

    public boolean wakeImmediately() { return wakeImmediately; }
    public boolean updateLevel() { return updateLevel; }
}

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.ICancellableEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class PermissionsChangedEvent extends PlayerEvent implements ICancellableEvent {
    private final net.minecraftforge.event.entity.player.PermissionsChangedEvent forgeDelegate;
    private final int newLevel;
    private final int oldLevel;

    public PermissionsChangedEvent() {
        super();
        this.forgeDelegate = null;
        this.newLevel = 0;
        this.oldLevel = 0;
    }

    public PermissionsChangedEvent(ServerPlayer player, int newLevel, int oldLevel) {
        super(player);
        this.forgeDelegate = null;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    public PermissionsChangedEvent(net.minecraftforge.event.entity.player.PermissionsChangedEvent forge) {
        super(forge);
        this.forgeDelegate = forge;
        this.oldLevel = forge.getOldLevel();
        this.newLevel = forge.getNewLevel();
    }

    public int getNewLevel() {
        return newLevel;
    }

    public int getOldLevel() {
        return oldLevel;
    }

    @Override
    public void setCanceled(boolean canceled) {
        super.setCanceled(canceled);
        if (forgeDelegate != null) {
            forgeDelegate.setCanceled(canceled);
        }
    }
}

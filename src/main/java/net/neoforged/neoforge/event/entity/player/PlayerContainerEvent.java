package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Stub: Fired when a player opens/closes a container.
 */
public class PlayerContainerEvent extends PlayerEvent {
    private final AbstractContainerMenu container;

    public PlayerContainerEvent(Player player, AbstractContainerMenu container) {
        super();
        this.container = container;
    }

    public AbstractContainerMenu getContainer() { return container; }

    public static class Open extends PlayerContainerEvent {
        public Open(Player player, AbstractContainerMenu container) {
            super(player, container);
        }

        /** Forge wrapper constructor for automatic event bridging */
        public Open(net.minecraftforge.event.entity.player.PlayerContainerEvent.Open delegate) {
            this(delegate.getEntity(), delegate.getContainer());
        }
    }

    public static class Close extends PlayerContainerEvent {
        public Close(Player player, AbstractContainerMenu container) {
            super(player, container);
        }

        /** Forge wrapper constructor for automatic event bridging */
        public Close(net.minecraftforge.event.entity.player.PlayerContainerEvent.Close delegate) {
            this(delegate.getEntity(), delegate.getContainer());
        }
    }
}

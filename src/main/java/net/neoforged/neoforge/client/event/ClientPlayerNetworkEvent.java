package net.neoforged.neoforge.client.event;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import org.jetbrains.annotations.Nullable;

/**
 * Events fired when the client player network state changes.
 */
public abstract class ClientPlayerNetworkEvent extends net.neoforged.bus.api.Event {
    @Nullable
    private final MultiPlayerGameMode multiPlayerGameMode;
    @Nullable
    private final LocalPlayer player;
    @Nullable
    private final Connection connection;

    protected ClientPlayerNetworkEvent(@Nullable MultiPlayerGameMode multiPlayerGameMode,
            @Nullable LocalPlayer player, @Nullable Connection connection) {
        this.multiPlayerGameMode = multiPlayerGameMode;
        this.player = player;
        this.connection = connection;
    }

    @Nullable
    public MultiPlayerGameMode getMultiPlayerGameMode() { return multiPlayerGameMode; }
    @Nullable
    public LocalPlayer getPlayer() { return player; }
    @Nullable
    public Connection getConnection() { return connection; }

    /** Fired when the client player logs in. */
    public static class LoggingIn extends ClientPlayerNetworkEvent {
        public LoggingIn(MultiPlayerGameMode multiPlayerGameMode, LocalPlayer player, Connection connection) {
            super(multiPlayerGameMode, player, connection);
        }

        /** Wrapper constructor for EventBusAdapter bridging. */
        public LoggingIn(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingIn forge) {
            this(forge.getMultiPlayerGameMode(), forge.getPlayer(), forge.getConnection());
        }
    }

    /** Fired when the client player logs out. All fields may be null. */
    public static class LoggingOut extends ClientPlayerNetworkEvent {
        public LoggingOut(@Nullable MultiPlayerGameMode multiPlayerGameMode,
                @Nullable LocalPlayer player, @Nullable Connection connection) {
            super(multiPlayerGameMode, player, connection);
        }

        /** Wrapper constructor for EventBusAdapter bridging. */
        public LoggingOut(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut forge) {
            this(forge.getMultiPlayerGameMode(), forge.getPlayer(), forge.getConnection());
        }
    }

    /** Fired when the client player is cloned (e.g., respawn). */
    public static class Clone extends ClientPlayerNetworkEvent {
        private final LocalPlayer oldPlayer;

        public Clone(MultiPlayerGameMode multiPlayerGameMode, LocalPlayer oldPlayer,
                LocalPlayer newPlayer, Connection connection) {
            super(multiPlayerGameMode, newPlayer, connection);
            this.oldPlayer = oldPlayer;
        }

        /** Wrapper constructor for EventBusAdapter bridging. */
        public Clone(net.minecraftforge.client.event.ClientPlayerNetworkEvent.Clone forge) {
            this(forge.getMultiPlayerGameMode(), forge.getOldPlayer(), forge.getNewPlayer(), forge.getConnection());
        }

        public LocalPlayer getOldPlayer() { return oldPlayer; }
        public LocalPlayer getNewPlayer() { return getPlayer(); }
    }
}

package net.neoforged.neoforge.client.event;

import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.Event;

/**
 * Fired when the client player is notified of a change of {@link GameType} from the server.
 */
public class ClientPlayerChangeGameTypeEvent extends Event {
    private final PlayerInfo info;
    private final GameType currentGameType;
    private final GameType newGameType;

    public ClientPlayerChangeGameTypeEvent(PlayerInfo info, GameType currentGameType, GameType newGameType) {
        this.info = info;
        this.currentGameType = currentGameType;
        this.newGameType = newGameType;
    }

    /** Wrapper constructor for EventBusAdapter bridging. */
    public ClientPlayerChangeGameTypeEvent(net.minecraftforge.client.event.ClientPlayerChangeGameTypeEvent forge) {
        this(forge.getInfo(), forge.getCurrentGameType(), forge.getNewGameType());
    }

    public PlayerInfo getInfo() { return info; }
    public GameType getCurrentGameType() { return currentGameType; }
    public GameType getNewGameType() { return newGameType; }
}

package net.neoforged.neoforge.event;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Fired when a player sends a chat message on the server.
 * Cancelable: prevents the message from being sent.
 */
public class ServerChatEvent extends net.neoforged.bus.api.Event {
    private final ServerPlayer player;
    private final String rawText;
    private Component message;

    public ServerChatEvent(ServerPlayer player, String rawText, Component message) {
        this.player = player;
        this.rawText = rawText;
        this.message = message;
    }

    /** The player who sent the message. */
    public ServerPlayer getPlayer() { return player; }
    /** The raw text of the message as typed. */
    public String getRawText() { return rawText; }
    /** @deprecated Use {@link #getRawText()} */
    @Deprecated
    public String getMessage() { return rawText; }
    /** The formatted message component. */
    public Component getComponent() { return message; }
    /** Sets the formatted message component. */
    public void setComponent(Component message) { this.message = message; }

    @Override
    public boolean isCancelable() { return true; }
}

package net.neoforged.neoforge.client.event;

import com.google.common.base.Strings;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Stub: Fired when a player sends a chat message from the client.
 */
public class ClientChatEvent extends net.neoforged.bus.api.Event implements ICancellableEvent {
    private String message;
    private final String originalMessage;

    public ClientChatEvent(String message) {
        this.originalMessage = Strings.nullToEmpty(message);
        this.message = this.originalMessage;
    }

    /** Wrapper constructor for EventBusAdapter bridging. */
    public ClientChatEvent(net.minecraftforge.client.event.ClientChatEvent forge) {
        this(forge.getMessage());
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = Strings.nullToEmpty(message); }
    public String getOriginalMessage() { return originalMessage; }
}

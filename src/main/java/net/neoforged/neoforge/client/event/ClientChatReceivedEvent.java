package net.neoforged.neoforge.client.event;

import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Stub: Fired when a chat message is received on the client.
 */
public class ClientChatReceivedEvent extends net.neoforged.bus.api.Event implements ICancellableEvent {
    private Component message;
    @Nullable
    private final ChatType.Bound boundChatType;
    private final UUID sender;

    public ClientChatReceivedEvent(@Nullable ChatType.Bound boundChatType, Component message, UUID sender) {
        this.boundChatType = boundChatType;
        this.message = message;
        this.sender = sender;
    }

    /** Wrapper constructor for EventBusAdapter bridging. */
    public ClientChatReceivedEvent(net.minecraftforge.client.event.ClientChatReceivedEvent forge) {
        this(forge.getBoundChatType(), forge.getMessage(), forge.getSender());
    }

    public Component getMessage() { return message; }
    public void setMessage(Component message) { this.message = message; }
    @Nullable public ChatType.Bound getBoundChatType() { return boundChatType; }
    public UUID getSender() { return sender; }
    public boolean isSystem() { return Util.NIL_UUID.equals(sender); }

    public static class System extends ClientChatReceivedEvent {
        private final boolean overlay;

        public System(Component message) {
			this(message, false);
		}

		public System(Component message, boolean overlay) {
			super(null, message, Util.NIL_UUID);
			this.overlay = overlay;
		}

		/** Wrapper constructor for EventBusAdapter bridging. */
		public System(net.minecraftforge.client.event.ClientChatReceivedEvent.System forge) {
			this(forge.getMessage(), forge.isOverlay());
		}

		public boolean isOverlay() { return overlay; }
    }

    public static class Player extends ClientChatReceivedEvent {
        private final PlayerChatMessage playerChatMessage;

		public Player(Component message) {
			this(null, message, null, Util.NIL_UUID);
		}

		public Player(@Nullable ChatType.Bound boundChatType, Component message, @Nullable PlayerChatMessage playerChatMessage, UUID sender) {
			super(boundChatType, message, sender);
			this.playerChatMessage = playerChatMessage;
		}

		/** Wrapper constructor for EventBusAdapter bridging. */
		public Player(net.minecraftforge.client.event.ClientChatReceivedEvent.Player forge) {
			this(forge.getBoundChatType(), forge.getMessage(), forge.getPlayerChatMessage(), forge.getSender());
		}

		@Nullable
		public PlayerChatMessage getPlayerChatMessage() { return playerChatMessage; }
    }
}

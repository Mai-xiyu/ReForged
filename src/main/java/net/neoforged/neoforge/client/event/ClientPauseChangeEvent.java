package net.neoforged.neoforge.client.event;

import net.minecraftforge.eventbus.api.Event;

/**
 * Stub: Fired when the client pause state changes.
 */
public class ClientPauseChangeEvent extends Event {
    private final boolean paused;

    public ClientPauseChangeEvent(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() { return paused; }

    public static class Pre extends ClientPauseChangeEvent {
        public Pre(boolean paused) { super(paused); }

        /** Wrapper constructor for EventBusAdapter bridging. */
        public Pre(net.minecraftforge.client.event.ClientPauseChangeEvent.Pre forge) {
            this(forge.isPaused());
        }
    }

    public static class Post extends ClientPauseChangeEvent {
        public Post(boolean paused) { super(paused); }

        /** Wrapper constructor for EventBusAdapter bridging. */
        public Post(net.minecraftforge.client.event.ClientPauseChangeEvent.Post forge) {
            this(forge.isPaused());
        }
    }
}

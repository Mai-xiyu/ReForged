package net.neoforged.neoforge.client.event;

import net.minecraft.client.DeltaTracker;

/**
 * Fired before/after the game renderer processes a frame.
 */
public abstract class RenderFrameEvent extends net.neoforged.bus.api.Event {
    protected final DeltaTracker partialTick;

    protected RenderFrameEvent(DeltaTracker partialTick) {
        this.partialTick = partialTick;
    }

    public DeltaTracker getPartialTick() { return partialTick; }

    /** Fired before the game renderer processes a frame. */
    public static class Pre extends RenderFrameEvent {
        public Pre(DeltaTracker partialTick) { super(partialTick); }
    }

    /** Fired after the game renderer processes a frame. */
    public static class Post extends RenderFrameEvent {
        public Post(DeltaTracker partialTick) { super(partialTick); }
    }
}

package net.neoforged.neoforge.client.event;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired when the HUD is rendered to the screen.
 * In Forge 1.21.1 the original RenderGuiEvent was removed (Mojang layered rendering).
 * ReForged fires this synthetically via Mixin into Gui.render() so NeoForge mods
 * (like Jade) that listen for RenderGuiEvent.Post can render their overlays.
 */
public abstract class RenderGuiEvent extends Event {
    private final GuiGraphics guiGraphics;
    private final DeltaTracker partialTick;

    /** No-arg ctor required by Forge EventListenerHelper to compute ListenerList */
    protected RenderGuiEvent() { this(null, null); }

    protected RenderGuiEvent(GuiGraphics guiGraphics, DeltaTracker partialTick) {
        this.guiGraphics = guiGraphics;
        this.partialTick = partialTick;
    }

    public GuiGraphics getGuiGraphics() { return guiGraphics; }
    public DeltaTracker getPartialTick() { return partialTick; }

    public static class Pre extends RenderGuiEvent {
        public Pre() { super(); }
        public Pre(GuiGraphics g, DeltaTracker pt) { super(g, pt); }
    }

    public static class Post extends RenderGuiEvent {
        public Post() { super(); }
        public Post(GuiGraphics g, DeltaTracker pt) { super(g, pt); }
    }
}

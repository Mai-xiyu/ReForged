package net.neoforged.neoforge.client.event;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;

/**
 * Fired before/after each GUI layer is rendered.
 */
public abstract class RenderGuiLayerEvent extends net.neoforged.bus.api.Event {
    private final GuiGraphics guiGraphics;
    private final DeltaTracker partialTick;
    private final ResourceLocation name;
    private final LayeredDraw.Layer layer;

    protected RenderGuiLayerEvent(GuiGraphics guiGraphics, DeltaTracker partialTick,
            ResourceLocation name, LayeredDraw.Layer layer) {
        this.guiGraphics = guiGraphics;
        this.partialTick = partialTick;
        this.name = name;
        this.layer = layer;
    }

    public GuiGraphics getGuiGraphics() { return guiGraphics; }
    public DeltaTracker getPartialTick() { return partialTick; }
    public ResourceLocation getName() { return name; }
    public LayeredDraw.Layer getLayer() { return layer; }

    /** Fired before a GUI layer is rendered. Cancellable. */
    public static class Pre extends RenderGuiLayerEvent implements net.neoforged.bus.api.ICancellableEvent {
        public Pre(GuiGraphics guiGraphics, DeltaTracker partialTick, ResourceLocation name, LayeredDraw.Layer layer) {
            super(guiGraphics, partialTick, name, layer);
        }
    }

    /** Fired after a GUI layer is rendered. */
    public static class Post extends RenderGuiLayerEvent {
        public Post(GuiGraphics guiGraphics, DeltaTracker partialTick, ResourceLocation name, LayeredDraw.Layer layer) {
            super(guiGraphics, partialTick, name, layer);
        }
    }
}

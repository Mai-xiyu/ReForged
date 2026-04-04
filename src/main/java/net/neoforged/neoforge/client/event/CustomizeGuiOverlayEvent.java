package net.neoforged.neoforge.client.event;

import net.minecraft.client.gui.GuiGraphics;
import java.util.List;

/** Wrapper around Forge's {@link net.minecraftforge.client.event.CustomizeGuiOverlayEvent}. */
public class CustomizeGuiOverlayEvent {
    private final net.minecraftforge.client.event.CustomizeGuiOverlayEvent delegate;

    public CustomizeGuiOverlayEvent(net.minecraftforge.client.event.CustomizeGuiOverlayEvent delegate) {
        this.delegate = delegate;
    }

    public GuiGraphics getGuiGraphics() { return delegate.getGuiGraphics(); }

    public static class BossEventProgress extends CustomizeGuiOverlayEvent {
        private final net.minecraftforge.client.event.CustomizeGuiOverlayEvent.BossEventProgress forgeEvent;

        public BossEventProgress(net.minecraftforge.client.event.CustomizeGuiOverlayEvent.BossEventProgress delegate) {
            super(delegate);
            this.forgeEvent = delegate;
        }

        public net.minecraft.client.gui.components.LerpingBossEvent getBossEvent() {
            return forgeEvent.getBossEvent();
        }

        public int getX() { return forgeEvent.getX(); }
        public int getY() { return forgeEvent.getY(); }
        public int getIncrement() { return forgeEvent.getIncrement(); }
        public void setIncrement(int increment) { forgeEvent.setIncrement(increment); }
    }

    /** Wrapper for Forge's DebugText event — adds/removes debug screen text. */
    public static class DebugText extends CustomizeGuiOverlayEvent {
        private final net.minecraftforge.client.event.CustomizeGuiOverlayEvent.DebugText forgeEvent;

        public DebugText(net.minecraftforge.client.event.CustomizeGuiOverlayEvent.DebugText delegate) {
            super(delegate);
            this.forgeEvent = delegate;
        }

        public List<String> getText() { return forgeEvent.getText(); }
    }
}

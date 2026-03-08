package net.neoforged.neoforge.client.event;

import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.NotNull;

/**
 * NeoForge RenderTooltipEvent hierarchy with full fields and Forge wrapper constructors.
 */
public class RenderTooltipEvent extends Event {
    @NotNull private ItemStack itemStack;
    private GuiGraphics graphics;
    protected int x;
    protected int y;
    protected Font font;
    private List<ClientTooltipComponent> components;

    /** Required by Forge's EventListenerHelper */
    public RenderTooltipEvent() {}

    public RenderTooltipEvent(@NotNull ItemStack itemStack, GuiGraphics graphics,
                               int x, int y, Font font, List<ClientTooltipComponent> components) {
        this.itemStack = itemStack;
        this.graphics = graphics;
        this.x = x;
        this.y = y;
        this.font = font;
        this.components = components;
    }

    @NotNull public ItemStack getItemStack() { return itemStack; }
    public GuiGraphics getGraphics() { return graphics; }
    public List<ClientTooltipComponent> getComponents() { return components; }
    public int getX() { return x; }
    public int getY() { return y; }
    public Font getFont() { return font; }

    // ── Pre ───────────────────────────────────────────────
    public static class Pre extends RenderTooltipEvent implements ICancellableEvent {
        private int screenWidth;
        private int screenHeight;
        private ClientTooltipPositioner positioner;

        /** Required by Forge's EventListenerHelper */
        public Pre() { super(); }

        public Pre(@NotNull ItemStack stack, GuiGraphics graphics, int x, int y,
                   int screenWidth, int screenHeight, Font font,
                   List<ClientTooltipComponent> components, ClientTooltipPositioner positioner) {
            super(stack, graphics, x, y, font, components);
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
            this.positioner = positioner;
        }

        /** Wrapper constructor */
        public Pre(net.minecraftforge.client.event.RenderTooltipEvent.Pre forge) {
            super(forge.getItemStack(), forge.getGraphics(), forge.getX(), forge.getY(),
                  forge.getFont(), forge.getComponents());
            this.screenWidth = forge.getScreenWidth();
            this.screenHeight = forge.getScreenHeight();
            this.positioner = forge.getTooltipPositioner();
        }

        public int getScreenWidth() { return screenWidth; }
        public int getScreenHeight() { return screenHeight; }
        public ClientTooltipPositioner getTooltipPositioner() { return positioner; }
        public void setX(int x) { this.x = x; }
        public void setY(int y) { this.y = y; }
        public void setFont(Font font) { this.font = font; }
    }

    // ── Color ─────────────────────────────────────────────
    public static class Color extends RenderTooltipEvent {
        private int originalBackground;
        private int originalBorderStart;
        private int originalBorderEnd;
        private int backgroundStart;
        private int backgroundEnd;
        private int borderStart;
        private int borderEnd;

        /** Required by Forge's EventListenerHelper */
        public Color() { super(); }

        public Color(@NotNull ItemStack stack, GuiGraphics graphics, int x, int y, Font font,
                     int background, int borderStart, int borderEnd,
                     List<ClientTooltipComponent> components) {
            super(stack, graphics, x, y, font, components);
            this.originalBackground = background;
            this.originalBorderStart = borderStart;
            this.originalBorderEnd = borderEnd;
            this.backgroundStart = background;
            this.backgroundEnd = background;
            this.borderStart = borderStart;
            this.borderEnd = borderEnd;
        }

        /** Wrapper constructor */
        public Color(net.minecraftforge.client.event.RenderTooltipEvent.Color forge) {
            super(forge.getItemStack(), forge.getGraphics(), forge.getX(), forge.getY(),
                  forge.getFont(), forge.getComponents());
            this.originalBackground = forge.getOriginalBackgroundStart();
            this.originalBorderStart = forge.getOriginalBorderStart();
            this.originalBorderEnd = forge.getOriginalBorderEnd();
            this.backgroundStart = forge.getBackgroundStart();
            this.backgroundEnd = forge.getBackgroundEnd();
            this.borderStart = forge.getBorderStart();
            this.borderEnd = forge.getBorderEnd();
        }

        public int getBackgroundStart() { return backgroundStart; }
        public int getBackgroundEnd() { return backgroundEnd; }
        public int getBorderStart() { return borderStart; }
        public int getBorderEnd() { return borderEnd; }
        public void setBackgroundStart(int bg) { backgroundStart = bg; }
        public void setBackgroundEnd(int bg) { backgroundEnd = bg; }
        public void setBorderStart(int b) { borderStart = b; }
        public void setBorderEnd(int b) { borderEnd = b; }
        public int getOriginalBackgroundStart() { return originalBackground; }
        public int getOriginalBackgroundEnd() { return originalBackground; }
        public int getOriginalBorderStart() { return originalBorderStart; }
        public int getOriginalBorderEnd() { return originalBorderEnd; }
    }
}

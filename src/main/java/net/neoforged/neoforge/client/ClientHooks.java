package net.neoforged.neoforge.client;

import com.mojang.datafixers.util.Either;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import java.util.function.Supplier;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderTooltipEvent;
import org.jetbrains.annotations.NotNull;

/**
 * NeoForge ClientHooks — delegates to Forge's ForgeHooksClient.
 * Method signatures match NeoForge's API; the implementation forwards to Forge.
 */
public class ClientHooks {

    // ── GUI Layer Management ──────────────────────────────

    public static void pushGuiLayer(Minecraft minecraft, Screen screen) {
        ForgeHooksClient.pushGuiLayer(minecraft, screen);
    }

    public static void popGuiLayer(Minecraft minecraft) {
        ForgeHooksClient.popGuiLayer(minecraft);
    }

    public static void clearGuiLayers(Minecraft minecraft) {
        ForgeHooksClient.clearGuiLayers(minecraft);
    }

    public static void resizeGuiLayers(Minecraft minecraft, int width, int height) {
        ForgeHooksClient.resizeGuiLayers(minecraft, width, height);
    }

    public static float getGuiFarPlane() {
        return ForgeHooksClient.getGuiFarPlane();
    }

    public static void onTextureAtlasStitched(TextureAtlas atlas) {
        // Forge 1.21.1 does not expose a matching public post-stitch event here.
        // Keep the NeoForge API entrypoint to preserve binary compatibility.
    }

    public static void onBlockColorsInit(BlockColors blockColors) {
        ForgeHooksClient.onBlockColorsInit(blockColors);
    }

    public static void onItemColorsInit(ItemColors itemColors, BlockColors blockColors) {
        ForgeHooksClient.onItemColorsInit(itemColors, blockColors);
    }

    public static void registerLayerDefinition(ModelLayerLocation layerLocation, Supplier<LayerDefinition> supplier) {
        ForgeHooksClient.registerLayerDefinition(layerLocation, supplier);
    }

    public static void loadLayerDefinitions(ImmutableMap.Builder<ModelLayerLocation, LayerDefinition> builder) {
        ForgeHooksClient.loadLayerDefinitions(builder);
    }

    // ── Screen Rendering ──────────────────────────────────

    public static void drawScreen(Screen screen, GuiGraphics guiGraphics,
                                   int mouseX, int mouseY, float partialTick) {
        ForgeHooksClient.drawScreen(screen, guiGraphics, mouseX, mouseY, partialTick);
    }

    // ── Tooltip Methods ───────────────────────────────────

    public static Font getTooltipFont(@NotNull ItemStack stack, Font fallbackFont) {
        return ForgeHooksClient.getTooltipFont(stack, fallbackFont);
    }

    public static RenderTooltipEvent.Pre onRenderTooltipPre(
            @NotNull ItemStack stack, GuiGraphics graphics, int x, int y,
            int screenWidth, int screenHeight,
            @NotNull List<ClientTooltipComponent> components,
            @NotNull Font fallbackFont,
            @NotNull ClientTooltipPositioner positioner) {
        return ForgeHooksClient.onRenderTooltipPre(
                stack, graphics, x, y, screenWidth, screenHeight,
                components, fallbackFont, positioner);
    }

    public static RenderTooltipEvent.Color onRenderTooltipColor(
            @NotNull ItemStack stack, GuiGraphics graphics, int x, int y,
            @NotNull Font font, @NotNull List<ClientTooltipComponent> components) {
        return ForgeHooksClient.onRenderTooltipColor(stack, graphics, x, y, font, components);
    }

    public static List<ClientTooltipComponent> gatherTooltipComponents(
            ItemStack stack, List<? extends FormattedText> textElements,
            int mouseX, int screenWidth, int screenHeight, Font fallbackFont) {
        return ForgeHooksClient.gatherTooltipComponents(
                stack, textElements, mouseX, screenWidth, screenHeight, fallbackFont);
    }

    public static List<ClientTooltipComponent> gatherTooltipComponents(
            ItemStack stack, List<? extends FormattedText> textElements,
            Optional<TooltipComponent> itemComponent,
            int mouseX, int screenWidth, int screenHeight, Font fallbackFont) {
        return ForgeHooksClient.gatherTooltipComponents(
                stack, textElements, itemComponent, mouseX, screenWidth, screenHeight, fallbackFont);
    }

    public static List<ClientTooltipComponent> gatherTooltipComponentsFromElements(
            ItemStack stack, List<Either<FormattedText, TooltipComponent>> elements,
            int mouseX, int screenWidth, int screenHeight, Font fallbackFont) {
        return ForgeHooksClient.gatherTooltipComponentsFromElements(
                stack, elements, mouseX, screenWidth, screenHeight, fallbackFont);
    }

    /**
     * Triggers a renderer reload (e.g. when experimental pipeline setting changes).
     */
    public static void reloadRenderer() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.levelRenderer != null) {
            mc.levelRenderer.allChanged();
        }
    }
}

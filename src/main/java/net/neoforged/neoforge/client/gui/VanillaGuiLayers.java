package net.neoforged.neoforge.client.gui;

import net.minecraft.resources.ResourceLocation;

/**
 * Proxy: NeoForge's VanillaGuiLayers — provides ResourceLocation constants for GUI layers.
 * Fields match NeoForge 1.21.1 VanillaGuiLayers, in render order.
 */
public final class VanillaGuiLayers {
    private VanillaGuiLayers() {}

    public static final ResourceLocation CAMERA_OVERLAYS = ResourceLocation.withDefaultNamespace("camera_overlays");
    public static final ResourceLocation CROSSHAIR = ResourceLocation.withDefaultNamespace("crosshair");
    public static final ResourceLocation HOTBAR = ResourceLocation.withDefaultNamespace("hotbar");
    public static final ResourceLocation JUMP_METER = ResourceLocation.withDefaultNamespace("jump_meter");
    public static final ResourceLocation EXPERIENCE_BAR = ResourceLocation.withDefaultNamespace("experience_bar");
    public static final ResourceLocation PLAYER_HEALTH = ResourceLocation.withDefaultNamespace("player_health");
    public static final ResourceLocation ARMOR_LEVEL = ResourceLocation.withDefaultNamespace("armor_level");
    public static final ResourceLocation FOOD_LEVEL = ResourceLocation.withDefaultNamespace("food_level");
    public static final ResourceLocation VEHICLE_HEALTH = ResourceLocation.withDefaultNamespace("vehicle_health");
    public static final ResourceLocation AIR_LEVEL = ResourceLocation.withDefaultNamespace("air_level");
    public static final ResourceLocation SELECTED_ITEM_NAME = ResourceLocation.withDefaultNamespace("selected_item_name");
    public static final ResourceLocation SPECTATOR_TOOLTIP = ResourceLocation.withDefaultNamespace("spectator_tooltip");
    public static final ResourceLocation EXPERIENCE_LEVEL = ResourceLocation.withDefaultNamespace("experience_level");
    public static final ResourceLocation EFFECTS = ResourceLocation.withDefaultNamespace("effects");
    public static final ResourceLocation BOSS_OVERLAY = ResourceLocation.withDefaultNamespace("boss_overlay");
    public static final ResourceLocation SLEEP_OVERLAY = ResourceLocation.withDefaultNamespace("sleep_overlay");
    public static final ResourceLocation DEMO_OVERLAY = ResourceLocation.withDefaultNamespace("demo_overlay");
    public static final ResourceLocation DEBUG_OVERLAY = ResourceLocation.withDefaultNamespace("debug_overlay");
    public static final ResourceLocation SCOREBOARD_SIDEBAR = ResourceLocation.withDefaultNamespace("scoreboard_sidebar");
    public static final ResourceLocation OVERLAY_MESSAGE = ResourceLocation.withDefaultNamespace("overlay_message");
    public static final ResourceLocation TITLE = ResourceLocation.withDefaultNamespace("title");
    public static final ResourceLocation CHAT = ResourceLocation.withDefaultNamespace("chat");
    public static final ResourceLocation TAB_LIST = ResourceLocation.withDefaultNamespace("tab_list");
    public static final ResourceLocation SUBTITLE_OVERLAY = ResourceLocation.withDefaultNamespace("subtitle_overlay");
    public static final ResourceLocation SAVING_INDICATOR = ResourceLocation.withDefaultNamespace("saving_indicator");

    /** @deprecated Use {@link #BOSS_OVERLAY} instead — kept for backward compatibility */
    @Deprecated
    public static final ResourceLocation BOSS_EVENT_PROGRESS = BOSS_OVERLAY;
}

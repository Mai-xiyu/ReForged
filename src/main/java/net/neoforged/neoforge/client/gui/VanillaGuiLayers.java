package net.neoforged.neoforge.client.gui;

import net.minecraft.resources.ResourceLocation;

/** Proxy: NeoForge's VanillaGuiLayers — provides ResourceLocation constants for GUI layers */
public final class VanillaGuiLayers {
    private VanillaGuiLayers() {}

    public static final ResourceLocation BOSS_EVENT_PROGRESS = ResourceLocation.fromNamespaceAndPath("minecraft", "boss_event_progress");
    public static final ResourceLocation ARMOR_LEVEL = ResourceLocation.fromNamespaceAndPath("minecraft", "armor_level");
    public static final ResourceLocation PLAYER_HEALTH = ResourceLocation.fromNamespaceAndPath("minecraft", "player_health");
    public static final ResourceLocation EXPERIENCE_BAR = ResourceLocation.fromNamespaceAndPath("minecraft", "experience_bar");
    public static final ResourceLocation HOTBAR = ResourceLocation.fromNamespaceAndPath("minecraft", "hotbar");
    public static final ResourceLocation CROSSHAIR = ResourceLocation.fromNamespaceAndPath("minecraft", "crosshair");
}

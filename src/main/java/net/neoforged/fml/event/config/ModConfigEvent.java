package net.neoforged.fml.event.config;

import net.minecraftforge.eventbus.api.Event;

/**
 * Proxy: NeoForge's ModConfigEvent fired when configs are loaded/reloaded.
 */
public class ModConfigEvent extends Event {
    private final net.neoforged.fml.config.ModConfig config;

    public ModConfigEvent(net.neoforged.fml.config.ModConfig config) {
        this.config = config;
    }

    public net.neoforged.fml.config.ModConfig getConfig() { return config; }

    public static class Loading extends ModConfigEvent {
        public Loading(net.neoforged.fml.config.ModConfig config) { super(config); }
    }

    public static class Reloading extends ModConfigEvent {
        public Reloading(net.neoforged.fml.config.ModConfig config) { super(config); }
    }
}

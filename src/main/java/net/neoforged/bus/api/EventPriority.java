package net.neoforged.bus.api;

/**
 * Proxy for NeoForge's {@code EventPriority} enum.
 * Mirrors Forge's {@link net.minecraftforge.eventbus.api.EventPriority} with the same constants.
 */
public enum EventPriority {
    HIGHEST,
    HIGH,
    NORMAL,
    LOW,
    LOWEST;

    /**
     * Convert this NeoForge EventPriority to Forge's EventPriority.
     */
    public net.minecraftforge.eventbus.api.EventPriority toForge() {
        return net.minecraftforge.eventbus.api.EventPriority.values()[this.ordinal()];
    }

    /**
     * Convert from Forge's EventPriority.
     */
    public static EventPriority fromForge(net.minecraftforge.eventbus.api.EventPriority forge) {
        return values()[forge.ordinal()];
    }
}

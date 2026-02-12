package net.neoforged.fml.config;

/**
 * Proxy: NeoForge's IConfigSpec interface.
 * Forge doesn't have this exact interface — it uses ForgeConfigSpec directly.
 */
public interface IConfigSpec {
    default boolean isEmpty() { return false; }
    default void validateSpec() {}
}

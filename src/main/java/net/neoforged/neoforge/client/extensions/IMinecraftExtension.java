package net.neoforged.neoforge.client.extensions;

import java.util.Locale;
import net.minecraft.client.gui.screens.Screen;

/**
 * Extension interface for {@link net.minecraft.client.Minecraft}.
 * Provides NeoForge-specific convenience methods.
 */
public interface IMinecraftExtension {

    /**
     * Push a new GUI layer screen.
     */
    default void pushGuiLayer(Screen screen) {
        // Shim: delegate to ClientHooks in full implementation
    }

    /**
     * Pop the topmost GUI layer.
     */
    default void popGuiLayer() {
        // Shim: delegate to ClientHooks in full implementation
    }

    /**
     * Returns the player's configured locale.
     */
    default Locale getLocale() {
        return Locale.getDefault();
    }
}

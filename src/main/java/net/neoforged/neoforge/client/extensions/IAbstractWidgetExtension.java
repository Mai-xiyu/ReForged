package net.neoforged.neoforge.client.extensions;

import net.minecraft.client.gui.components.AbstractWidget;

/**
 * Extension interface for {@link AbstractWidget}.
 * Provides NeoForge's additional widget features.
 */
public interface IAbstractWidgetExtension {

    /**
     * Called when the widget is clicked with a specific mouse button.
     * Default delegates to the vanilla two-arg onClick.
     */
    default void onClick(double mouseX, double mouseY, int button) {
        ((AbstractWidget) this).onClick(mouseX, mouseY);
    }
}

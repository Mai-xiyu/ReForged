package net.neoforged.neoforge.client.gui;

import net.minecraft.client.gui.screens.Screen;

/** Proxy: NeoForge's IConfigScreenFactory */
@FunctionalInterface
public interface IConfigScreenFactory {
    Screen createScreen(Object container, Screen parent);
}

package net.neoforged.neoforge.client.extensions;

import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Extension interface for menu providers.
 * NeoForge uses this for additional data in screen open packets.
 */
public interface IMenuProviderExtension {

    /**
     * Whether opening this menu should close any existing client-side container first.
     * Return false to prevent mouse re-centering when opening layered menus.
     */
    default boolean shouldTriggerClientSideContainerClosingOnOpen() {
        return true;
    }
}

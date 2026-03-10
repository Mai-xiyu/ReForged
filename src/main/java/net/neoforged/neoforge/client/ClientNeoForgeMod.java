package net.neoforged.neoforge.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Client-side NeoForge mod initialization.
 * Registers client-side event handlers, config screen, and model loaders.
 */
@ApiStatus.Internal
public class ClientNeoForgeMod {
    public ClientNeoForgeMod(IEventBus modEventBus, ModContainer container) {
        ClientCommandHandler.init();
        TagConventionLogWarningClient.init();

        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}

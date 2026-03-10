package net.neoforged.neoforge.client.event;

import java.util.Map;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Fired to allow registration of menu screen factories.
 */
public class RegisterMenuScreensEvent extends net.neoforged.bus.api.Event implements IModBusEvent {
    private final Map<MenuType<?>, MenuScreens.ScreenConstructor<?, ?>> screens;

    public RegisterMenuScreensEvent(Map<MenuType<?>, MenuScreens.ScreenConstructor<?, ?>> screens) {
        this.screens = screens;
    }

    @SuppressWarnings("unchecked")
    public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(
            MenuType<? extends M> menuType, MenuScreens.ScreenConstructor<M, U> screenConstructor) {
        screens.put(menuType, screenConstructor);
    }
}

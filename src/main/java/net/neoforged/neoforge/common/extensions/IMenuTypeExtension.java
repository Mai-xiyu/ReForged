package net.neoforged.neoforge.common.extensions;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.network.IContainerFactory;

/**
 * Extension interface for MenuType that supports additional data.
 */
public interface IMenuTypeExtension {
    /**
     * Creates a MenuType that can receive additional data from the server via a buffer.
     */
    static <T extends AbstractContainerMenu> MenuType<T> create(IContainerFactory<T> factory) {
        return new MenuType<>(factory, net.minecraft.world.flag.FeatureFlags.VANILLA_SET);
    }
}

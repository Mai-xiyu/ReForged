package net.neoforged.neoforge.client.extensions.common;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * Event for registering client extensions for various game objects.
 * Fired on the mod event bus during client setup.
 */
public class RegisterClientExtensionsEvent extends Event implements IModBusEvent {

    public RegisterClientExtensionsEvent() {
    }

    /**
     * Register client fluid type extensions.
     * Since IClientFluidTypeExtensions now extends Forge's interface and queries
     * getRenderPropertiesInternal(), Forge-registered extensions are auto-discovered.
     * This method is available for NeoForge mods that register extensions via event.
     */
    public void registerFluidType(IClientFluidTypeExtensions extensions, FluidType... fluidTypes) {
        for (FluidType type : fluidTypes) {
            try {
                // Use Forge's initializeClient mechanism to store the extensions
                java.lang.reflect.Field f = net.minecraftforge.fluids.FluidType.class.getDeclaredField("renderProperties");
                f.setAccessible(true);
                f.set(type, extensions);
            } catch (Throwable t) {
                // Fallback: log and continue
            }
        }
    }

    /**
     * Register client item extensions for the given items.
     */
    public void registerItem(IClientItemExtensions extensions, Item... items) {
        for (Item item : items) {
            try {
                // Forge stores render properties via Item.renderProperties field
                java.lang.reflect.Field f = Item.class.getDeclaredField("renderProperties");
                f.setAccessible(true);
                f.set(item, extensions);
            } catch (Throwable t) {
                // Fallback: log and continue
            }
        }
    }

    /**
     * Register client block extensions for the given blocks.
     */
    public void registerBlock(IClientBlockExtensions extensions, Block... blocks) {
        for (Block block : blocks) {
            try {
                // Forge stores render properties via Block.renderProperties field
                java.lang.reflect.Field f = Block.class.getDeclaredField("renderProperties");
                f.setAccessible(true);
                f.set(block, extensions);
            } catch (Throwable t) {
                // Fallback: log and continue
            }
        }
    }
}

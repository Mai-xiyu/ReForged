package net.neoforged.neoforge.fluids;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.eventbus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Event fired to register cauldron fluid contents.
 * Fired on the mod event bus.
 */
public class RegisterCauldronFluidContentEvent extends Event implements IModBusEvent {
    public RegisterCauldronFluidContentEvent() {}

    /**
     * Register a cauldron fluid content.
     */
    public void register(Block block, Fluid fluid, int totalAmount, @Nullable IntegerProperty levelProperty) {
        CauldronFluidContent.register(block, fluid, totalAmount, levelProperty);
    }
}

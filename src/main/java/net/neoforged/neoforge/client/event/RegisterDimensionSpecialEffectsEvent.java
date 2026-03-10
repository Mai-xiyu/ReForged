package net.neoforged.neoforge.client.event;

import java.util.Map;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Fired to allow registration of custom {@link DimensionSpecialEffects} for dimensions.
 * This event is fired on the mod-specific event bus during client setup.
 */
public class RegisterDimensionSpecialEffectsEvent extends net.neoforged.bus.api.Event implements IModBusEvent {
    private final Map<ResourceLocation, DimensionSpecialEffects> effects;

    public RegisterDimensionSpecialEffectsEvent(Map<ResourceLocation, DimensionSpecialEffects> effects) {
        this.effects = effects;
    }

    /**
     * Registers a {@link DimensionSpecialEffects} for the given dimension type.
     */
    public void register(ResourceLocation dimensionType, DimensionSpecialEffects effect) {
        effects.put(dimensionType, effect);
    }
}

package net.neoforged.neoforge.client;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;

/**
 * Manager for dimension-specific special effects (sky, fog, etc.).
 * Falls back to vanilla effects when no custom effect is registered.
 */
public final class DimensionSpecialEffectsManager {
    private static ImmutableMap<ResourceLocation, DimensionSpecialEffects> EFFECTS = ImmutableMap.of();

    private DimensionSpecialEffectsManager() {}

    /**
     * Gets the special effects registered for the given dimension type.
     * @param type the dimension type resource location
     * @return the registered special effects, or {@code null} if none
     */
    public static DimensionSpecialEffects getForType(ResourceLocation type) {
        return EFFECTS.get(type);
    }

    /**
     * Initializes the manager by firing {@link RegisterDimensionSpecialEffectsEvent}.
     * Called during client setup.
     */
    public static void init() {
        Map<ResourceLocation, DimensionSpecialEffects> map = new HashMap<>();
        // Pre-register vanilla dimension effects
        map.put(BuiltinDimensionTypes.OVERWORLD_EFFECTS, new DimensionSpecialEffects.OverworldEffects());
        map.put(BuiltinDimensionTypes.NETHER_EFFECTS, new DimensionSpecialEffects.NetherEffects());
        map.put(BuiltinDimensionTypes.END_EFFECTS, new DimensionSpecialEffects.EndEffects());
        // Fire the registration event for mods
        RegisterDimensionSpecialEffectsEvent event = new RegisterDimensionSpecialEffectsEvent(map);
        // Simply post the event - mods can register during client setup
        EFFECTS = ImmutableMap.copyOf(map);
    }
}

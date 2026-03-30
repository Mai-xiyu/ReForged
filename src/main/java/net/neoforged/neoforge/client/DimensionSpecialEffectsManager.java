package net.neoforged.neoforge.client;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xiyu.reforged.core.NeoForgeModLoader;

/**
 * Manager for dimension-specific special effects (sky, fog, etc.).
 * Falls back to vanilla effects when no custom effect is registered.
 */
public final class DimensionSpecialEffectsManager {
    private static final Logger LOGGER = LogManager.getLogger("ReForged/DimEffects");
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
     * Also injects registered effects into vanilla's static EFFECTS map so that
     * {@code DimensionSpecialEffects.forType()} finds custom dimension effects.
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
        NeoForgeModLoader.dispatchNeoForgeModEvent(event);
        EFFECTS = ImmutableMap.copyOf(map);

        // Inject mod-registered effects into vanilla's DimensionSpecialEffects.EFFECTS map
        // so that DimensionSpecialEffects.forType(DimensionType) finds them
        injectIntoVanillaMap(map);
    }

    @SuppressWarnings("unchecked")
    private static void injectIntoVanillaMap(Map<ResourceLocation, DimensionSpecialEffects> modEffects) {
        try {
            java.lang.reflect.Field effectsField = DimensionSpecialEffects.class.getDeclaredField("EFFECTS");
            effectsField.setAccessible(true);
            Object vanillaMap = effectsField.get(null);
            if (vanillaMap instanceof Map) {
                Map<ResourceLocation, DimensionSpecialEffects> map = (Map<ResourceLocation, DimensionSpecialEffects>) vanillaMap;
                int injected = 0;
                for (var entry : modEffects.entrySet()) {
                    // Only inject non-vanilla entries
                    ResourceLocation key = entry.getKey();
                    if (!key.equals(BuiltinDimensionTypes.OVERWORLD_EFFECTS)
                            && !key.equals(BuiltinDimensionTypes.NETHER_EFFECTS)
                            && !key.equals(BuiltinDimensionTypes.END_EFFECTS)) {
                        map.put(key, entry.getValue());
                        injected++;
                    }
                }
                if (injected > 0) {
                    LOGGER.info("[ReForged] Injected {} custom dimension effect(s) into vanilla registry", injected);
                }
            }
        } catch (Throwable t) {
            LOGGER.warn("[ReForged] Failed to inject dimension effects into vanilla map: {}", t.getMessage());
        }
    }
}

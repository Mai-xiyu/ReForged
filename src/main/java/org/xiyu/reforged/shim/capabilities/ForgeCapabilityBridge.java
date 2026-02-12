package org.xiyu.reforged.shim.capabilities;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ForgeCapabilityBridge — Maps NeoForge capability ResourceLocations to
 * Forge's Capability objects.
 *
 * <p>NeoForge introduced a new capability resolution system based on
 * ResourceLocations and static factory methods. Forge still uses the
 * old {@code Capability<T>} tokens from {@code ForgeCapabilities}.</p>
 *
 * <p>This bridge provides the translation layer.</p>
 */
public final class ForgeCapabilityBridge {

    private static final Logger LOGGER = LogUtils.getLogger();

    /** Map of NeoForge capability RL → Forge Capability token */
    private static final Map<ResourceLocation, Capability<?>> CAPABILITY_MAP = new ConcurrentHashMap<>();

    static {
        // Initialize known capability mappings
        try {
            CAPABILITY_MAP.put(ResourceLocation.parse("neoforge:item_handler"), ForgeCapabilities.ITEM_HANDLER);
            CAPABILITY_MAP.put(ResourceLocation.parse("neoforge:fluid_handler"), ForgeCapabilities.FLUID_HANDLER);
            CAPABILITY_MAP.put(ResourceLocation.parse("neoforge:energy"), ForgeCapabilities.ENERGY);
            CAPABILITY_MAP.put(ResourceLocation.parse("neoforge:fluid_handler_item"), ForgeCapabilities.FLUID_HANDLER_ITEM);
            LOGGER.info("[ReForged] ForgeCapabilityBridge: Mapped {} NeoForge capabilities to Forge equivalents",
                    CAPABILITY_MAP.size());
        } catch (Exception e) {
            LOGGER.warn("[ReForged] ForgeCapabilityBridge: Failed to initialize capability mappings", e);
        }
    }

    /**
     * Find the Forge Capability token for a NeoForge capability ResourceLocation.
     *
     * @param neoCapName the NeoForge capability name
     * @param typeClass  the expected type class (for validation)
     * @return the Forge Capability, or null if not found
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> Capability<T> findForgeCapability(ResourceLocation neoCapName, Class<T> typeClass) {
        Capability<?> cap = CAPABILITY_MAP.get(neoCapName);
        if (cap != null) {
            return (Capability<T>) cap;
        }
        LOGGER.debug("[ReForged] No Forge capability mapping for: {}", neoCapName);
        return null;
    }

    /**
     * Register a custom capability mapping.
     */
    public static void registerMapping(ResourceLocation neoName, Capability<?> forgeCap) {
        CAPABILITY_MAP.put(neoName, forgeCap);
        LOGGER.info("[ReForged] Registered custom capability mapping: {} → {}", neoName, forgeCap.getName());
    }

    private ForgeCapabilityBridge() {}
}

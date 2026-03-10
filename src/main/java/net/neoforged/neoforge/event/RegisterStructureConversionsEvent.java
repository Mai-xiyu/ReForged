package net.neoforged.neoforge.event;

import java.util.Map;
import java.util.HashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired to register structure conversions for world upgrading.
 */
public class RegisterStructureConversionsEvent extends Event {
    private final Map<String, ResourceLocation> conversions = new HashMap<>();

    public RegisterStructureConversionsEvent() {}

    /**
     * Register a structure conversion from an old name to a new ResourceLocation.
     */
    public void register(String oldName, ResourceLocation newId) {
        conversions.put(oldName, newId);
    }

    public Map<String, ResourceLocation> getConversions() {
        return conversions;
    }
}

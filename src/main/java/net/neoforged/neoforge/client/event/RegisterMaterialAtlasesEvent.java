package net.neoforged.neoforge.client.event;

import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Stub: Fired to register material atlases.
 */
public class RegisterMaterialAtlasesEvent extends net.neoforged.bus.api.Event implements IModBusEvent {
    private final Map<ResourceLocation, ResourceLocation> atlases;

    public RegisterMaterialAtlasesEvent(Map<ResourceLocation, ResourceLocation> atlases) {
        this.atlases = atlases;
    }

    public void register(ResourceLocation atlasLocation, ResourceLocation atlasInfoLocation) {
        ResourceLocation old = atlases.putIfAbsent(atlasLocation, atlasInfoLocation);
        if (old != null) {
            throw new IllegalStateException("Duplicate registration of atlas: " + atlasLocation + " (old info: " + old + ", new info: " + atlasInfoLocation + ")");
        }
    }
}

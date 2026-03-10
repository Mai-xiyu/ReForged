package net.neoforged.neoforge.client.event;

import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Stub: Fired to register entity spectator shaders.
 */
public class RegisterEntitySpectatorShadersEvent extends net.neoforged.bus.api.Event implements IModBusEvent {
    private final Map<EntityType<?>, ResourceLocation> shaders;

    public RegisterEntitySpectatorShadersEvent(Map<EntityType<?>, ResourceLocation> shaders) {
        this.shaders = shaders;
    }

    public void register(EntityType<?> entityType, ResourceLocation shader) {
        shaders.put(entityType, shader);
    }
}

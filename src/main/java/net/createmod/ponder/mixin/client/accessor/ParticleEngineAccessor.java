package net.createmod.ponder.mixin.client.accessor;

import java.util.Map;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.resources.ResourceLocation;

public interface ParticleEngineAccessor {
    Map<ResourceLocation, ParticleProvider<?>> ponder$getProviders();
}

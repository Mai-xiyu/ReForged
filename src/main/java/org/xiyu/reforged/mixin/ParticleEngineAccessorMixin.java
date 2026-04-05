package org.xiyu.reforged.mixin;

import java.util.Map;
import net.createmod.ponder.mixin.client.accessor.ParticleEngineAccessor;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ParticleEngine.class, remap = false)
public abstract class ParticleEngineAccessorMixin implements ParticleEngineAccessor {
    @Shadow
    @Final
    private Map<ResourceLocation, ParticleProvider<?>> providers;

    @Override
    public Map<ResourceLocation, ParticleProvider<?>> ponder$getProviders() {
        return this.providers;
    }
}

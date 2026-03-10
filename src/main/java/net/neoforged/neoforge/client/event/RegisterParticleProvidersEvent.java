package net.neoforged.neoforge.client.event;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Wrapper around Forge's {@link net.minecraftforge.client.event.RegisterParticleProvidersEvent}.
 */
public class RegisterParticleProvidersEvent extends net.neoforged.bus.api.Event implements IModBusEvent {
    private final net.minecraftforge.client.event.RegisterParticleProvidersEvent delegate;
    private final ParticleEngine particleEngine;

    public RegisterParticleProvidersEvent(net.minecraftforge.client.event.RegisterParticleProvidersEvent delegate) {
        this.delegate = delegate;
        this.particleEngine = null;
    }

    public RegisterParticleProvidersEvent(ParticleEngine particleEngine) {
		this.delegate = null;
		this.particleEngine = particleEngine;
    }

    public <T extends ParticleOptions> void registerSpriteSet(ParticleType<T> type,
                                                               ParticleEngine.SpriteParticleRegistration<T> registration) {
		if (delegate != null) {
        	delegate.registerSpriteSet(type, registration);
		} else {
			particleEngine.register(type, registration);
		}
    }

    @SuppressWarnings("deprecation")
    public <T extends ParticleOptions> void registerSprite(ParticleType<T> type,
                                                            ParticleProvider.Sprite<T> sprite) {
		if (delegate != null) {
			delegate.registerSprite(type, sprite);
		} else {
			particleEngine.register(type, sprite);
		}
	}

    public <T extends ParticleOptions> void registerSpecial(ParticleType<T> type,
                                                             ParticleProvider<T> provider) {
		if (delegate != null) {
        	delegate.registerSpecial(type, provider);
		} else {
			particleEngine.register(type, provider);
		}
    }
}

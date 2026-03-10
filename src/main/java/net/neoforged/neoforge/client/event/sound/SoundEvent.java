package net.neoforged.neoforge.client.event.sound;

import com.mojang.blaze3d.audio.Channel;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.neoforged.bus.api.Event;

/**
 * Superclass for sound related events.
 */
public abstract class SoundEvent extends Event {
    private final SoundEngine engine;

    protected SoundEvent(SoundEngine engine) {
        this.engine = engine;
    }

    public SoundEngine getEngine() { return engine; }

    /**
     * Superclass for when a sound has started to play on an audio channel.
     */
    public static abstract class SoundSourceEvent extends SoundEvent {
        private final SoundInstance sound;
        private final Channel channel;
        private final String name;

        protected SoundSourceEvent(SoundEngine engine, SoundInstance sound, Channel channel) {
            super(engine);
            this.name = sound.getLocation().getPath();
            this.sound = sound;
            this.channel = channel;
        }

        public SoundInstance getSound() { return sound; }
        public Channel getChannel() { return channel; }
        public String getName() { return name; }
    }
}

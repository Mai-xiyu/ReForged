package net.neoforged.neoforge.client.event.sound;

import com.mojang.blaze3d.audio.Channel;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;

/**
 * Fired when a non-streaming sound is being played on an audio channel.
 */
public class PlaySoundSourceEvent extends SoundEvent.SoundSourceEvent {
    public PlaySoundSourceEvent(SoundEngine engine, SoundInstance sound, Channel channel) {
        super(engine, sound, channel);
    }
}

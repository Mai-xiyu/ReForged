package net.neoforged.neoforge.client.event.sound;

import net.minecraft.client.sounds.SoundEngine;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Fired when the sound engine is constructed or reloaded.
 */
public class SoundEngineLoadEvent extends SoundEvent implements IModBusEvent {
    public SoundEngineLoadEvent(SoundEngine manager) {
        super(manager);
    }
}

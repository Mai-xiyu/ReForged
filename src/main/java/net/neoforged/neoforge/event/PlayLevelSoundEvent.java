package net.neoforged.neoforge.event;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import org.jetbrains.annotations.Nullable;

@Cancelable
public class PlayLevelSoundEvent extends Event implements ICancellableEvent {
    @Nullable
    private final net.minecraftforge.event.PlayLevelSoundEvent forgeDelegate;
    private final Level level;
    private final float originalVolume;
    private final float originalPitch;
    private Holder<SoundEvent> sound;
    private SoundSource source;
    private float newVolume;
    private float newPitch;

    public PlayLevelSoundEvent(Level level, Holder<SoundEvent> sound, SoundSource source, float volume, float pitch) {
        this.forgeDelegate = null;
        this.level = level;
        this.sound = sound;
        this.source = source;
        this.originalVolume = volume;
        this.originalPitch = pitch;
        this.newVolume = volume;
        this.newPitch = pitch;
    }

    public PlayLevelSoundEvent(net.minecraftforge.event.PlayLevelSoundEvent forge) {
        this.forgeDelegate = forge;
        this.level = forge.getLevel();
        this.sound = forge.getSound();
        this.source = forge.getSource();
        this.originalVolume = forge.getOriginalVolume();
        this.originalPitch = forge.getOriginalPitch();
        this.newVolume = forge.getOriginalVolume();
        this.newPitch = forge.getOriginalPitch();
        this.newVolume = forge.getNewVolume();
        this.newPitch = forge.getNewPitch();
    }

    public Level getLevel() {
        return level;
    }

    @Nullable
    public Holder<SoundEvent> getSound() {
        return sound;
    }

    public void setSound(@Nullable Holder<SoundEvent> sound) {
        this.sound = sound;
        if (forgeDelegate != null) {
            forgeDelegate.setSound(sound);
        }
    }

    public SoundSource getSource() {
        return source;
    }

    public void setSource(SoundSource source) {
        this.source = source;
        if (forgeDelegate != null) {
            forgeDelegate.setSource(source);
        }
    }

    public float getOriginalVolume() {
        return originalVolume;
    }

    public float getOriginalPitch() {
        return originalPitch;
    }

    public float getNewVolume() {
        return newVolume;
    }

    public void setNewVolume(float newVolume) {
        this.newVolume = newVolume;
        if (forgeDelegate != null) {
            forgeDelegate.setNewVolume(newVolume);
        }
    }

    public float getNewPitch() {
        return newPitch;
    }

    public void setNewPitch(float newPitch) {
        this.newPitch = newPitch;
        if (forgeDelegate != null) {
            forgeDelegate.setNewPitch(newPitch);
        }
    }

    @Override
    public void setCanceled(boolean canceled) {
        super.setCanceled(canceled);
        if (forgeDelegate != null) {
            forgeDelegate.setCanceled(canceled);
        }
    }

    public static class AtPosition extends PlayLevelSoundEvent {
        private final Vec3 position;

        public AtPosition(Level level, Vec3 position, Holder<SoundEvent> sound, SoundSource source, float volume, float pitch) {
            super(level, sound, source, volume, pitch);
            this.position = position;
        }

        public AtPosition(net.minecraftforge.event.PlayLevelSoundEvent.AtPosition forge) {
            super(forge);
            this.position = forge.getPosition();
        }

        public Vec3 getPosition() {
            return position;
        }
    }

    public static class AtEntity extends PlayLevelSoundEvent {
        private final Entity entity;

        public AtEntity(Entity entity, Holder<SoundEvent> sound, SoundSource source, float volume, float pitch) {
            super(entity.level(), sound, source, volume, pitch);
            this.entity = entity;
        }

        public AtEntity(net.minecraftforge.event.PlayLevelSoundEvent.AtEntity forge) {
            super(forge);
            this.entity = forge.getEntity();
        }

        public Entity getEntity() {
            return entity;
        }
    }
}

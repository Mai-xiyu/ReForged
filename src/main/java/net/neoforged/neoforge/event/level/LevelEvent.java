package net.neoforged.neoforge.event.level;

import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.Event;

/**
 * NeoForge LevelEvent with level field and Forge wrapper constructors.
 */
public class LevelEvent extends Event {
    private LevelAccessor level;

    /** Required by Forge's EventListenerHelper */
    public LevelEvent() {}

    public LevelEvent(LevelAccessor level) {
        this.level = level;
    }

    /** Wrapper constructor */
    public LevelEvent(net.minecraftforge.event.level.LevelEvent forge) {
        this.level = forge.getLevel();
    }

    public LevelAccessor getLevel() { return level; }

    public static class Load extends LevelEvent {
        /** Required by Forge's EventListenerHelper */
        public Load() { super(); }

        public Load(LevelAccessor level) { super(level); }

        /** Wrapper constructor */
        public Load(net.minecraftforge.event.level.LevelEvent.Load forge) {
            super(forge);
        }
    }

    public static class Unload extends LevelEvent {
        /** Required by Forge's EventListenerHelper */
        public Unload() { super(); }

        public Unload(LevelAccessor level) { super(level); }

        /** Wrapper constructor */
        public Unload(net.minecraftforge.event.level.LevelEvent.Unload forge) {
            super(forge);
        }
    }

    public static class PotentialSpawns extends LevelEvent {
        /** Required by Forge's EventListenerHelper */
        public PotentialSpawns() { super(); }

        public PotentialSpawns(LevelAccessor level) { super(level); }

        /** Wrapper constructor */
        public PotentialSpawns(net.minecraftforge.event.level.LevelEvent.PotentialSpawns forge) {
            super(forge);
        }
    }
}

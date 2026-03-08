package net.neoforged.neoforge.event.entity;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;

/**
 * NeoForge EntityEvent with entity field and Forge wrapper constructors.
 */
public class EntityEvent extends Event {
    private final Entity entity;

    /** Required by Forge's EventListenerHelper */
    public EntityEvent() {
        this.entity = null;
    }

    public EntityEvent(Entity entity) {
        this.entity = entity;
    }

    /** Wrapper constructor */
    public EntityEvent(net.minecraftforge.event.entity.EntityEvent forge) {
        this.entity = forge.getEntity();
    }

    public Entity getEntity() { return entity; }

    public static class EnteringSection extends EntityEvent {
        private long packedOldPos;
        private long packedNewPos;

        public EnteringSection() {}

        public EnteringSection(Entity entity, long packedOldPos, long packedNewPos) {
            super(entity);
            this.packedOldPos = packedOldPos;
            this.packedNewPos = packedNewPos;
        }

        /** Wrapper constructor */
        public EnteringSection(net.minecraftforge.event.entity.EntityEvent.EnteringSection forge) {
            super(forge.getEntity());
            this.packedOldPos = forge.getPackedOldPos();
            this.packedNewPos = forge.getPackedNewPos();
        }

        public long getPackedOldPos() { return packedOldPos; }
        public long getPackedNewPos() { return packedNewPos; }
    }

    public static class Size extends EntityEvent {
        public Size() {}
        public Size(Entity entity) { super(entity); }
    }
}

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

        /**
         * Whether the entity moved to a different chunk (NeoForge extension).
         */
        public boolean didChunkChange() {
            return net.minecraft.core.SectionPos.x(packedOldPos) != net.minecraft.core.SectionPos.x(packedNewPos)
                || net.minecraft.core.SectionPos.z(packedOldPos) != net.minecraft.core.SectionPos.z(packedNewPos);
        }
    }

    public static class Size extends EntityEvent {
        private net.minecraft.world.entity.Pose pose;
        private net.minecraft.world.entity.EntityDimensions oldSize;
        private net.minecraft.world.entity.EntityDimensions newSize;
        private float newEyeHeight;

        public Size() {}

        public Size(Entity entity) { super(entity); }

        public Size(Entity entity, net.minecraft.world.entity.Pose pose,
                    net.minecraft.world.entity.EntityDimensions oldSize,
                    net.minecraft.world.entity.EntityDimensions newSize) {
            super(entity);
            this.pose = pose;
            this.oldSize = oldSize;
            this.newSize = newSize;
            this.newEyeHeight = newSize.eyeHeight();
        }

        public net.minecraft.world.entity.Pose getPose() { return pose; }
        public net.minecraft.world.entity.EntityDimensions getOldSize() { return oldSize; }
        public net.minecraft.world.entity.EntityDimensions getNewSize() { return newSize; }
        public float getNewEyeHeight() { return newEyeHeight; }

        public void setNewSize(net.minecraft.world.entity.EntityDimensions size) {
            this.newSize = size;
        }

        public void setNewEyeHeight(float eyeHeight) {
            this.newEyeHeight = eyeHeight;
        }
    }
}

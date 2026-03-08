package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.EntityEvent;

/**
 * NeoForge LivingEvent — extends EntityEvent for proper hierarchy.
 */
public class LivingEvent extends EntityEvent {

    /** Required by Forge's EventListenerHelper */
    public LivingEvent() {
        super((net.minecraft.world.entity.Entity) null);
    }

    public LivingEvent(LivingEntity entity) {
        super(entity);
    }

    /** Wrapper constructor */
    public LivingEvent(net.minecraftforge.event.entity.living.LivingEvent forge) {
        super(forge.getEntity());
    }

    @Override
    public LivingEntity getEntity() {
        Entity e = super.getEntity();
        return e instanceof LivingEntity le ? le : null;
    }

    public static class LivingJumpEvent extends LivingEvent {
        /** Required by Forge's EventListenerHelper */
        public LivingJumpEvent() { super(); }

        public LivingJumpEvent(LivingEntity entity) { super(entity); }

        /** Wrapper constructor */
        public LivingJumpEvent(net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent forge) {
            super(forge);
        }
    }

    public static class LivingVisibilityEvent extends LivingEvent {
        private double visibilityModifier;
        private Entity lookingEntity;

        /** Required by Forge's EventListenerHelper */
        public LivingVisibilityEvent() { super(); }

        public LivingVisibilityEvent(LivingEntity entity, Entity lookingEntity, double originalMultiplier) {
            super(entity);
            this.lookingEntity = lookingEntity;
            this.visibilityModifier = originalMultiplier;
        }

        /** Wrapper constructor */
        public LivingVisibilityEvent(net.minecraftforge.event.entity.living.LivingEvent.LivingVisibilityEvent forge) {
            super(forge);
            this.lookingEntity = forge.getLookingEntity();
            this.visibilityModifier = forge.getVisibilityModifier();
        }

        public double getVisibilityModifier() { return visibilityModifier; }
        public void modifyVisibility(double mod) { visibilityModifier *= mod; }
        public Entity getLookingEntity() { return lookingEntity; }
    }
}

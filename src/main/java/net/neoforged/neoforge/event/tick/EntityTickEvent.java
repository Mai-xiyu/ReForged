package net.neoforged.neoforge.event.tick;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;

/**
 * NeoForge EntityTickEvent — wraps Forge's LivingTickEvent.
 * NeoForge fires for all entities; Forge only for LivingEntity.
 */
public class EntityTickEvent extends Event {
    private Entity entity;

    /** Required by Forge's EventListenerHelper */
    public EntityTickEvent() {}

    public EntityTickEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() { return entity; }

    public static class Pre extends EntityTickEvent {
        /** Required by Forge's EventListenerHelper */
        public Pre() { super(); }

        public Pre(Entity entity) { super(entity); }

        /** Wrapper constructor */
        public Pre(net.minecraftforge.event.entity.living.LivingEvent.LivingTickEvent forge) {
            super(forge.getEntity());
        }
    }

    public static class Post extends EntityTickEvent {
        /** Required by Forge's EventListenerHelper */
        public Post() { super(); }

        public Post(Entity entity) { super(entity); }
    }
}

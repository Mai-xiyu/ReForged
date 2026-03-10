package net.neoforged.neoforge.event.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fired when an entity mounts or dismounts another entity.
 */
public class EntityMountEvent extends Event implements ICancellableEvent {
    private final Entity entityMounting;
    private final Entity entityBeingMounted;
    private final Level level;
    private final boolean isMounting;

    public EntityMountEvent(Entity entityMounting, Entity entityBeingMounted, Level level, boolean isMounting) {
        this.entityMounting = entityMounting;
        this.entityBeingMounted = entityBeingMounted;
        this.level = level;
        this.isMounting = isMounting;
    }

    public boolean isMounting() { return isMounting; }
    public boolean isDismounting() { return !isMounting; }
    public Entity getEntityMounting() { return entityMounting; }
    public Entity getEntityBeingMounted() { return entityBeingMounted; }
    public Entity getEntity() { return entityMounting; }
    public Level getLevel() { return level; }
}

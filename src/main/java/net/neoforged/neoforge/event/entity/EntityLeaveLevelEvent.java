package net.neoforged.neoforge.event.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;

/**
 * Fired when an entity is removed from a level.
 */
public class EntityLeaveLevelEvent extends Event {
    private final Entity entity;
    private final Level level;

    public EntityLeaveLevelEvent(Entity entity, Level level) {
        this.entity = entity;
        this.level = level;
    }

    /** Forge wrapper constructor for automatic event bridging */
    public EntityLeaveLevelEvent(net.minecraftforge.event.entity.EntityLeaveLevelEvent delegate) {
        this(delegate.getEntity(), delegate.getLevel());
    }

    public Entity getEntity() { return entity; }
    public Level getLevel() { return level; }
}

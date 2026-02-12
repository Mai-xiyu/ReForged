package net.neoforged.neoforge.event.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

/**
 * Proxy: NeoForge's EntityJoinLevelEvent.
 * Same concept as Forge's version but in NeoForge's package.
 */
public class EntityJoinLevelEvent extends Event {
    private final Entity entity;
    private final Level level;

    public EntityJoinLevelEvent(Entity entity, Level level) {
        this.entity = entity;
        this.level = level;
    }

    public Entity getEntity() { return entity; }
    public Level getLevel() { return level; }
}

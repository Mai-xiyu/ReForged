package dev.engine_room.flywheel.impl.extension;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/**
 * Flywheel extension interface for Level.
 * This copy lives on TransformingClassLoader (via ReForged JAR) so that:
 * 1. The Mixin framework can apply it to Level
 * 2. NeoModClassLoader (parent-first for this package) delegates here
 * 3. Both classloaders share the same class identity → casts work
 */
public interface LevelExtension {
    Iterable<Entity> flywheel$getAllLoadedEntities();

    static Iterable<Entity> getAllLoadedEntities(Level level) {
        return ((LevelExtension) level).flywheel$getAllLoadedEntities();
    }
}

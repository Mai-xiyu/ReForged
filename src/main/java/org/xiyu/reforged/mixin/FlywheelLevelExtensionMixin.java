package org.xiyu.reforged.mixin;

import dev.engine_room.flywheel.impl.extension.LevelExtension;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.LevelEntityGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Replicates Flywheel's LevelMixin: makes Level implement LevelExtension.
 * This is needed because Flywheel's own mixin can't be applied (classloader boundary).
 */
@Mixin(value = Level.class, remap = false)
abstract class FlywheelLevelExtensionMixin implements LevelExtension {

    @Shadow(remap = false)
    protected abstract LevelEntityGetter<Entity> getEntities();

    @Override
    public Iterable<Entity> flywheel$getAllLoadedEntities() {
        return this.getEntities().getAll();
    }
}

package net.createmod.ponder.mixin.accessor;

import net.minecraft.world.level.Level;

/**
 * Accessor interface matching Ponder's EntityAccessor mixin.
 * Implemented via {@link org.xiyu.reforged.mixin.EntityAccessorMixin}.
 */
public interface EntityAccessor {
    void catnip$callSetLevel(Level level);
}

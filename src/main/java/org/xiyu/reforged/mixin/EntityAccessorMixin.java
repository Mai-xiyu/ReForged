package org.xiyu.reforged.mixin;

import net.createmod.ponder.mixin.accessor.EntityAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public abstract class EntityAccessorMixin implements EntityAccessor {

    @Shadow
    protected abstract void setLevel(Level level);

    @Override
    public void catnip$callSetLevel(Level level) {
        this.setLevel(level);
    }
}

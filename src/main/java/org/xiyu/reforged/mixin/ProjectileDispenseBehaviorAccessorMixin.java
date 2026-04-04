package org.xiyu.reforged.mixin;

import net.minecraft.core.dispenser.ProjectileDispenseBehavior;
import net.minecraft.world.item.ProjectileItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ProjectileDispenseBehavior.class)
public abstract class ProjectileDispenseBehaviorAccessorMixin {
    @Shadow @Final private ProjectileItem projectileItem;
    @Shadow @Final private ProjectileItem.DispenseConfig dispenseConfig;

    public ProjectileItem create$getProjectileItem() {
        return this.projectileItem;
    }

    public ProjectileItem.DispenseConfig create$getDispenseConfig() {
        return this.dispenseConfig;
    }
}

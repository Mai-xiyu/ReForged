package org.xiyu.reforged.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.entity.EntityTickList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerLevel.class)
public abstract class ServerLevelAccessorMixin {
    @Shadow @Final EntityTickList entityTickList;

    public EntityTickList create$getEntityTickList() {
        return this.entityTickList;
    }
}

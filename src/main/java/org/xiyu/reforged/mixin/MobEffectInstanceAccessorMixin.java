package org.xiyu.reforged.mixin;

import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MobEffectInstance.class)
public abstract class MobEffectInstanceAccessorMixin {
    @Shadow private MobEffectInstance hiddenEffect;

    public MobEffectInstance create$getHiddenEffect() {
        return this.hiddenEffect;
    }
}

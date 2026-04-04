package org.xiyu.reforged.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LivingEntity.class)
public abstract class LivingEntityAccessorMixin {
    @Shadow
    private void spawnItemParticles(ItemStack stack, int count) {
        throw new AssertionError();
    }

    public void create$callSpawnItemParticles(ItemStack stack, int count) {
        this.spawnItemParticles(stack, count);
    }
}

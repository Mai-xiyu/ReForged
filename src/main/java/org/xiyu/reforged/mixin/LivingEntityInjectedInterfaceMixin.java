package org.xiyu.reforged.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.extensions.ILivingEntityExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntity.class)
public abstract class LivingEntityInjectedInterfaceMixin implements ILivingEntityExtension {
}
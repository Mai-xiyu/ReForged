package org.xiyu.reforged.mixin;

import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.extensions.IEntityExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
public abstract class EntityInjectedInterfaceMixin implements IEntityExtension {
}
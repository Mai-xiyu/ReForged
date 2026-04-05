package org.xiyu.reforged.mixin;

import net.minecraft.world.level.block.state.StateHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = StateHolder.class, remap = false)
public abstract class StateHolderAccessorMixin {
    @Shadow @Final protected Object owner;

    public Object getOwner() {
        return this.owner;
    }
}

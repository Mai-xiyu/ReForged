package org.xiyu.reforged.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = MouseHandler.class, remap = false)
public abstract class MouseHandlerAccessorMixin {
    @Shadow private double xpos;
    @Shadow private double ypos;

    public void create$setXPos(double x) {
        this.xpos = x;
    }

    public void create$setYPos(double y) {
        this.ypos = y;
    }
}

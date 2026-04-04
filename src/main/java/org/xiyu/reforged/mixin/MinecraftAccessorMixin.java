package org.xiyu.reforged.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Minecraft.class)
public abstract class MinecraftAccessorMixin {
    @Shadow private int missTime;

    public void create$setMissTime(int time) {
        this.missTime = time;
    }
}

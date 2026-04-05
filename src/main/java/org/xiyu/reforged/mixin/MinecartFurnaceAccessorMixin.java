package org.xiyu.reforged.mixin;

import net.minecraft.world.entity.vehicle.MinecartFurnace;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = MinecartFurnace.class, remap = false)
public abstract class MinecartFurnaceAccessorMixin {
    @Shadow private int fuel;

    public int create$getFuel() {
        return this.fuel;
    }

    public void create$setFuel(int fuel) {
        this.fuel = fuel;
    }
}

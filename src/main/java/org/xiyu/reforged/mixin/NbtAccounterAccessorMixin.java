package org.xiyu.reforged.mixin;

import net.minecraft.nbt.NbtAccounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NbtAccounter.class)
public abstract class NbtAccounterAccessorMixin {
    @Shadow private long usage;

    public long create$getUsage() {
        return this.usage;
    }
}

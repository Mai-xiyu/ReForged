package org.xiyu.reforged.mixin;

import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = CropBlock.class, remap = false)
public abstract class CropBlockAccessorMixin {
    @Shadow
    protected abstract IntegerProperty getAgeProperty();

    public IntegerProperty create$callGetAgeProperty() {
        return this.getAgeProperty();
    }
}

package org.xiyu.reforged.mixin;

import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AgeableListModel.class)
public abstract class AgeableListModelAccessorMixin {
    @Shadow
    protected abstract Iterable<ModelPart> headParts();
    @Shadow
    protected abstract Iterable<ModelPart> bodyParts();

    public Iterable<ModelPart> create$callHeadParts() {
        return this.headParts();
    }

    public Iterable<ModelPart> create$callBodyParts() {
        return this.bodyParts();
    }
}

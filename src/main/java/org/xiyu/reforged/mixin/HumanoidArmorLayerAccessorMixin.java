package org.xiyu.reforged.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = HumanoidArmorLayer.class, remap = false)
public abstract class HumanoidArmorLayerAccessorMixin {
    @Shadow @Final private HumanoidModel innerModel;
    @Shadow @Final private HumanoidModel outerModel;

    @Shadow
    protected abstract void setPartVisibility(HumanoidModel model, EquipmentSlot slot);

    public HumanoidModel create$getInnerModel() {
        return this.innerModel;
    }

    public HumanoidModel create$getOuterModel() {
        return this.outerModel;
    }

    public void create$callSetPartVisibility(HumanoidModel model, EquipmentSlot slot) {
        this.setPartVisibility(model, slot);
    }
}

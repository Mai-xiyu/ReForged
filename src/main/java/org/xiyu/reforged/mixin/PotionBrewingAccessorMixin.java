package org.xiyu.reforged.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(PotionBrewing.class)
public abstract class PotionBrewingAccessorMixin {
    @Shadow @Final public List<PotionBrewing.Mix<Potion>> potionMixes;
    @Shadow @Final public List<PotionBrewing.Mix<Item>> containerMixes;

    @Shadow
    private boolean isContainer(ItemStack stack) {
        throw new AssertionError();
    }

    public List<PotionBrewing.Mix<Potion>> create$getPotionMixes() {
        return this.potionMixes;
    }

    public List<PotionBrewing.Mix<Item>> create$getContainerMixes() {
        return this.containerMixes;
    }

    public boolean create$isContainer(ItemStack stack) {
        return this.isContainer(stack);
    }
}

package org.xiyu.reforged.mixin;

import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Injects Create's {@code create$getPattern()} accessor method into ShapedRecipe.
 * The accessor interface cast is handled by BytecodeRewriter
 * (INVOKEINTERFACE → INVOKEVIRTUAL).
 */
@Mixin(ShapedRecipe.class)
public abstract class ShapedRecipeAccessorMixin {

    @Shadow @Final
    ShapedRecipePattern pattern;

    public ShapedRecipePattern create$getPattern() {
        return this.pattern;
    }
}

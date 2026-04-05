package org.xiyu.reforged.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Function;

@Mixin(value = Font.class, remap = false)
public abstract class FontAccessorMixin {
    @Shadow @Final private Function<ResourceLocation, FontSet> fonts;

    public Function<ResourceLocation, FontSet> create$getFonts() {
        return this.fonts;
    }
}

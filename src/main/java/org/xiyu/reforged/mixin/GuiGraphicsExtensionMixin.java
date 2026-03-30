package org.xiyu.reforged.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.extensions.IGuiGraphicsExtension;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Makes GuiGraphics implement NeoForge's IGuiGraphicsExtension interface,
 * enabling 9-slice rendering and inscribed blit for NeoForge mods.
 */
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsExtensionMixin implements IGuiGraphicsExtension {
}

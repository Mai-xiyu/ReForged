package org.xiyu.reforged.mixin;

import java.util.List;
import net.createmod.ponder.mixin.client.accessor.ScreenAccessor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Implements Ponder's {@link ScreenAccessor} on {@link Screen} so that
 * NeoForge mod code can cast Screen instances to ScreenAccessor.
 */
@Mixin(value = Screen.class, remap = false)
public abstract class ScreenAccessorImplMixin implements ScreenAccessor {
    @Shadow
    private List<Renderable> renderables;

    @Override
    public List<Renderable> catnip$getRenderables() {
        return this.renderables;
    }
}

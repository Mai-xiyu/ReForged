package net.createmod.ponder.mixin.client.accessor;

import java.util.List;
import net.minecraft.client.gui.components.Renderable;

/**
 * Accessor interface matching Ponder's {@code ScreenAccessor} mixin.
 * Exposes {@link net.minecraft.client.gui.screens.Screen#renderables}.
 *
 * <p>Implemented via {@code org.xiyu.reforged.mixin.ScreenAccessorImplMixin}
 * since NeoForge mod mixin configs are not registered with Forge's Mixin system.</p>
 */
public interface ScreenAccessor {
    List<Renderable> catnip$getRenderables();
}

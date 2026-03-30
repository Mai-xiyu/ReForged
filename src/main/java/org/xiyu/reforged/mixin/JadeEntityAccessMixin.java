package org.xiyu.reforged.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/**
 * Implements Jade's {@code snownee.jade.mixin.EntityAccess} interface on Entity.
 *
 * <p>Jade's EntityAccess is a {@code @Mixin} interface with
 * {@code @Invoker("getTypeName") Component callGetTypeName()}.
 * On NeoForge, Jade's own mixin applies this. On Forge + ReForged we must provide
 * it ourselves. The interface is added to Entity's class node via ASM in
 * {@link ReForgedMixinPlugin#postApply}.</p>
 */
@Mixin(Entity.class)
public abstract class JadeEntityAccessMixin {

    @Shadow(remap = false)
    protected abstract Component getTypeName();

    /**
     * Provides the {@code callGetTypeName()} method that Jade's EntityAccess
     * interface declares. This delegates to the protected {@code Entity.getTypeName()}.
     */
    @Unique
    public Component callGetTypeName() {
        return this.getTypeName();
    }
}

package org.xiyu.reforged.mixin;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Prevents NPE when an entity has no registered renderer.
 *
 * <p>NeoForge mods may register entity types without properly registering
 * corresponding client-side renderers via Forge's event system. This results
 * in {@code getRenderer()} returning null, causing a crash in
 * {@code shouldRender()}. This mixin returns false for such entities.</p>
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(
        method = "shouldRender",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private <E extends Entity> void reforged$skipNullRenderer(E entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
		if (((EntityRenderDispatcher) (Object) this).getRenderer(entity) == null) {
            cir.setReturnValue(false);
        }
    }
}

package org.xiyu.reforged.mixin;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * Prevents NPE when an entity has no registered renderer,
 * and injects Create's {@code create$getRenderers()} accessor method.
 * The accessor interface cast is handled by BytecodeRewriter (INVOKEINTERFACE → INVOKEVIRTUAL).
 */
@Mixin(value = EntityRenderDispatcher.class, remap = false)
public abstract class EntityRenderDispatcherMixin {

    @Shadow @Final
    private Map<EntityType<?>, EntityRenderer<?>> renderers;

    /**
     * Accessor method injected into EntityRenderDispatcher.
     * Create's code calls this via INVOKEVIRTUAL after BytecodeRewriter transforms
     * the original INVOKEINTERFACE on EntityRenderDispatcherAccessor.
     */
    public Map<EntityType<?>, EntityRenderer<?>> create$getRenderers() {
        return this.renderers;
    }

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

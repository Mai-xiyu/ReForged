package org.xiyu.reforged.mixin;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.xiyu.reforged.bridge.FlywheelRenderBridge;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replicates Flywheel's visualmanage.BlockEntityMixin — notifies Flywheel when
 * a block entity is removed, so its visual can be cleaned up.
 *
 * <p>Original injection: method = "setRemoved", at = TAIL.
 * Calls VisualizationManager.blockEntities().queueRemove(this) via bridge.</p>
 */
@Mixin(value = BlockEntity.class, remap = false)
public abstract class FlywheelBlockEntityRemoveMixin {

    @Shadow(remap = false)
    protected Level level;

    @Inject(method = "setRemoved", at = @At("TAIL"), remap = false)
    private void reforged$flywheelOnBlockEntityRemoved(CallbackInfo ci) {
        try {
            FlywheelRenderBridge.onBlockEntityRemoved(this.level, (BlockEntity) (Object) this);
        } catch (Throwable ignored) {
        }
    }
}

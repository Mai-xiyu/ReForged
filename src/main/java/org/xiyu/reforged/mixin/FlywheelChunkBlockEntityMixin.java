package org.xiyu.reforged.mixin;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.xiyu.reforged.bridge.FlywheelRenderBridge;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replicates Flywheel's visualmanage.LevelChunkMixin — notifies Flywheel when
 * block entities are added to a chunk (e.g., during chunk loading or block placement).
 *
 * <p>Original injection: method = "setBlockEntity", at INVOKE_ASSIGN on Map.put().
 * We use TAIL for simplicity since the block entity is already in the map at that point.</p>
 */
@Mixin(value = LevelChunk.class, remap = false)
public abstract class FlywheelChunkBlockEntityMixin {

    @Shadow(remap = false) @Final
    Level level;

    @Inject(method = "setBlockEntity", at = @At("TAIL"), remap = false)
    private void reforged$flywheelOnBlockEntityAdded(BlockEntity blockEntity, CallbackInfo ci) {
        try {
            FlywheelRenderBridge.onBlockEntityAdded(this.level, blockEntity);
        } catch (Throwable ignored) {
        }
    }
}

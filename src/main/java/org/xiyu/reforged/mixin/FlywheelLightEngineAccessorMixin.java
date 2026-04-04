package org.xiyu.reforged.mixin;

import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import net.minecraft.world.level.lighting.LightEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Injects Flywheel's LightEngineAccessor.flywheel$storage()
 * so Flywheel can access the underlying light storage for its lighting system.
 */
@Mixin(value = LightEngine.class, remap = false)
public abstract class FlywheelLightEngineAccessorMixin {

    @Shadow(remap = false) @Final
    protected LayerLightSectionStorage storage;

    public LayerLightSectionStorage flywheel$storage() {
        return this.storage;
    }
}

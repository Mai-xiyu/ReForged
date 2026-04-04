package org.xiyu.reforged.mixin;

import net.createmod.ponder.mixin.accessor.TimerAccessor;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DeltaTracker.Timer.class)
public abstract class TimerAccessorMixin implements TimerAccessor {

    @Shadow
    private float deltaTickResidual;

    @Override
    public float catnip$getDeltaTickResidual() {
        return this.deltaTickResidual;
    }

    // Create's TimerAccessor uses this name (no catnip$ prefix)
    public float getDeltaTickResidual() {
        return this.deltaTickResidual;
    }
}

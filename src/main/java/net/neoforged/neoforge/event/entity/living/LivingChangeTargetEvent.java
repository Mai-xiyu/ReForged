package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;

import javax.annotation.Nullable;

/**
 * Fired when a living entity changes its AI target.
 */
public class LivingChangeTargetEvent extends net.neoforged.bus.api.Event implements ICancellableEvent {
    private final LivingEntity entity;
    @Nullable
    private LivingEntity newTarget;
    private final ILivingTargetType targetType;

    public LivingChangeTargetEvent() {
        this.entity = null;
        this.newTarget = null;
        this.targetType = ILivingTargetType.MOB_TARGET;
    }

    public LivingChangeTargetEvent(LivingEntity entity, @Nullable LivingEntity newTarget, ILivingTargetType targetType) {
        this.entity = entity;
        this.newTarget = newTarget;
        this.targetType = targetType;
    }

    /** Wrapper from Forge event */
    public LivingChangeTargetEvent(net.minecraftforge.event.entity.living.LivingChangeTargetEvent forge) {
        this.entity = forge.getEntity();
        this.newTarget = forge.getNewTarget();
        this.targetType = ILivingTargetType.MOB_TARGET;
    }

    public LivingEntity getEntity() { return entity; }

    @Nullable
    public LivingEntity getNewTarget() { return newTarget; }

    public void setNewTarget(@Nullable LivingEntity newTarget) { this.newTarget = newTarget; }

    public ILivingTargetType getTargetType() { return targetType; }

    public enum ILivingTargetType {
        MOB_TARGET,
        BEHAVIOR_TARGET
    }
}

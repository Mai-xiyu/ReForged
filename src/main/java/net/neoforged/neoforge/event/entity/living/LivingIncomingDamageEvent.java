package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * Wrapper around Forge's {@link net.minecraftforge.event.entity.living.LivingAttackEvent}.
 * NeoForge renamed LivingAttackEvent to LivingIncomingDamageEvent.
 */
@Cancelable
public class LivingIncomingDamageEvent extends LivingEvent implements ICancellableEvent {
    private final net.minecraftforge.event.entity.living.LivingAttackEvent delegate;

    public LivingIncomingDamageEvent() {
        super();
        this.delegate = null;
    }

    public LivingIncomingDamageEvent(net.minecraftforge.event.entity.living.LivingAttackEvent delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public LivingEntity getEntity() { return delegate.getEntity(); }
    public float getAmount() { return delegate.getAmount(); }
    public DamageSource getSource() { return delegate.getSource(); }
    /** NeoForge has setAmount; Forge's LivingAttackEvent doesn't — no-op. */
    public void setAmount(float amount) { /* not supported by Forge LivingAttackEvent */ }

    @Override
    public void setCanceled(boolean canceled) {
        super.setCanceled(canceled);
        delegate.setCanceled(canceled);
    }
}

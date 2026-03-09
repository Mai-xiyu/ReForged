package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * Wrapper around Forge's {@link net.minecraftforge.event.entity.living.LivingAttackEvent}.
 * NeoForge renamed LivingAttackEvent to LivingIncomingDamageEvent.
 */
@Cancelable
public class LivingIncomingDamageEvent extends LivingEvent implements ICancellableEvent {
    private final net.minecraftforge.event.entity.living.LivingAttackEvent delegate;
    private final DamageContainer container;

    public LivingIncomingDamageEvent() {
        super();
        this.delegate = null;
        this.container = null;
    }

    public LivingIncomingDamageEvent(LivingEntity entity, DamageContainer container) {
		super(entity);
		this.delegate = null;
		this.container = container;
    }

    public LivingIncomingDamageEvent(net.minecraftforge.event.entity.living.LivingAttackEvent delegate) {
        super(delegate);
        this.delegate = delegate;
        this.container = new DamageContainer(delegate.getSource(), delegate.getAmount());
    }

    @Override
    public LivingEntity getEntity() { return delegate != null ? delegate.getEntity() : super.getEntity(); }
    public DamageContainer getContainer() { return container; }
    public float getAmount() { return container != null ? container.getNewDamage() : 0f; }
    public float getOriginalAmount() { return container != null ? container.getOriginalDamage() : 0f; }
    public DamageSource getSource() { return container != null ? container.getSource() : null; }
    public void setAmount(float amount) {
		if (container != null) {
			container.setNewDamage(amount);
		}
	}
    public void setInvulnerabilityTicks(int ticks) {
		if (container != null) {
			container.setPostAttackInvulnerabilityTicks(ticks);
		}
	}

    @Override
    public void setCanceled(boolean canceled) {
        super.setCanceled(canceled);
        if (delegate != null) {
			delegate.setCanceled(canceled);
		}
    }
}

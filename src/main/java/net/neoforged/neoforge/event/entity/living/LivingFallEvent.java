package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class LivingFallEvent extends LivingEvent implements ICancellableEvent {
	private final net.minecraftforge.event.entity.living.LivingFallEvent delegate;

	public LivingFallEvent() {
		super();
		this.delegate = null;
	}

	public LivingFallEvent(net.minecraftforge.event.entity.living.LivingFallEvent delegate) {
		super(delegate);
		this.delegate = delegate;
	}

	@Override
	public LivingEntity getEntity() {
		return delegate.getEntity();
	}

	public float getDistance() {
		return delegate.getDistance();
	}

	public void setDistance(float distance) {
		delegate.setDistance(distance);
	}

	public float getDamageMultiplier() {
		return delegate.getDamageMultiplier();
	}

	public void setDamageMultiplier(float damageMultiplier) {
		delegate.setDamageMultiplier(damageMultiplier);
	}

	@Override
	public void setCanceled(boolean canceled) {
		super.setCanceled(canceled);
		delegate.setCanceled(canceled);
	}
}

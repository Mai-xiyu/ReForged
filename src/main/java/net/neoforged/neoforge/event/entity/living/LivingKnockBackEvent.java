package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class LivingKnockBackEvent extends LivingEvent implements ICancellableEvent {
	private final net.minecraftforge.event.entity.living.LivingKnockBackEvent delegate;

	public LivingKnockBackEvent() {
		super();
		this.delegate = null;
	}

	public LivingKnockBackEvent(net.minecraftforge.event.entity.living.LivingKnockBackEvent delegate) {
		super(delegate);
		this.delegate = delegate;
	}

	@Override
	public LivingEntity getEntity() {
		return delegate.getEntity();
	}

	public float getStrength() {
		return delegate.getStrength();
	}

	public double getRatioX() {
		return delegate.getRatioX();
	}

	public double getRatioZ() {
		return delegate.getRatioZ();
	}

	public float getOriginalStrength() {
		return delegate.getOriginalStrength();
	}

	public double getOriginalRatioX() {
		return delegate.getOriginalRatioX();
	}

	public double getOriginalRatioZ() {
		return delegate.getOriginalRatioZ();
	}

	public void setStrength(float strength) {
		delegate.setStrength(strength);
	}

	public void setRatioX(double ratioX) {
		delegate.setRatioX(ratioX);
	}

	public void setRatioZ(double ratioZ) {
		delegate.setRatioZ(ratioZ);
	}

	@Override
	public void setCanceled(boolean canceled) {
		super.setCanceled(canceled);
		delegate.setCanceled(canceled);
	}
}

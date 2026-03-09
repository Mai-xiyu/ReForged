package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;

public class LivingBreatheEvent extends LivingEvent {
	private final net.minecraftforge.event.entity.living.LivingBreatheEvent delegate;

	public LivingBreatheEvent() {
		super();
		this.delegate = null;
	}

	public LivingBreatheEvent(net.minecraftforge.event.entity.living.LivingBreatheEvent delegate) {
		super(delegate);
		this.delegate = delegate;
	}

	@Override
	public LivingEntity getEntity() {
		return delegate.getEntity();
	}

	public boolean canBreathe() {
		return delegate.canBreathe();
	}

	public void setCanBreathe(boolean canBreathe) {
		delegate.setCanBreathe(canBreathe);
	}

	public boolean canRefillAir() {
		return delegate.canRefillAir();
	}

	public void setCanRefillAir(boolean canRefillAir) {
		delegate.setCanRefillAir(canRefillAir);
	}

	public int getConsumeAirAmount() {
		return delegate.getConsumeAirAmount();
	}

	public void setConsumeAirAmount(int consumeAirAmount) {
		delegate.setConsumeAirAmount(consumeAirAmount);
	}

	public int getRefillAirAmount() {
		return delegate.getRefillAirAmount();
	}

	public void setRefillAirAmount(int refillAirAmount) {
		delegate.setRefillAirAmount(refillAirAmount);
	}
}

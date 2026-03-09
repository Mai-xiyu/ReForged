package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/** Wrapper around Forge's {@link net.minecraftforge.event.entity.living.LivingHealEvent}. */
@Cancelable
public class LivingHealEvent extends LivingEvent implements ICancellableEvent {
    private final net.minecraftforge.event.entity.living.LivingHealEvent delegate;

    public LivingHealEvent() {
        super();
        this.delegate = null;
    }

    public LivingHealEvent(net.minecraftforge.event.entity.living.LivingHealEvent delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public LivingEntity getEntity() { return delegate.getEntity(); }
    public float getAmount() { return delegate.getAmount(); }
    public void setAmount(float amount) { delegate.setAmount(amount); }

    @Override
    public void setCanceled(boolean canceled) {
        super.setCanceled(canceled);
        delegate.setCanceled(canceled);
    }
}

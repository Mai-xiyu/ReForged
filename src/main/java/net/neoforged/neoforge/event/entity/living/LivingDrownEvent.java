package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * Stub: Fired when a living entity is drowning.
 */
@Cancelable
public class LivingDrownEvent extends LivingEvent implements ICancellableEvent {
    private final net.minecraftforge.event.entity.living.LivingDrownEvent delegate;

    public LivingDrownEvent() {
        super();
        this.delegate = null;
    }

    public LivingDrownEvent(net.minecraftforge.event.entity.living.LivingDrownEvent delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public LivingEntity getEntity() {
        return delegate.getEntity();
    }

    public boolean isDrowning() {
        return delegate.isDrowning();
    }

    public void setDrowning(boolean drowning) {
        delegate.setDrowning(drowning);
    }

    public float getDamageAmount() {
        return delegate.getDamageAmount();
    }

    public void setDamageAmount(float damageAmount) {
        delegate.setDamageAmount(damageAmount);
    }

    public int getBubbleCount() {
        return delegate.getBubbleCount();
    }

    public void setBubbleCount(int bubbleCount) {
        delegate.setBubbleCount(bubbleCount);
    }

    @Override
    public void setCanceled(boolean canceled) {
        super.setCanceled(canceled);
        delegate.setCanceled(canceled);
    }
}

package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/** Wrapper around Forge's {@link net.minecraftforge.event.entity.living.LivingDeathEvent}. */
@Cancelable
public class LivingDeathEvent extends LivingEvent implements ICancellableEvent {
    private final net.minecraftforge.event.entity.living.LivingDeathEvent delegate;

    public LivingDeathEvent() {
        super();
        this.delegate = null;
    }

    public LivingDeathEvent(net.minecraftforge.event.entity.living.LivingDeathEvent delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public LivingEntity getEntity() { return delegate.getEntity(); }
    public DamageSource getSource() { return delegate.getSource(); }

    @Override
    public void setCanceled(boolean canceled) {
        super.setCanceled(canceled);
        delegate.setCanceled(canceled);
    }
}

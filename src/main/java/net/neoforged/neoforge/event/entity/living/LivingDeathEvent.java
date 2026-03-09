package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/** Wrapper around Forge's {@link net.minecraftforge.event.entity.living.LivingDeathEvent}. */
@Cancelable
public class LivingDeathEvent extends LivingEvent implements ICancellableEvent {
    private final net.minecraftforge.event.entity.living.LivingDeathEvent delegate;
    private final DamageSource source;

    public LivingDeathEvent() {
        super();
        this.delegate = null;
        this.source = null;
    }

    public LivingDeathEvent(LivingEntity entity, DamageSource source) {
		super(entity);
		this.delegate = null;
		this.source = source;
    }

    public LivingDeathEvent(net.minecraftforge.event.entity.living.LivingDeathEvent delegate) {
        super(delegate);
        this.delegate = delegate;
        this.source = delegate.getSource();
    }

    @Override
    public LivingEntity getEntity() { return delegate != null ? delegate.getEntity() : super.getEntity(); }
    public DamageSource getSource() { return source; }

    @Override
    public void setCanceled(boolean canceled) {
        super.setCanceled(canceled);
        if (delegate != null) {
			delegate.setCanceled(canceled);
		}
    }
}

package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/** Wrapper around Forge's {@link net.minecraftforge.event.entity.living.LivingExperienceDropEvent}. */
@Cancelable
public class LivingExperienceDropEvent extends LivingEvent implements ICancellableEvent {
    private final net.minecraftforge.event.entity.living.LivingExperienceDropEvent delegate;

    public LivingExperienceDropEvent() {
        super();
        this.delegate = null;
    }

    public LivingExperienceDropEvent(net.minecraftforge.event.entity.living.LivingExperienceDropEvent delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public LivingEntity getEntity() { return delegate.getEntity(); }
    public int getDroppedExperience() { return delegate.getDroppedExperience(); }
    public void setDroppedExperience(int xp) { delegate.setDroppedExperience(xp); }
    public Player getAttackingPlayer() { return delegate.getAttackingPlayer(); }
    public int getOriginalExperience() { return delegate.getOriginalExperience(); }

    @Override
    public void setCanceled(boolean canceled) {
        super.setCanceled(canceled);
        delegate.setCanceled(canceled);
    }
}

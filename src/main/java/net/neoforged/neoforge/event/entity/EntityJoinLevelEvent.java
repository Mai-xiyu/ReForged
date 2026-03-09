package net.neoforged.neoforge.event.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.ICancellableEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * Wrapper around Forge's {@link net.minecraftforge.event.entity.EntityJoinLevelEvent}.
 */
@Cancelable
public class EntityJoinLevelEvent extends EntityEvent implements ICancellableEvent {
    private final net.minecraftforge.event.entity.EntityJoinLevelEvent delegate;

    public EntityJoinLevelEvent() {
        super();
        this.delegate = null;
    }

    public EntityJoinLevelEvent(net.minecraftforge.event.entity.EntityJoinLevelEvent delegate) {
        super(delegate);
        this.delegate = delegate;
    }

    @Override
    public Entity getEntity() { return delegate.getEntity(); }
    public Level getLevel() { return delegate.getLevel(); }
    public boolean loadedFromDisk() { return delegate.loadedFromDisk(); }

    @Override
    public void setCanceled(boolean canceled) {
        super.setCanceled(canceled);
        delegate.setCanceled(canceled);
    }
}

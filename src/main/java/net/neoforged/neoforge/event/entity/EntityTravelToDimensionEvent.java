package net.neoforged.neoforge.event.entity;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.ICancellableEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class EntityTravelToDimensionEvent extends EntityEvent implements ICancellableEvent {
    private final net.minecraftforge.event.entity.EntityTravelToDimensionEvent delegate;
    private final ResourceKey<Level> dimension;

    public EntityTravelToDimensionEvent() {
        super();
        this.delegate = null;
        this.dimension = null;
    }

    public EntityTravelToDimensionEvent(Entity entity, ResourceKey<Level> dimension) {
        super(entity);
        this.delegate = null;
        this.dimension = dimension;
    }

    public EntityTravelToDimensionEvent(net.minecraftforge.event.entity.EntityTravelToDimensionEvent delegate) {
        super(delegate);
        this.delegate = delegate;
        this.dimension = delegate.getDimension();
    }

    @Override
    public Entity getEntity() { return delegate != null ? delegate.getEntity() : super.getEntity(); }
    public ResourceKey<Level> getDimension() { return dimension; }

    @Override
    public void setCanceled(boolean canceled) {
        super.setCanceled(canceled);
        if (delegate != null) {
            delegate.setCanceled(canceled);
        }
    }
}

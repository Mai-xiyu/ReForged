package net.neoforged.neoforge.event.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.neoforged.bus.api.ICancellableEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class EntityStruckByLightningEvent extends EntityEvent implements ICancellableEvent {
    private final net.minecraftforge.event.entity.EntityStruckByLightningEvent delegate;
    private final Entity entity;
    private final LightningBolt lightning;

    public EntityStruckByLightningEvent() {
        super();
        this.delegate = null;
        this.entity = null;
        this.lightning = null;
    }

    public EntityStruckByLightningEvent(Entity entity, LightningBolt lightning) {
        super(entity);
        this.delegate = null;
        this.entity = entity;
        this.lightning = lightning;
    }

    public EntityStruckByLightningEvent(net.minecraftforge.event.entity.EntityStruckByLightningEvent delegate) {
        super(delegate);
        this.delegate = delegate;
        this.entity = delegate.getEntity();
        this.lightning = delegate.getLightning();
    }

    @Override
    public Entity getEntity() { return entity; }
    public LightningBolt getLightning() { return lightning; }

    @Override
    public void setCanceled(boolean canceled) {
        super.setCanceled(canceled);
        if (delegate != null) {
            delegate.setCanceled(canceled);
        }
    }
}

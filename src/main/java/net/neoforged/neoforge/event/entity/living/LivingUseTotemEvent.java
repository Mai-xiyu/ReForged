package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * Wrapper around Forge's {@link net.minecraftforge.event.entity.living.LivingUseTotemEvent}.
 */
@Cancelable
public class LivingUseTotemEvent extends LivingEvent implements ICancellableEvent {
    private final net.minecraftforge.event.entity.living.LivingUseTotemEvent delegate;
    private final DamageSource source;
    private final ItemStack totem;
    private final InteractionHand hand;

    public LivingUseTotemEvent() {
        super();
        this.delegate = null;
        this.source = null;
        this.totem = ItemStack.EMPTY;
        this.hand = null;
    }

    public LivingUseTotemEvent(LivingEntity entity, DamageSource source, ItemStack totem, InteractionHand hand) {
        super(entity);
        this.delegate = null;
        this.source = source;
        this.totem = totem;
        this.hand = hand;
    }

    public LivingUseTotemEvent(net.minecraftforge.event.entity.living.LivingUseTotemEvent delegate) {
        super(delegate);
        this.delegate = delegate;
        this.source = delegate.getSource();
        this.totem = delegate.getTotem();
        this.hand = delegate.getHandHolding();
    }

    public DamageSource getSource() {
        return source;
    }

    public ItemStack getTotem() {
        return totem;
    }

    public InteractionHand getHandHolding() {
        return hand;
    }

    @Override
    public void setCanceled(boolean canceled) {
        super.setCanceled(canceled);
        if (delegate != null) {
            delegate.setCanceled(canceled);
        }
    }
}

package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Stub: Fired when a living entity uses an item (start, tick, stop, finish).
 */
public class LivingEntityUseItemEvent extends LivingEvent {
    private final ItemStack item;
    private int duration;

    private LivingEntityUseItemEvent(LivingEntity entity, ItemStack item, int duration) {
        super(entity);
        this.item = item;
        this.duration = duration;
    }

    public ItemStack getItem() { return item; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public static class Start extends LivingEntityUseItemEvent {
        public Start(LivingEntity entity, ItemStack item, int duration) {
            super(entity, item, duration);
        }

        /** Forge wrapper constructor for automatic event bridging */
        public Start(net.minecraftforge.event.entity.living.LivingEntityUseItemEvent.Start delegate) {
            this(delegate.getEntity(), delegate.getItem(), delegate.getDuration());
        }
    }

    public static class Tick extends LivingEntityUseItemEvent {
        public Tick(LivingEntity entity, ItemStack item, int duration) {
            super(entity, item, duration);
        }

        /** Forge wrapper constructor for automatic event bridging */
        public Tick(net.minecraftforge.event.entity.living.LivingEntityUseItemEvent.Tick delegate) {
            this(delegate.getEntity(), delegate.getItem(), delegate.getDuration());
        }
    }

    public static class Stop extends LivingEntityUseItemEvent {
        public Stop(LivingEntity entity, ItemStack item, int duration) {
            super(entity, item, duration);
        }

        /** Forge wrapper constructor for automatic event bridging */
        public Stop(net.minecraftforge.event.entity.living.LivingEntityUseItemEvent.Stop delegate) {
            this(delegate.getEntity(), delegate.getItem(), delegate.getDuration());
        }
    }

    public static class Finish extends LivingEntityUseItemEvent {
        private ItemStack result;

        public Finish(LivingEntity entity, ItemStack item, int duration, ItemStack result) {
            super(entity, item, duration);
            this.result = result;
        }

        /** Forge wrapper constructor for automatic event bridging */
        public Finish(net.minecraftforge.event.entity.living.LivingEntityUseItemEvent.Finish delegate) {
            this(delegate.getEntity(), delegate.getItem(), delegate.getDuration(), delegate.getResultStack());
        }

        public ItemStack getResultStack() { return result; }
        public void setResultStack(ItemStack result) { this.result = result; }
    }
}

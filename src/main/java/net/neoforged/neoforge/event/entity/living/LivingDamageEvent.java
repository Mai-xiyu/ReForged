package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

/** Proxy: NeoForge LivingDamageEvent */
public class LivingDamageEvent extends Event {
    private final LivingEntity entity;
    private float amount;

    public LivingDamageEvent(LivingEntity entity, float amount) {
        this.entity = entity;
        this.amount = amount;
    }

    public LivingEntity getEntity() { return entity; }
    public float getAmount() { return amount; }
    public void setAmount(float amount) { this.amount = amount; }

    public static class Pre extends LivingDamageEvent {
        public Pre(LivingEntity entity, float amount) { super(entity, amount); }
    }
}

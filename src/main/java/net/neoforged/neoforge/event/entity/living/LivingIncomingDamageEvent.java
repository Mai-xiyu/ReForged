package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

/** Proxy: NeoForge LivingIncomingDamageEvent */
public class LivingIncomingDamageEvent extends Event {
    private final LivingEntity entity;
    private float amount;
    public LivingIncomingDamageEvent(LivingEntity entity, float amount) { this.entity = entity; this.amount = amount; }
    public LivingEntity getEntity() { return entity; }
    public float getAmount() { return amount; }
    public void setAmount(float a) { this.amount = a; }
}

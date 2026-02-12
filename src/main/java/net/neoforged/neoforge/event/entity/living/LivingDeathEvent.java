package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Event;

/** Proxy: NeoForge LivingDeathEvent */
public class LivingDeathEvent extends Event {
    private final LivingEntity entity;
    public LivingDeathEvent(LivingEntity entity) { this.entity = entity; }
    public LivingEntity getEntity() { return entity; }
}

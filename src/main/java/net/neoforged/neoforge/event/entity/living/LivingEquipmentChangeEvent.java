package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

/**
 * Fired when a living entity's equipment changes.
 */
public class LivingEquipmentChangeEvent extends Event {
    private final LivingEntity entity;
    private final EquipmentSlot slot;
    private final ItemStack from;
    private final ItemStack to;

    public LivingEquipmentChangeEvent(LivingEntity entity, EquipmentSlot slot, ItemStack from, ItemStack to) {
        this.entity = entity;
        this.slot = slot;
        this.from = from;
        this.to = to;
    }

    /** Forge wrapper constructor for automatic event bridging */
    public LivingEquipmentChangeEvent(net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent delegate) {
        this(delegate.getEntity(), delegate.getSlot(), delegate.getFrom(), delegate.getTo());
    }

    public LivingEntity getEntity() { return entity; }
    public EquipmentSlot getSlot() { return slot; }
    public ItemStack getFrom() { return from; }
    public ItemStack getTo() { return to; }
}

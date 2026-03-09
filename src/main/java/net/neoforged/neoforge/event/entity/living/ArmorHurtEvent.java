package net.neoforged.neoforge.event.entity.living;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;

public class ArmorHurtEvent extends LivingEvent implements ICancellableEvent {
    public static class ArmorEntry {
        public ItemStack armorItemStack;
        public final float originalDamage;
        public float newDamage;

        public ArmorEntry(ItemStack armorStack, float damageIn) {
            this.armorItemStack = armorStack;
            this.originalDamage = damageIn;
            this.newDamage = damageIn;
        }
    }

    private final DamageSource source;
    private final EnumMap<EquipmentSlot, ArmorEntry> armorEntries;

    public ArmorHurtEvent(EnumMap<EquipmentSlot, ArmorEntry> armorMap, LivingEntity entity, DamageSource source) {
        super(entity);
        this.armorEntries = armorMap;
        this.source = source;
    }

    public ItemStack getArmorItemStack(EquipmentSlot slot) {
        return armorEntries.containsKey(slot) ? armorEntries.get(slot).armorItemStack : ItemStack.EMPTY;
    }

    public float getOriginalDamage(EquipmentSlot slot) {
        return armorEntries.containsKey(slot) ? armorEntries.get(slot).originalDamage : 0f;
    }

    public float getNewDamage(EquipmentSlot slot) {
        return armorEntries.containsKey(slot) ? armorEntries.get(slot).newDamage : 0f;
    }

    public void setNewDamage(EquipmentSlot slot, float damage) {
        if (armorEntries.containsKey(slot)) {
            armorEntries.get(slot).newDamage = Math.max(0f, damage);
        }
    }

    public Map<EquipmentSlot, ArmorEntry> getArmorMap() {
        return armorEntries;
    }

    public DamageSource getDamageSource() {
        return source;
    }
}

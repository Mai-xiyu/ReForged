package net.neoforged.neoforge.items.wrapper;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class PlayerArmorInvWrapper extends RangedWrapper {
    private final Inventory inventoryPlayer;

    public PlayerArmorInvWrapper(Inventory inv) {
        super(new InvWrapper(inv), inv.items.size(), inv.items.size() + inv.armor.size());
        this.inventoryPlayer = inv;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        EquipmentSlot equipmentSlot = null;
        for (EquipmentSlot slotType : EquipmentSlot.values()) {
            if (slotType.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && slotType.getIndex() == slot) {
                equipmentSlot = slotType;
                break;
            }
        }

        if (equipmentSlot != null && slot < 4 && !stack.isEmpty() && stack.canEquip(equipmentSlot, inventoryPlayer.player)) {
            return super.insertItem(slot, stack, simulate);
        }
        return stack;
    }
}
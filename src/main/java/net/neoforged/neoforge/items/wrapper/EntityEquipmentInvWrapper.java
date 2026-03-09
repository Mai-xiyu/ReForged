package net.neoforged.neoforge.items.wrapper;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.List;

public abstract class EntityEquipmentInvWrapper implements IItemHandlerModifiable {
    protected final LivingEntity entity;
    protected final List<EquipmentSlot> slots;

    public EntityEquipmentInvWrapper(LivingEntity entity, EquipmentSlot.Type slotType) {
        this.entity = entity;
        this.slots = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == slotType) {
                this.slots.add(slot);
            }
        }
    }

    @Override
    public int getSlots() {
        return slots.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return entity.getItemBySlot(validateSlotIndex(slot));
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        EquipmentSlot equipmentSlot = validateSlotIndex(slot);
        ItemStack existing = entity.getItemBySlot(equipmentSlot);
        int limit = getStackLimit(slot, stack);

        if (!existing.isEmpty()) {
            if (!ItemStack.isSameItemSameComponents(stack, existing)) {
                return stack;
            }
            limit -= existing.getCount();
        }

        if (limit <= 0) {
            return stack;
        }

        boolean reachedLimit = stack.getCount() > limit;
        if (!simulate) {
            if (existing.isEmpty()) {
                entity.setItemSlot(equipmentSlot, reachedLimit ? stack.copyWithCount(limit) : stack);
            } else {
                existing.grow(reachedLimit ? limit : stack.getCount());
            }
        }

        return reachedLimit ? stack.copyWithCount(stack.getCount() - limit) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) {
            return ItemStack.EMPTY;
        }

        EquipmentSlot equipmentSlot = validateSlotIndex(slot);
        ItemStack existing = entity.getItemBySlot(equipmentSlot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int toExtract = Math.min(amount, existing.getMaxStackSize());
        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                entity.setItemSlot(equipmentSlot, ItemStack.EMPTY);
            }
            return existing;
        }

        if (!simulate) {
            entity.setItemSlot(equipmentSlot, existing.copyWithCount(existing.getCount() - toExtract));
        }
        return existing.copyWithCount(toExtract);
    }

    @Override
    public int getSlotLimit(int slot) {
        EquipmentSlot equipmentSlot = validateSlotIndex(slot);
        return equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR ? 1 : Item.ABSOLUTE_MAX_STACK_SIZE;
    }

    protected int getStackLimit(int slot, ItemStack stack) {
        return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        EquipmentSlot equipmentSlot = validateSlotIndex(slot);
        if (!ItemStack.matches(entity.getItemBySlot(equipmentSlot), stack)) {
            entity.setItemSlot(equipmentSlot, stack);
        }
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }

    protected EquipmentSlot validateSlotIndex(int slot) {
        if (slot < 0 || slot >= slots.size()) {
            throw new IllegalArgumentException("Slot " + slot + " not in valid range - [0," + slots.size() + ")");
        }
        return slots.get(slot);
    }
}
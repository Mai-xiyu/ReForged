package net.neoforged.neoforge.client.event;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Fired to allow mods to skip certain attribute tooltips.
 */
public class GatherSkippedAttributeTooltipsEvent extends net.neoforged.bus.api.Event {
    private final ItemStack stack;
    @Nullable
    private Set<ResourceLocation> skippedIds;
    @Nullable
    private Set<EquipmentSlotGroup> skippedGroups;
    private boolean skipAll;

    public GatherSkippedAttributeTooltipsEvent(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getStack() { return stack; }

    public void skipId(ResourceLocation id) {
        if (skippedIds == null) skippedIds = new HashSet<>();
        skippedIds.add(id);
    }

    public void skipGroup(EquipmentSlotGroup group) {
        if (skippedGroups == null) skippedGroups = new HashSet<>();
        skippedGroups.add(group);
    }

    public boolean isSkipped(ResourceLocation id) {
        return skipAll || (skippedIds != null && skippedIds.contains(id));
    }

    public boolean isSkipped(EquipmentSlotGroup group) {
        return skipAll || (skippedGroups != null && skippedGroups.contains(group));
    }

    public boolean isSkippingAll() { return skipAll; }
    public void setSkipAll(boolean skipAll) { this.skipAll = skipAll; }
}

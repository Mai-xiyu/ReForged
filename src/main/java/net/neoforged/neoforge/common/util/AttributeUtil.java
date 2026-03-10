package net.neoforged.neoforge.common.util;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceLinkedOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.AddAttributeTooltipsEvent;
import net.neoforged.neoforge.client.event.GatherSkippedAttributeTooltipsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.extensions.IAttributeExtension;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility code to support {@link IAttributeExtension}.
 */
public class AttributeUtil {
    /**
     * ID of the base modifier for Attack Damage
     */
    public static final ResourceLocation BASE_ATTACK_DAMAGE_ID = Item.BASE_ATTACK_DAMAGE_ID;

    /**
     * ID of the base modifier for Attack Speed
     */
    public static final ResourceLocation BASE_ATTACK_SPEED_ID = Item.BASE_ATTACK_SPEED_ID;

    /**
     * ID of the base modifier for Attack Range
     */
    public static final ResourceLocation BASE_ENTITY_REACH_ID = ResourceLocation.withDefaultNamespace("base_entity_reach");

    /**
     * ID used for attribute modifiers used to hold merged values when {@link NeoForgeMod#enableMergedAttributeTooltips()} is active.
     */
    public static final ResourceLocation FAKE_MERGED_ID = ResourceLocation.fromNamespaceAndPath("neoforge", "fake_merged_modifier");

    /**
     * Comparator for {@link AttributeModifier}. First compares by operation, then amount, then the ID.
     */
    public static final Comparator<AttributeModifier> ATTRIBUTE_MODIFIER_COMPARATOR = Comparator.comparing(AttributeModifier::operation)
            .thenComparingDouble(a -> -Math.abs(a.amount()))
            .thenComparing(AttributeModifier::id);

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Checks if attribute modifier tooltips should show, and if so, adds tooltips for all attribute modifiers on an item stack.
     */
    public static void addAttributeTooltips(ItemStack stack, Consumer<Component> tooltip, AttributeTooltipContext ctx) {
        ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        if (modifiers.showInTooltip()) {
            applyModifierTooltips(stack, tooltip, ctx);
        }
        NeoForge.EVENT_BUS.post(new AddAttributeTooltipsEvent(stack, tooltip, ctx));
    }

    /**
     * Applies the attribute modifier tooltips for all attribute modifiers present on the item stack.
     */
    public static void applyModifierTooltips(ItemStack stack, Consumer<Component> tooltip, AttributeTooltipContext ctx) {
        GatherSkippedAttributeTooltipsEvent event = new GatherSkippedAttributeTooltipsEvent(stack);
        NeoForge.EVENT_BUS.post(event);
        if (event.isSkippingAll()) {
            return;
        }

        for (EquipmentSlotGroup group : EquipmentSlotGroup.values()) {
            if (event.isSkipped(group)) {
                continue;
            }

            Multimap<Holder<Attribute>, AttributeModifier> modifiers = getSortedModifiers(stack, group);
            modifiers.values().removeIf(m -> event.isSkipped(m.id()));

            if (modifiers.isEmpty()) {
                continue;
            }

            tooltip.accept(Component.empty());
            tooltip.accept(Component.translatable("item.modifiers." + group.getSerializedName()).withStyle(ChatFormatting.GRAY));

            applyTextFor(stack, tooltip, modifiers, ctx);
        }
    }

    /**
     * Applies the text for the provided attribute modifiers to the tooltip.
     * Attempts to merge multiple modifiers for a single attribute if merged tooltips are enabled.
     */
    public static void applyTextFor(ItemStack stack, Consumer<Component> tooltip, Multimap<Holder<Attribute>, AttributeModifier> modifierMap, AttributeTooltipContext ctx) {
        if (modifierMap.isEmpty()) {
            return;
        }

        // Collect all base modifiers
        Map<Holder<Attribute>, BaseModifier> baseModifs = new Reference2ReferenceLinkedOpenHashMap<>();

        var it = modifierMap.entries().iterator();
        while (it.hasNext()) {
            Entry<Holder<Attribute>, AttributeModifier> entry = it.next();
            Holder<Attribute> attr = entry.getKey();
            AttributeModifier modif = entry.getValue();
            if (modif.id().equals(((IAttributeExtension) attr.value()).getBaseId())) {
                baseModifs.put(attr, new BaseModifier(modif, new ArrayList<>()));
                it.remove();
            }
        }

        // Collect children of all base modifiers
        for (Map.Entry<Holder<Attribute>, AttributeModifier> entry : modifierMap.entries()) {
            BaseModifier base = baseModifs.get(entry.getKey());
            if (base != null) {
                base.children.add(entry.getValue());
            }
        }

        // Add tooltip lines for base modifiers
        for (Map.Entry<Holder<Attribute>, BaseModifier> entry : baseModifs.entrySet()) {
            Holder<Attribute> attr = entry.getKey();
            BaseModifier baseModif = entry.getValue();
            double entityBase = ctx.player() == null ? 0 : ctx.player().getAttributeBaseValue(attr);
            double base = baseModif.base.amount() + entityBase;
            final double rawBase = base;
            double amt = base;

            if (NeoForgeMod.shouldMergeAttributeTooltips()) {
                for (AttributeModifier modif : baseModif.children) {
                    switch (modif.operation()) {
                        case ADD_VALUE:
                            base = amt = amt + modif.amount();
                            break;
                        case ADD_MULTIPLIED_BASE:
                            amt += modif.amount() * base;
                            break;
                        case ADD_MULTIPLIED_TOTAL:
                            amt *= 1 + modif.amount();
                            break;
                    }
                }
            }

            boolean isMerged = NeoForgeMod.shouldMergeAttributeTooltips() && !baseModif.children.isEmpty();
            IAttributeExtension ext = (IAttributeExtension) attr.value();
            MutableComponent text = ext.toBaseComponent(amt, entityBase, isMerged, ctx.flag());
            tooltip.accept(Component.literal(" ").append(text).withStyle(isMerged ? ChatFormatting.GOLD : ChatFormatting.DARK_GREEN));
            if (isShiftDown() && isMerged) {
                text = ext.toBaseComponent(rawBase, entityBase, false, ctx.flag());
                tooltip.accept(listHeader().append(text.withStyle(ChatFormatting.DARK_GREEN)));
                for (AttributeModifier modifier : baseModif.children) {
                    tooltip.accept(listHeader().append(ext.toComponent(modifier, ctx.flag())));
                }
            }
        }

        for (Holder<Attribute> attr : modifierMap.keySet()) {
            if (NeoForgeMod.shouldMergeAttributeTooltips() && baseModifs.containsKey(attr)) {
                continue;
            }

            IAttributeExtension ext = (IAttributeExtension) attr.value();
            Collection<AttributeModifier> modifs = modifierMap.get(attr);

            if (NeoForgeMod.shouldMergeAttributeTooltips() && modifs.size() > 1) {
                Map<Operation, MergedModifierData> mergeData = new EnumMap<>(Operation.class);

                for (AttributeModifier modifier : modifs) {
                    if (modifier.amount() == 0) continue;
                    MergedModifierData data = mergeData.computeIfAbsent(modifier.operation(), op -> new MergedModifierData());
                    if (data.sum != 0) {
                        data.isMerged = true;
                    }
                    data.sum += modifier.amount();
                    data.children.add(modifier);
                }

                for (Operation op : Operation.values()) {
                    MergedModifierData data = mergeData.get(op);
                    if (data == null || data.sum == 0) continue;

                    if (data.isMerged) {
                        TextColor color = ext.getMergedStyle(data.sum > 0);
                        var fakeModif = new AttributeModifier(FAKE_MERGED_ID, data.sum, op);
                        MutableComponent comp = ext.toComponent(fakeModif, ctx.flag());
                        tooltip.accept(comp.withStyle(comp.getStyle().withColor(color)));
                        if (isShiftDown()) {
                            data.children.forEach(modif -> tooltip.accept(listHeader().append(ext.toComponent(modif, ctx.flag()))));
                        }
                    } else {
                        var fakeModif = new AttributeModifier(FAKE_MERGED_ID, data.sum, op);
                        tooltip.accept(ext.toComponent(fakeModif, ctx.flag()));
                    }
                }
            } else {
                for (AttributeModifier m : modifs) {
                    if (m.amount() != 0) {
                        tooltip.accept(ext.toComponent(m, ctx.flag()));
                    }
                }
            }
        }
    }

    /**
     * Adds tooltip lines for the attribute modifiers contained in a potion.
     */
    public static void addPotionTooltip(List<Pair<Holder<Attribute>, AttributeModifier>> list, Consumer<Component> tooltips) {
        for (Pair<Holder<Attribute>, AttributeModifier> pair : list) {
            tooltips.accept(((IAttributeExtension) pair.getFirst().value()).toComponent(pair.getSecond(), getTooltipFlag()));
        }
    }

    /**
     * Creates a sorted {@link TreeMultimap} used to ensure a stable iteration order of item attribute modifiers.
     */
    public static Multimap<Holder<Attribute>, AttributeModifier> sortedMap() {
        return TreeMultimap.create(
                Comparator.comparing(h -> h.unwrapKey().map(k -> k.location().toString()).orElse("")),
                ATTRIBUTE_MODIFIER_COMPARATOR);
    }

    /**
     * Returns a sorted, mutable {@link Multimap} containing all attribute modifiers on an item stack for the given group.
     */
    public static Multimap<Holder<Attribute>, AttributeModifier> getSortedModifiers(ItemStack stack, EquipmentSlotGroup slot) {
        Multimap<Holder<Attribute>, AttributeModifier> map = LinkedListMultimap.create();
        stack.forEachModifier(slot, (attr, modif) -> {
            if (attr != null && modif != null) {
                map.put(attr, modif);
            } else {
                LOGGER.debug("Detected broken attribute modifier entry on item {}.  Attr={}, Modif={}", stack, attr, modif);
            }
        });
        return map;
    }

    private static MutableComponent listHeader() {
        return Component.literal(" \u2507 ").withStyle(ChatFormatting.GRAY);
    }

    private static TooltipFlag getTooltipFlag() {
        if (FMLEnvironment.dist.isClient()) {
            return ClientAccess.getTooltipFlag();
        }
        return TooltipFlag.NORMAL;
    }

    private static record BaseModifier(AttributeModifier base, List<AttributeModifier> children) {}

    private static class MergedModifierData {
        double sum = 0;
        boolean isMerged = false;
        private List<AttributeModifier> children = new LinkedList<>();
    }

    private static class ClientAccess {
        static TooltipFlag getTooltipFlag() {
            return Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
        }

        static boolean hasShiftDown() {
            return net.minecraft.client.gui.screens.Screen.hasShiftDown();
        }
    }

    /**
     * Checks if SHIFT is held down (client only). Returns false on server.
     */
    private static boolean isShiftDown() {
        if (FMLEnvironment.dist.isClient()) {
            return ClientAccess.hasShiftDown();
        }
        return false;
    }
}

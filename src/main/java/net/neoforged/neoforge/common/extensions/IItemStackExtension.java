package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.AdventureModePredicate;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.extensions.IForgeItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.event.EventHooks;

import javax.annotation.Nullable;

public interface IItemStackExtension extends IForgeItemStack {

    default ItemStack self() {
        return (ItemStack) (Object) this;
    }

    default ItemStack getCraftingRemainingItem() {
        return self().getItem().getCraftingRemainingItem(self());
    }

    default boolean hasCraftingRemainingItem() {
        return self().getItem().hasCraftingRemainingItem(self());
    }

    default int getBurnTime(@Nullable RecipeType<?> recipeType) {
        if (self().isEmpty()) {
            return 0;
        }

        int burnTime = self().getItem().getBurnTime(self(), recipeType);
        // Forge's IForgeItem.getBurnTime returns -1 to mean "use vanilla defaults".
        // NeoForge's convention is 0 = not a fuel, positive = burn time.
        // We must NOT throw on -1 — instead, pass it through so Forge's caller
        // (ForgeHooks.getBurnTime) does the vanilla lookup correctly.
        if (burnTime < -1) {
            throw new IllegalStateException("Negative burn time for stack " + self());
        }
        if (burnTime == -1) {
            // Return -1 as-is — Forge's ForgeHooks.getBurnTime will map it to the vanilla value
            return -1;
        }
        return EventHooks.getItemBurnTime(self(), burnTime, recipeType);
    }

    default InteractionResult onItemUseFirst(UseOnContext context) {
        Player player = context.getPlayer();
        BlockPos blockPos = context.getClickedPos();
        BlockInWorld blockInWorld = new BlockInWorld(context.getLevel(), blockPos, false);
        AdventureModePredicate canPlaceOn = self().get(DataComponents.CAN_PLACE_ON);
        if (player != null && !player.getAbilities().mayBuild && (canPlaceOn == null || !canPlaceOn.test(blockInWorld))) {
            return InteractionResult.PASS;
        }

        Item item = self().getItem();
        InteractionResult result = item.onItemUseFirst(self(), context);
        if (player != null && result == InteractionResult.SUCCESS) {
            player.awardStat(Stats.ITEM_USED.get(item));
        }
        return result;
    }

    default boolean canPerformAction(ItemAbility ability) {
        return self().getItem().canPerformAction(self(), ToolAction.get(ability.name()));
    }

    default boolean shouldCauseBlockBreakReset(ItemStack newStack) {
        Item item = self().getItem();
        if (item instanceof IItemExtension ext) {
            return ext.shouldCauseBlockBreakReset(self(), newStack);
        }
        // Fallback: inline default logic from IItemExtension
        if (!newStack.is(item)) return true;
        if (!newStack.isDamageableItem() || !self().isDamageableItem()) {
            return !ItemStack.isSameItemSameComponents(newStack, self());
        }
        return !ItemStack.isSameItemSameComponents(newStack, self());
    }

    default boolean isPrimaryItemFor(Holder<Enchantment> enchantment) {
        Item item = self().getItem();
        if (item instanceof IItemExtension ext) {
            return ext.isPrimaryItemFor(self(), enchantment);
        }
        if (item == Items.BOOK) return true;
        java.util.Optional<net.minecraft.core.HolderSet<Item>> primaryItems = enchantment.value().definition().primaryItems();
        return this.supportsEnchantment(enchantment) && (primaryItems.isEmpty() || self().is(primaryItems.get()));
    }

    default boolean supportsEnchantment(Holder<Enchantment> enchantment) {
        Item item = self().getItem();
        if (item instanceof IItemExtension ext) {
            return ext.supportsEnchantment(self(), enchantment);
        }
        return self().is(Items.ENCHANTED_BOOK) || enchantment.value().isSupportedItem(self());
    }

    default int getEnchantmentLevel(Holder<Enchantment> enchantment) {
        Item item = self().getItem();
        if (item instanceof IItemExtension ext) {
            return ext.getEnchantmentLevel(self(), enchantment);
        }
        return EnchantmentHelper.getEnchantmentsForCrafting(self()).getLevel(enchantment);
    }

    default ItemEnchantments getAllEnchantments(RegistryLookup<Enchantment> lookup) {
        Item item = self().getItem();
        if (item instanceof IItemExtension ext) {
            return ext.getAllEnchantments(self(), lookup);
        }
        return EnchantmentHelper.getEnchantmentsForCrafting(self());
    }

    default int getEnchantmentValue() {
        return self().getItem().getEnchantmentValue(self());
    }

    @Nullable
    default EquipmentSlot getEquipmentSlot() {
        return self().getItem().getEquipmentSlot(self());
    }

    default boolean canDisableShield(ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return self().getItem().canDisableShield(self(), shield, entity, attacker);
    }

    @SuppressWarnings("removal")
    @Deprecated(forRemoval = true, since = "21.1")
    default boolean onEntitySwing(LivingEntity entity) {
        return self().getItem().onEntitySwing(self(), entity);
    }

    default boolean onEntitySwing(LivingEntity entity, InteractionHand hand) {
        Item item = self().getItem();
        if (item instanceof IItemExtension ext) {
            return ext.onEntitySwing(self(), entity, hand);
        }
        return item.onEntitySwing(self(), entity);
    }

    default void onStopUsing(LivingEntity entity, int count) {
        self().getItem().onStopUsing(self(), entity, count);
    }

    default int getEntityLifespan(Level level) {
        return self().getItem().getEntityLifespan(self(), level);
    }

    default boolean onEntityItemUpdate(ItemEntity entity) {
        return self().getItem().onEntityItemUpdate(self(), entity);
    }

    default float getXpRepairRatio() {
        Item item = self().getItem();
        if (item instanceof IItemExtension ext) {
            return ext.getXpRepairRatio(self());
        }
        return 1f;
    }

    default void onAnimalArmorTick(Level level, Mob horse) {
        Item item = self().getItem();
        if (item instanceof IItemExtension ext) {
            ext.onAnimalArmorTick(self(), level, horse);
        } else {
            item.onHorseArmorTick(self(), level, horse);
        }
    }

    default boolean canEquip(EquipmentSlot armorType, LivingEntity entity) {
        return self().getItem().canEquip(self(), armorType, entity);
    }

    default boolean isBookEnchantable(ItemStack book) {
        return self().getItem().isBookEnchantable(self(), book);
    }

    default boolean onDroppedByPlayer(Player player) {
        return self().getItem().onDroppedByPlayer(self(), player);
    }

    default Component getHighlightTip(Component displayName) {
        return self().getItem().getHighlightTip(self(), displayName);
    }

    default boolean doesSneakBypassUse(net.minecraft.world.level.LevelReader level, BlockPos pos, Player player) {
        return self().isEmpty() || self().getItem().doesSneakBypassUse(self(), level, pos, player);
    }

    default boolean isRepairable() {
        Item item = self().getItem();
        if (item instanceof IItemExtension ext) {
            return ext.isRepairable(self());
        }
        return self().isDamageableItem();
    }

    default boolean isPiglinCurrency() {
        return self().getItem().isPiglinCurrency(self());
    }

    default boolean makesPiglinsNeutral(LivingEntity wearer) {
        return self().getItem().makesPiglinsNeutral(self(), wearer);
    }

    default boolean isEnderMask(Player player, EnderMan endermanEntity) {
        return self().getItem().isEnderMask(self(), player, endermanEntity);
    }

    default boolean canElytraFly(LivingEntity entity) {
        return self().getItem().canElytraFly(self(), entity);
    }

    default boolean elytraFlightTick(LivingEntity entity, int flightTicks) {
        return self().getItem().elytraFlightTick(self(), entity, flightTicks);
    }

    default boolean canWalkOnPowderedSnow(LivingEntity wearer) {
        return self().getItem().canWalkOnPowderedSnow(self(), wearer);
    }

    default AABB getSweepHitBox(Player player, Entity target) {
        return self().getItem().getSweepHitBox(self(), player, target);
    }

    default void onDestroyed(ItemEntity itemEntity, DamageSource damageSource) {
        self().getItem().onDestroyed(itemEntity, damageSource);
    }

    @Nullable
    default FoodProperties getFoodProperties(@Nullable LivingEntity entity) {
        Item item = self().getItem();
        if (item instanceof IItemExtension ext) {
            return ext.getFoodProperties(self(), entity);
        }
        return self().get(DataComponents.FOOD);
    }

    default boolean isNotReplaceableByPickAction(Player player, int inventorySlot) {
        return self().getItem().isNotReplaceableByPickAction(self(), player, inventorySlot);
    }

    default boolean canGrindstoneRepair() {
        return self().getItem().canGrindstoneRepair(self());
    }

    @Nullable
    default <T, C> T getCapability(ItemCapability<T, C> capability, @Nullable C context) {
        return capability.getCapability(self(), context);
    }

    @Nullable
    default <T> T getCapability(ItemCapability<T, Void> capability) {
        return capability.getCapability(self(), null);
    }

    default ItemAttributeModifiers getAttributeModifiers() {
        ItemAttributeModifiers defaultModifiers = self().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        if (defaultModifiers.modifiers().isEmpty()) {
            defaultModifiers = self().getItem().getDefaultAttributeModifiers(self());
        }
        return CommonHooks.computeModifiedAttributes(self(), defaultModifiers);
    }

    default boolean canFitInsideContainerItems() {
        Item item = self().getItem();
        if (item instanceof IItemExtension ext) {
            return ext.canFitInsideContainerItems(self());
        }
        return item.canFitInsideContainerItems();
    }
}

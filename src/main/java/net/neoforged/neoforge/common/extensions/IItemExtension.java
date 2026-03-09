package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantment.EnchantmentDefinition;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.extensions.IForgeItem;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public interface IItemExtension extends IForgeItem {

    default Item self() {
        return (Item) this;
    }

    default boolean isRepairable(ItemStack stack) {
        return stack.isDamageableItem();
    }

    default float getXpRepairRatio(ItemStack stack) {
        return 1f;
    }

    default boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        return self().onEntitySwing(stack, entity);
    }

    default int getDamage(ItemStack stack) {
        return Mth.clamp(stack.getOrDefault(DataComponents.DAMAGE, 0), 0, stack.getMaxDamage());
    }

    default int getMaxDamage(ItemStack stack) {
        return stack.getOrDefault(DataComponents.MAX_DAMAGE, 0);
    }

    default boolean isDamaged(ItemStack stack) {
        return stack.getDamageValue() > 0;
    }

    default void setDamage(ItemStack stack, int damage) {
        stack.set(DataComponents.DAMAGE, Mth.clamp(damage, 0, stack.getMaxDamage()));
    }

    default boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return self().canPerformAction(stack, ToolAction.get(itemAbility.name()));
    }

    default int getMaxStackSize(ItemStack stack) {
        return stack.getOrDefault(DataComponents.MAX_STACK_SIZE, 1);
    }

    @ApiStatus.OverrideOnly
    default boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        if (stack.getItem() == Items.BOOK) {
            return true;
        }
        Optional<net.minecraft.core.HolderSet<Item>> primaryItems = enchantment.value().definition().primaryItems();
        return this.supportsEnchantment(stack, enchantment) && (primaryItems.isEmpty() || stack.is(primaryItems.get()));
    }

    @ApiStatus.OverrideOnly
    default boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return stack.is(Items.ENCHANTED_BOOK) || enchantment.value().isSupportedItem(stack);
    }

    @ApiStatus.OverrideOnly
    default int getEnchantmentLevel(ItemStack stack, Holder<Enchantment> enchantment) {
        return EnchantmentHelper.getEnchantmentsForCrafting(stack).getLevel(enchantment);
    }

    @ApiStatus.OverrideOnly
    default ItemEnchantments getAllEnchantments(ItemStack stack, RegistryLookup<Enchantment> lookup) {
        return EnchantmentHelper.getEnchantmentsForCrafting(stack);
    }

    default boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !oldStack.equals(newStack);
    }

    default boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        if (!newStack.is(oldStack.getItem())) {
            return true;
        }
        if (!newStack.isDamageableItem() || !oldStack.isDamageableItem()) {
            return !ItemStack.isSameItemSameComponents(newStack, oldStack);
        }

        DataComponentMap newComponents = newStack.getComponents();
        DataComponentMap oldComponents = oldStack.getComponents();
        if (newComponents.isEmpty() || oldComponents.isEmpty()) {
            return !(newComponents.isEmpty() && oldComponents.isEmpty());
        }

        Set<DataComponentType<?>> newKeys = new HashSet<>(newComponents.keySet());
        Set<DataComponentType<?>> oldKeys = new HashSet<>(oldComponents.keySet());
        newKeys.remove(DataComponents.DAMAGE);
        oldKeys.remove(DataComponents.DAMAGE);
        if (!newKeys.equals(oldKeys)) {
            return true;
        }
        return !newKeys.stream().allMatch(key -> Objects.equals(newComponents.get(key), oldComponents.get(key)));
    }

    default boolean canContinueUsing(ItemStack oldStack, ItemStack newStack) {
        if (oldStack == newStack) {
            return true;
        }
        return !oldStack.isEmpty() && !newStack.isEmpty() && ItemStack.isSameItem(newStack, oldStack);
    }

    @Nullable
    default String getCreatorModId(ItemStack itemStack) {
        return ForgeHooks.getDefaultCreatorModId(itemStack);
    }

    default void onAnimalArmorTick(ItemStack stack, Level level, Mob horse) {
        self().onHorseArmorTick(stack, level, horse);
    }

    default boolean isEnderMask(ItemStack stack, Player player, EnderMan endermanEntity) {
        return self().isEnderMask(stack, player, endermanEntity);
    }

    default boolean isDamageable(ItemStack stack) {
        return stack.has(DataComponents.MAX_DAMAGE);
    }

    @Nullable
    default FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        return stack.get(DataComponents.FOOD);
    }

    default boolean canBeHurtBy(ItemStack stack, DamageSource source) {
        return true;
    }

    default ItemStack applyEnchantments(ItemStack stack, List<EnchantmentInstance> enchantments) {
        if (stack.is(Items.BOOK)) {
            stack = stack.transmuteCopy(Items.ENCHANTED_BOOK);
        }
        for (EnchantmentInstance instance : enchantments) {
            stack.enchant(instance.enchantment, instance.level);
        }
        return stack;
    }

    default boolean canFitInsideContainerItems(ItemStack stack) {
        return self().canFitInsideContainerItems();
    }
}

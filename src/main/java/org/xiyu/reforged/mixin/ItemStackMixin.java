package org.xiyu.reforged.mixin;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.common.extensions.IItemStackExtension;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Adds NeoForge-style {@code Supplier<DataComponentType>} overloads to {@link ItemStack}.
 *
 * <p>NeoForge adds convenience methods that accept {@code Supplier<DataComponentType>}
 * (typically a {@code DeferredHolder}), while Forge only has methods accepting
 * {@code DataComponentType} directly. NeoForge mods call these overloads
 * (e.g., {@code stack.has(MY_COMPONENT)}) which would fail with
 * {@link NoSuchMethodError} on Forge without this mixin.</p>
 *
 * <p>Note: {@code has}, {@code get}, {@code getOrDefault} come from the
 * {@code DataComponentHolder} interface (default methods), so we call them
 * via a cast to {@code ItemStack} rather than using {@code @Shadow}.</p>
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements IItemStackExtension {

    // ── Helper: self-cast to ItemStack ─────────────────────────────────────

    private ItemStack stackSelf() {
        return (ItemStack) (Object) this;
    }

    // ── NeoForge Supplier<DataComponentType> overloads ─────────────────────

    public boolean has(Supplier<? extends DataComponentType<?>> type) {
        return stackSelf().has(type.get());
    }

    @Nullable
    public <T> T get(Supplier<? extends DataComponentType<? extends T>> type) {
        return stackSelf().get(type.get());
    }

    public <T> T getOrDefault(Supplier<? extends DataComponentType<? extends T>> type, T defaultValue) {
        return stackSelf().getOrDefault(type.get(), defaultValue);
    }

    @Nullable
    public <T> T set(Supplier<? extends DataComponentType<? super T>> type, @Nullable T value) {
		return stackSelf().set(type.get(), value);
    }

    @Nullable
    public <T> T remove(Supplier<? extends DataComponentType<? extends T>> type) {
		return stackSelf().remove(type.get());
    }

    public <T> T update(Supplier<? extends DataComponentType<T>> type, T defaultValue, UnaryOperator<T> updater) {
		return stackSelf().update(type.get(), defaultValue, updater);
    }

    @Nullable
    public <T, C> T getCapability(ItemCapability<T, C> capability, @Nullable C context) {
        return capability.getCapability(stackSelf(), context);
    }

    @Nullable
    public <T> T getCapability(ItemCapability<T, Void> capability) {
        return capability.getCapability(stackSelf(), null);
    }

    // ── NeoForge hurtAndBreak(LivingEntity) overload ───────────────────────
    // NeoForge adds an overload accepting LivingEntity instead of just ServerPlayer.
    // Forge only has: hurtAndBreak(int, ServerLevel, @Nullable ServerPlayer, Consumer<Item>)
    // We delegate by extracting ServerPlayer if the entity is one, otherwise pass null.

    public void hurtAndBreak(int amount, ServerLevel level, @Nullable LivingEntity entity, Consumer<Item> onBreak) {
        ServerPlayer sp = entity instanceof ServerPlayer p ? p : null;
        stackSelf().hurtAndBreak(amount, level, sp, onBreak);
    }

    // ── Safe tooltip rendering ─────────────────────────────────────────────

    private static final Logger REFORGED_TOOLTIP_LOGGER = LogManager.getLogger("ReForged");

    @Redirect(
        method = "getTooltipLines(Lnet/minecraft/world/item/Item$TooltipContext;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/TooltipFlag;)Ljava/util/List;",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/Item;appendHoverText(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/List;Lnet/minecraft/world/item/TooltipFlag;)V"
        ),
        remap = false
    )
    private void reforged$safeAppendHoverText(Item item, ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        try {
            item.appendHoverText(stack, context, tooltip, flag);
        } catch (Exception e) {
            REFORGED_TOOLTIP_LOGGER.debug("[ReForged] Tooltip error for {}: {}", item.getClass().getName(), e.getMessage());
        }
    }
}

package org.xiyu.reforged.mixin;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
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
public abstract class ItemStackMixin {

    // ── Shadow only methods declared directly in ItemStack ─────────────────

    @Shadow @Nullable
    public abstract <T> T set(DataComponentType<? super T> type, @Nullable T value);

    @Shadow @Nullable
    public abstract <T> T remove(DataComponentType<? extends T> type);

    @Shadow
    public abstract <T> T update(DataComponentType<T> type, T defaultValue, UnaryOperator<T> updater);

    // ── Helper: self-cast to ItemStack ─────────────────────────────────────

    private ItemStack self() {
        return (ItemStack) (Object) this;
    }

    // ── NeoForge Supplier<DataComponentType> overloads ─────────────────────

    public boolean has(Supplier<? extends DataComponentType<?>> type) {
        return self().has(type.get());
    }

    @Nullable
    public <T> T get(Supplier<? extends DataComponentType<? extends T>> type) {
        return self().get(type.get());
    }

    public <T> T getOrDefault(Supplier<? extends DataComponentType<? extends T>> type, T defaultValue) {
        return self().getOrDefault(type.get(), defaultValue);
    }

    @Nullable
    public <T> T set(Supplier<? extends DataComponentType<? super T>> type, @Nullable T value) {
        return this.set(type.get(), value);
    }

    @Nullable
    public <T> T remove(Supplier<? extends DataComponentType<? extends T>> type) {
        return this.remove(type.get());
    }

    public <T> T update(Supplier<? extends DataComponentType<T>> type, T defaultValue, UnaryOperator<T> updater) {
        return this.update(type.get(), defaultValue, updater);
    }
}

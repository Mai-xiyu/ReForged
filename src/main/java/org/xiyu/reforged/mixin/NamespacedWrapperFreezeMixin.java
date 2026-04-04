package org.xiyu.reforged.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Forge freezes registries by rejecting any dangling intrusive holders.
 *
 * <p>When a NeoForge mod triggers class init failures during registration,
 * temporary intrusive holders can remain unbound and crash freeze. We drop only
 * unbound entries so freeze can continue.</p>
 */
@Mixin(targets = "net.minecraftforge.registries.NamespacedWrapper", remap = false)
public class NamespacedWrapperFreezeMixin<T> {

    private static final Field REFORGED$UNREGISTERED_INTRUSIVE_HOLDERS;

    static {
        try {
            Field field = net.minecraft.core.MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
            field.setAccessible(true);
            REFORGED$UNREGISTERED_INTRUSIVE_HOLDERS = field;
        } catch (Exception e) {
            throw new RuntimeException("Failed to resolve MappedRegistry.unregisteredIntrusiveHolders", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<T, Holder.Reference<T>> reforged$getIntrusiveHolders() {
        try {
            return (Map<T, Holder.Reference<T>>) REFORGED$UNREGISTERED_INTRUSIVE_HOLDERS.get(this);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private void reforged$setIntrusiveHolders(Map<T, Holder.Reference<T>> map) {
        try {
            REFORGED$UNREGISTERED_INTRUSIVE_HOLDERS.set(this, map);
        } catch (IllegalAccessException ignored) {
        }
    }

    @Inject(method = "freeze()Lnet/minecraft/core/Registry;", at = @At("HEAD"), remap = false)
    private void reforged$pruneDanglingIntrusiveHolders(CallbackInfoReturnable<Registry<T>> cir) {
        Map<T, Holder.Reference<T>> intrusiveHolders = reforged$getIntrusiveHolders();
        if (intrusiveHolders == null || intrusiveHolders.isEmpty()) {
            return;
        }

        intrusiveHolders.entrySet().removeIf(entry -> {
            Holder.Reference<T> holder = entry.getValue();
            return holder == null || !holder.isBound();
        });

        if (intrusiveHolders.isEmpty()) {
            reforged$setIntrusiveHolders(null);
        }
    }
}
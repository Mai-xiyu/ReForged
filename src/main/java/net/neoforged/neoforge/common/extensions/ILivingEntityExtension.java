package net.neoforged.neoforge.common.extensions;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.extensions.IForgeLivingEntity;
import net.neoforged.neoforge.common.damagesource.DamageContainer;

/**
 * Stub extension interface for LivingEntity.
 *
 * <p><b>IMPORTANT:</b> Methods that also exist as {@code default} in Forge's
 * {@link IForgeLivingEntity} with the same JVM descriptor are intentionally
 * omitted to avoid {@code IncompatibleClassChangeError}. LivingEntity inherits
 * those defaults from IForgeLivingEntity automatically.</p>
 *
 * <p>Omitted conflicting methods:
 * <ul>
 *   <li>{@code self()} — same return type LivingEntity</li>
 *   <li>{@code moveInFluid(FluidState, Vec3, double)} — same parameter types</li>
 * </ul></p>
 */
public interface ILivingEntityExtension extends IEntityExtension {

    // FluidType-parameterized methods — NeoForge FluidType != Forge FluidType → no conflict

    default boolean canSwimInFluidType(net.neoforged.neoforge.fluids.FluidType type) {
        return ((IForgeLivingEntity) (Object) this).canSwimInFluidType(type);
    }

    default void jumpInFluid(net.neoforged.neoforge.fluids.FluidType type) {
        ((IForgeLivingEntity) (Object) this).jumpInFluid(type);
    }

    default void sinkInFluid(net.neoforged.neoforge.fluids.FluidType type) {
        ((IForgeLivingEntity) (Object) this).sinkInFluid(type);
    }

    default boolean canDrownInFluidType(net.neoforged.neoforge.fluids.FluidType type) {
        return ((IForgeLivingEntity) (Object) this).canDrownInFluidType(type);
    }

    // NeoForge-only method

    default void onDamageTaken(DamageContainer damageContainer) {
    }

    // ─── INTENTIONALLY OMITTED (conflict with IForgeLivingEntity defaults) ──
    // self() → ()Lnet/minecraft/world/entity/LivingEntity;
    // moveInFluid(FluidState, Vec3, double) → same parameter types
}

package net.neoforged.neoforge.common.extensions;

import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.extensions.IForgeLivingEntity;
import net.neoforged.neoforge.common.damagesource.DamageContainer;

/**
 * Stub extension interface for LivingEntity.
 */
public interface ILivingEntityExtension extends IEntityExtension {

    default LivingEntity self() {
        return (LivingEntity) this;
    }

    default boolean canSwimInFluidType(net.neoforged.neoforge.fluids.FluidType type) {
        return ((IForgeLivingEntity) self()).canSwimInFluidType(type);
    }

    default void jumpInFluid(net.neoforged.neoforge.fluids.FluidType type) {
        ((IForgeLivingEntity) self()).jumpInFluid(type);
    }

    default void sinkInFluid(net.neoforged.neoforge.fluids.FluidType type) {
        ((IForgeLivingEntity) self()).sinkInFluid(type);
    }

    default boolean canDrownInFluidType(net.neoforged.neoforge.fluids.FluidType type) {
        return ((IForgeLivingEntity) self()).canDrownInFluidType(type);
    }

    default boolean moveInFluid(FluidState state, Vec3 movementVector, double gravity) {
        return ((IForgeLivingEntity) self()).moveInFluid(state, movementVector, gravity);
    }

    default void onDamageTaken(DamageContainer damageContainer) {
    }
}

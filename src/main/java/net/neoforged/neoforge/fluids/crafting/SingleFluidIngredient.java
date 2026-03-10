/*
 * Copyright (c) 2025-2026 ReForged Team
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.neoforged.neoforge.fluids.crafting;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * Fluid ingredient that matches a single fluid.
 */
public class SingleFluidIngredient extends FluidIngredient {
    public static final MapCodec<SingleFluidIngredient> CODEC = FluidStack.FLUID_NON_EMPTY_CODEC
            .xmap(SingleFluidIngredient::new, SingleFluidIngredient::fluid).fieldOf("fluid");

    private final Holder<Fluid> fluid;

    public SingleFluidIngredient(Holder<Fluid> fluid) {
        this.fluid = fluid;
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return fluidStack.is(fluid);
    }

    protected Stream<FluidStack> generateStacks() {
        return Stream.of(new FluidStack(fluid, FluidType.BUCKET_VOLUME));
    }

    @Override
    public boolean isEmpty() {
        return fluid.is(Fluids.EMPTY.builtInRegistryHolder());
    }

    @Override
    public FluidStack[] getStacks() {
        return generateStacks().toArray(FluidStack[]::new);
    }

    @Override
    public boolean hasNoFluids() {
        return false;
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public FluidIngredientType<?> getType() {
        return null; // type registration not required for shim
    }

    @Override
    public List<FluidStack> fluids() {
        return generateStacks().toList();
    }

    @Override
    public int hashCode() {
        return fluid.value().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof SingleFluidIngredient other && other.fluid.is(this.fluid);
    }

    public Holder<Fluid> fluid() {
        return fluid;
    }
}

package net.neoforged.neoforge.fluids;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

/**
 * Simple fluid data component content.
 */
public class SimpleFluidContent {
    public static final SimpleFluidContent EMPTY = new SimpleFluidContent(FluidStack.EMPTY);

    public static final Codec<SimpleFluidContent> CODEC = FluidStack.CODEC.xmap(
            SimpleFluidContent::new, c -> c.fluidStack
    );

    private final FluidStack fluidStack;

    private SimpleFluidContent(FluidStack fluidStack) {
        this.fluidStack = fluidStack;
    }

    public static SimpleFluidContent copyOf(FluidStack stack) {
        if (stack.isEmpty()) return EMPTY;
        return new SimpleFluidContent(stack.copy());
    }

    public FluidStack copy() {
        return fluidStack.copy();
    }

    public boolean isEmpty() {
        return fluidStack.isEmpty();
    }

    public Fluid getFluid() {
        return fluidStack.getFluid();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SimpleFluidContent other)) return false;
        return FluidStack.isSameFluidSameComponents(this.fluidStack, other.fluidStack)
                && this.fluidStack.getAmount() == other.fluidStack.getAmount();
    }

    @Override
    public int hashCode() {
        return fluidStack.hashCode();
    }
}

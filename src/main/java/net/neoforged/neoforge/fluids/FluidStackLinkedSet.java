package net.neoforged.neoforge.fluids;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

/**
 * Provides a linked set implementation for FluidStack with custom hashing.
 */
public class FluidStackLinkedSet {
    /**
     * Hash strategy that compares FluidStacks by fluid type and components.
     */
    public static final Hash.Strategy<FluidStack> TYPE_AND_COMPONENTS = new Hash.Strategy<>() {
        @Override
        public int hashCode(@Nullable FluidStack stack) {
            if (stack == null || stack.isEmpty()) return 0;
            return stack.hashCode();
        }

        @Override
        public boolean equals(@Nullable FluidStack a, @Nullable FluidStack b) {
            if (a == b) return true;
            if (a == null || b == null) return false;
            return FluidStack.isSameFluidSameComponents(a, b);
        }
    };

    /**
     * Creates a new linked set for FluidStacks using type-and-components hashing.
     */
    public static Set<FluidStack> createTypeAndComponentsSet() {
        return new ObjectLinkedOpenCustomHashSet<>(TYPE_AND_COMPONENTS);
    }

    private FluidStackLinkedSet() {}
}

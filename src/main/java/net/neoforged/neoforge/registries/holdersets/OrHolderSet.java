package net.neoforged.neoforge.registries.holdersets;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;

/**
 * OR holder set — matches elements present in ANY child set (union).
 */
public class OrHolderSet<T> extends CompositeHolderSet<T> {
    public OrHolderSet(List<HolderSet<T>> values) {
        super(values);
    }

    @SafeVarargs
    public OrHolderSet(HolderSet<T>... values) {
        this(Arrays.asList(values));
    }

    public HolderSetType type() {
        return net.neoforged.neoforge.common.NeoForgeMod.OR_HOLDER_SET;
    }

    @Override
    protected Set<Holder<T>> createSet() {
        Set<Holder<T>> result = new LinkedHashSet<>();
        for (HolderSet<T> comp : getComponents()) {
            comp.stream().forEach(result::add);
        }
        return result;
    }

    @Override
    public String toString() { return "OrHolderSet[" + getComponents() + "]"; }
}

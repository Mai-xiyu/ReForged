package net.neoforged.neoforge.registries.holdersets;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;

/**
 * AND holder set — matches elements present in ALL child sets (intersection).
 */
public class AndHolderSet<T> extends CompositeHolderSet<T> {
    public AndHolderSet(List<HolderSet<T>> values) {
        super(values);
    }

    @SafeVarargs
    public AndHolderSet(HolderSet<T>... values) {
        this(Arrays.asList(values));
    }

    public HolderSetType type() {
        return net.neoforged.neoforge.common.NeoForgeMod.AND_HOLDER_SET;
    }

    @Override
    protected Set<Holder<T>> createSet() {
        List<HolderSet<T>> comps = getComponents();
        if (comps.isEmpty()) return Set.of();
        Set<Holder<T>> result = comps.get(0).stream().collect(Collectors.toSet());
        for (int i = 1; i < comps.size(); i++) {
            Set<Holder<T>> other = comps.get(i).stream().collect(Collectors.toSet());
            result.retainAll(other);
        }
        return result;
    }

    @Override
    public String toString() { return "AndHolderSet[" + getComponents() + "]"; }
}

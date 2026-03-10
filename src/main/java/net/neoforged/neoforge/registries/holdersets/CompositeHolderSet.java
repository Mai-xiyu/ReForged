package net.neoforged.neoforge.registries.holdersets;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Base class for composite holder sets (AND, OR, etc.).
 */
public abstract class CompositeHolderSet<T> extends HolderSet.ListBacked<T> implements ICustomHolderSet<T> {
    private final List<HolderSet<T>> components;
    private List<Holder<T>> resolvedContents;

    protected CompositeHolderSet() {
        this.components = Collections.emptyList();
    }

    protected CompositeHolderSet(List<HolderSet<T>> components) {
        this.components = List.copyOf(components);
    }

    public List<HolderSet<T>> getComponents() {
        return components;
    }

    /**
     * Returns the component list suitable for codec serialization.
     * <p>In the shim layer we simply return the raw components; NeoForge's full
     * implementation normalises non-{@link ICustomHolderSet} entries for a
     * homogeneous serialisation type, but that is unnecessary here.</p>
     */
    public List<HolderSet<T>> homogenize() {
        return getComponents();
    }

    /**
     * Subclasses override this to define how child sets are combined.
     */
    protected abstract Set<Holder<T>> createSet();

    @Override
    protected List<Holder<T>> contents() {
        if (resolvedContents == null) {
            resolvedContents = new ArrayList<>(createSet());
        }
        return resolvedContents;
    }

    @Override
    public Optional<TagKey<T>> unwrapKey() {
        return Optional.empty();
    }

    @Override
    public Either<TagKey<T>, List<Holder<T>>> unwrap() {
        return Either.right(contents());
    }

    @Override
    public boolean contains(Holder<T> holder) {
        return contents().contains(holder);
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> owner) {
        return true;
    }
}

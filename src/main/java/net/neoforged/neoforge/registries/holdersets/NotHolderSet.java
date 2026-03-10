package net.neoforged.neoforge.registries.holdersets;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * NOT holder set — matches elements NOT in the child set.
 */
public class NotHolderSet<T> implements ICustomHolderSet<T> {
    private final List<Runnable> owners = new ArrayList<>();
    private final HolderLookup.RegistryLookup<T> registryLookup;
    private final HolderSet<T> value;
    @Nullable
    private List<Holder<T>> list = null;

    public NotHolderSet(HolderLookup.RegistryLookup<T> registryLookup, HolderSet<T> value) {
        this.registryLookup = registryLookup;
        this.value = value;
    }

    public HolderLookup.RegistryLookup<T> registryLookup() {
        return registryLookup;
    }

    public HolderSet<T> value() {
        return value;
    }

    @Override
    public HolderSetType type() {
        return net.neoforged.neoforge.common.NeoForgeMod.NOT_HOLDER_SET;
    }

    @Override
    public void addInvalidationListener(Runnable runnable) {
        this.owners.add(runnable);
    }

    @Override
    public Iterator<Holder<T>> iterator() {
        return getList().iterator();
    }

    @Override
    public Stream<Holder<T>> stream() {
        return getList().stream();
    }

    @Override
    public int size() {
        return getList().size();
    }

    @Override
    public Either<TagKey<T>, List<Holder<T>>> unwrap() {
        return Either.right(getList());
    }

    @Override
    public Optional<Holder<T>> getRandomElement(RandomSource random) {
        List<Holder<T>> l = getList();
        return l.isEmpty() ? Optional.empty() : Optional.of(l.get(random.nextInt(l.size())));
    }

    @Override
    public Holder<T> get(int i) {
        return getList().get(i);
    }

    @Override
    public boolean contains(Holder<T> holder) {
        return !value.contains(holder);
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> owner) {
        return value.canSerializeIn(owner);
    }

    @Override
    public Optional<TagKey<T>> unwrapKey() {
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "NotSet(" + value + ")";
    }

    private List<Holder<T>> getList() {
        List<Holder<T>> thisList = this.list;
        if (thisList == null) {
            thisList = registryLookup.listElements()
                    .filter(holder -> !value.contains(holder))
                    .map(Function.identity())
                    .collect(java.util.stream.Collectors.toList());
            this.list = thisList;
        }
        return thisList;
    }

    private void invalidate() {
        this.list = null;
        for (Runnable runnable : owners) {
            runnable.run();
        }
    }
}

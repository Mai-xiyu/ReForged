package net.neoforged.neoforge.registries.holdersets;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderOwner;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * ANY holder set — matches all elements in the registry.
 */
public record AnyHolderSet<T>(HolderLookup.RegistryLookup<T> registryLookup) implements ICustomHolderSet<T> {

    @Override
    public HolderSetType type() {
        return net.neoforged.neoforge.common.NeoForgeMod.ANY_HOLDER_SET;
    }

    @Override
    public Iterator<Holder<T>> iterator() {
        return stream().iterator();
    }

    @Override
    public Stream<Holder<T>> stream() {
        return registryLookup.listElements().map(Function.identity());
    }

    @Override
    public int size() {
        return (int) stream().count();
    }

    @Override
    public Either<TagKey<T>, List<Holder<T>>> unwrap() {
        return Either.right(stream().toList());
    }

    @Override
    public Optional<Holder<T>> getRandomElement(RandomSource random) {
        List<Holder<T>> list = stream().toList();
        if (list.isEmpty()) return Optional.empty();
        return Optional.of(list.get(random.nextInt(list.size())));
    }

    @Override
    public Holder<T> get(int i) {
        return stream().toList().get(i);
    }

    @Override
    public boolean contains(Holder<T> holder) {
        return holder.unwrapKey().isPresent()
                && registryLookup.get(holder.unwrapKey().get()).isPresent();
    }

    @Override
    public boolean canSerializeIn(HolderOwner<T> owner) {
        return true;
    }

    @Override
    public Optional<TagKey<T>> unwrapKey() {
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "AnySet(" + registryLookup.key() + ")";
    }
}

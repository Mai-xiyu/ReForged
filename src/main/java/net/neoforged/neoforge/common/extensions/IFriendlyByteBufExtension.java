package net.neoforged.neoforge.common.extensions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Extension interface for {@link FriendlyByteBuf}.
 */
public interface IFriendlyByteBufExtension {

    private FriendlyByteBuf self() { return (FriendlyByteBuf) this; }

    default <T> void writeObjectCollection(Collection<T> set, BiConsumer<T, FriendlyByteBuf> writer) {
        self().writeVarInt(set.size());
        for (T t : set) writer.accept(t, self());
    }

    default <T> List<T> readObjectCollection(Function<FriendlyByteBuf, T> reader) {
        int size = self().readVarInt();
        List<T> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) list.add(reader.apply(self()));
        return list;
    }
}

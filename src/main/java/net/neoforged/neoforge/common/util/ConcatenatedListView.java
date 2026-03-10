package net.neoforged.neoforge.common.util;

import java.util.*;
import java.util.stream.Stream;

/**
 * An unmodifiable view that concatenates multiple lists into one.
 */
public class ConcatenatedListView<E> extends AbstractList<E> {
    private final List<List<E>> lists;

    private ConcatenatedListView(List<List<E>> lists) {
        this.lists = lists;
    }

    @SafeVarargs
    public static <E> List<E> of(List<E>... lists) {
        return new ConcatenatedListView<>(List.of(lists));
    }

    public static <E> List<E> of(List<List<E>> lists) {
        return new ConcatenatedListView<>(lists);
    }

    @Override
    public E get(int index) {
        int offset = 0;
        for (List<E> list : lists) {
            if (index < offset + list.size()) {
                return list.get(index - offset);
            }
            offset += list.size();
        }
        throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + offset);
    }

    @Override
    public int size() {
        int total = 0;
        for (List<E> list : lists) total += list.size();
        return total;
    }

    @Override
    public boolean isEmpty() {
        for (List<E> list : lists) {
            if (!list.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public Stream<E> stream() {
        return lists.stream().flatMap(Collection::stream);
    }
}

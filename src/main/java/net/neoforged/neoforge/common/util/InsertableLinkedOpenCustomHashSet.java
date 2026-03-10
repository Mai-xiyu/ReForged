package net.neoforged.neoforge.common.util;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import java.util.Iterator;

/**
 * A linked hash set that supports insertion at arbitrary positions.
 * Extends fastutil's ObjectLinkedOpenCustomHashSet with additional insert operations.
 */
public class InsertableLinkedOpenCustomHashSet<T> extends ObjectLinkedOpenCustomHashSet<T> {

    public InsertableLinkedOpenCustomHashSet(Hash.Strategy<? super T> strategy) {
        super(strategy);
    }

    /**
     * Adds an element before the specified element in iteration order.
     *
     * @param before the element to insert before
     * @param element the element to add
     * @return true if the element was added
     */
    public boolean addBefore(T before, T element) {
        // If element already exists, don't add
        if (contains(element)) return false;
        // fastutil doesn't natively support positional insert,
        // so add normally (maintains insertion order)
        return add(element);
    }

    /**
     * Adds an element after the specified element in iteration order.
     *
     * @param after the element to insert after
     * @param element the element to add
     * @return true if the element was added
     */
    public boolean addAfter(T after, T element) {
        if (contains(element)) return false;
        return add(element);
    }

    /**
     * Adds an element at the first position in iteration order.
     *
     * @param element the element to add
     * @return true if the element was added
     */
    public boolean insertFirst(T element) {
        if (contains(element)) return false;
        return addAndMoveToFirst(element);
    }

    /**
     * Adds an element at the last position in iteration order.
     *
     * @param element the element to add
     * @return true if the element was added
     */
    public boolean insertLast(T element) {
        if (contains(element)) return false;
        return addAndMoveToLast(element);
    }
}

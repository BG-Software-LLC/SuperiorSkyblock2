package com.bgsoftware.superiorskyblock.api.objects;

import java.util.Map;

/**
 * This class represents a pair of elements.
 *
 * @param <E1> First element of this pair.
 * @param <E2> Second element of this pair.
 */
public class Pair<E1, E2> {

    private E1 firstElement;
    private E2 secondElement;

    /**
     * Create a new pair out of a {@link Map.Entry} object.
     *
     * @param entry The entry to create the pair from.
     */
    public Pair(Map.Entry<E1, E2> entry) {
        this(entry.getKey(), entry.getValue());
    }

    /**
     * Create a new pair out of two elements.
     *
     * @param firstElement  The first element of this pair.
     * @param secondElement The second element of this pair.
     */
    public Pair(E1 firstElement, E2 secondElement) {
        this.firstElement = firstElement;
        this.secondElement = secondElement;
    }

    /**
     * Get the first element of this pair.
     */
    public E1 getKey() {
        return this.firstElement;
    }

    /**
     * Set the first element of this pair.
     *
     * @param element The new element to set.
     */
    public void setKey(E1 element) {
        this.firstElement = element;
    }

    /**
     * Get the second element of this pair.
     */
    public E2 getValue() {
        return this.secondElement;
    }

    /**
     * Set the second element of this pair.
     *
     * @param element The new element to set.
     */
    public void setValue(E2 element) {
        this.secondElement = element;
    }

    @Override
    public String toString() {
        return "{" + this.firstElement + "=" + this.secondElement + "}";
    }

}

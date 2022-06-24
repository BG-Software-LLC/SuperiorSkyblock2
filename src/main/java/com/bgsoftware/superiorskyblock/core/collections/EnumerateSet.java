package com.bgsoftware.superiorskyblock.core.collections;

import com.bgsoftware.superiorskyblock.api.objects.Enumerable;

import java.util.Arrays;
import java.util.Collection;

public class EnumerateSet<K extends Enumerable> {

    private boolean[] values;
    private int size = 0;

    public EnumerateSet(Collection<K> enumerables) {
        this.values = new boolean[enumerables.size()];
    }

    public EnumerateSet(EnumerateSet<K> other) {
        this.values = other.values.clone();
        this.size = other.size;
    }

    public int size() {
        return this.size;
    }

    public boolean isEmpty() {
        return size() <= 0;
    }

    public boolean contains(K key) {
        return isValidKey(key) && this.values[key.ordinal()];
    }

    public boolean add(K key) {
        this.ensureCapacity(key.ordinal() + 1);

        boolean containedBefore = this.values[key.ordinal()];
        this.values[key.ordinal()] = true;

        if (!containedBefore)
            ++this.size;

        return !containedBefore;
    }

    public boolean remove(K key) {
        if (!isValidKey(key))
            return false;

        boolean containedBefore = this.values[key.ordinal()];
        this.values[key.ordinal()] = false;

        --this.size;

        return containedBefore;
    }

    public void clear() {
        this.values = new boolean[this.values.length];
        this.size = 0;
    }

    @Override
    public String toString() {
        return "EnumerateSet{" + Arrays.toString(values) + '}';
    }

    private boolean isValidKey(K key) {
        return key.ordinal() >= 0 && key.ordinal() < this.values.length;
    }

    private void ensureCapacity(int capacity) {
        if (capacity > this.values.length)
            this.values = Arrays.copyOf(this.values, capacity);
    }

}

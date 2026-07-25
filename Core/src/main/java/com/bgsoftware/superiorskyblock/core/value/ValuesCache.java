package com.bgsoftware.superiorskyblock.core.value;

import java.util.Objects;
import java.util.function.IntFunction;

public class ValuesCache<T> {

    private static final int CACHE_SIZE = 32;

    private final int[] indexes = new int[CACHE_SIZE];
    private final Object[] cache = new Object[CACHE_SIZE];
    private int capacity = 0;

    private final IntFunction<T> creator;


    public ValuesCache(IntFunction<T> creator) {
        this.creator = creator;
    }

    public T fetch(int value) {
        for (int i = 0; i < this.capacity; ++i) {
            if (this.indexes[i] == value) {
                return Objects.requireNonNull((T) this.cache[i]);
            }
        }

        T cachedValue = this.creator.apply(value);
        if (cachedValue != null && this.capacity < CACHE_SIZE) {
            this.indexes[this.capacity] = value;
            this.cache[this.capacity] = cachedValue;

            ++this.capacity;
        }

        return cachedValue;
    }

}

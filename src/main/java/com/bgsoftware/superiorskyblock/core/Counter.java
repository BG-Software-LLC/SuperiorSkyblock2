package com.bgsoftware.superiorskyblock.core;

import java.util.concurrent.atomic.AtomicInteger;

public class Counter {

    private final AtomicInteger value;

    public Counter(int initialValue) {
        this.value = new AtomicInteger(initialValue);
    }

    public int inc(int delta) {
        return this.value.getAndAdd(delta);
    }

    public int set(int value) {
        return this.value.getAndSet(value);
    }

    public int get() {
        return this.value.get();
    }

}

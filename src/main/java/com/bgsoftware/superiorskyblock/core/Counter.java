package com.bgsoftware.superiorskyblock.core;

public class Counter {

    private int value;

    public Counter(int initialValue) {
        this.value = initialValue;
    }

    public void inc(int delta) {
        this.value += delta;
    }

    public int get() {
        return this.value;
    }

}

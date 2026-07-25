package com.bgsoftware.superiorskyblock.core;

public class Counter {

    private int value;

    public Counter(int initialValue) {
        this.value = initialValue;
    }

    public int inc(int delta) {
        int original = this.value;
        this.value += delta;
        return original;
    }

    public int set(int value) {
        int original = this.value;
        this.value = value;
        return original;
    }

    public int get() {
        return this.value;
    }

}

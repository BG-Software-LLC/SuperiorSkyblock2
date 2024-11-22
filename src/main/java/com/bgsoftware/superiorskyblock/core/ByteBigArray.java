package com.bgsoftware.superiorskyblock.core;

import java.util.Arrays;

public class ByteBigArray {

    private static final short DEFAULT_CAPACITY = 4096;
    private static final byte[][] EMPTY_BIG_ARRAY = {};

    private byte[][] backend;

    private final short capacity;

    private int backendIdx = 0;
    private short innerSize = 0;
    private int size = 0;

    private boolean readOnly = false;

    public ByteBigArray() {
        this(DEFAULT_CAPACITY);
    }

    public ByteBigArray(short capacity) {
        this.capacity = capacity;
        this.backend = EMPTY_BIG_ARRAY;
    }

    public int size() {
        return this.size;
    }

    public void add(byte val) {
        if (this.readOnly)
            throw new IllegalStateException("Cannot alter big arrays that are finalized");

        getNextInnerArray()[this.innerSize++] = val;
        ++this.size;
    }

    public byte get() {
        return getNextInnerArray()[this.innerSize];
    }

    public byte get(int pos) {
        int backendIdx = pos / this.capacity;
        int innerIdx = pos % this.capacity;
        return this.backend[backendIdx][innerIdx];
    }

    public ByteBigArray readOnly() {
        int backendIdx = this.size / this.capacity;
        int innerSize = this.size % this.capacity;

        this.backend = Arrays.copyOf(this.backend, backendIdx + 1);
        this.backend[this.backend.length - 1] = Arrays.copyOf(this.backend[this.backend.length - 1], innerSize);

        this.readOnly = true;
        return this;
    }

    private byte[] getNextInnerArray() {
        if (this.innerSize >= this.capacity) {
            ++this.backendIdx;
            this.innerSize = 0;
        }
        if (this.backendIdx >= this.backend.length) {
            this.backend = Arrays.copyOf(this.backend, this.backend.length + 1);
            this.backend[this.backendIdx] = new byte[this.capacity];
            this.innerSize = 0;
        }

        return this.backend[this.backendIdx];
    }

}

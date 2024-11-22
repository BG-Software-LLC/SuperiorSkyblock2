package com.bgsoftware.superiorskyblock.core;

import java.util.BitSet;

public class BigBitSet {

    private static final int DEFAULT_CAPACITY = 4096 * 64;

    private final BitSet[] backend;

    private int size;

    public BigBitSet(int nbits) {
        this.backend = new BitSet[(int) Math.ceil((double) nbits / DEFAULT_CAPACITY)];
        this.size = 0;
    }

    public int size() {
        return this.size;
    }

    public void set(int pos) {
        try {
            int backendIdx = pos / DEFAULT_CAPACITY;
            int bitSetIdx = pos % DEFAULT_CAPACITY;
            BitSet bitSet = this.backend[backendIdx];
            if (bitSet == null)
                bitSet = this.backend[backendIdx] = new BitSet(DEFAULT_CAPACITY);
            bitSet.set(bitSetIdx);
            ++this.size;
        } catch (IndexOutOfBoundsException error) {
            throw new IndexOutOfBoundsException("Out of bounds for pos " + pos);
        }
    }

    public int nextSetBit(int fromIndex) {
        int backendIdx = fromIndex / DEFAULT_CAPACITY;
        int bitSetIdx = fromIndex % DEFAULT_CAPACITY;

        int next = -1;

        BitSet bitSet = this.backend[backendIdx];
        if (bitSet != null) {
            next = bitSet.nextSetBit(bitSetIdx);
            if (next == -1 && ++backendIdx < this.backend.length) {
                bitSet = this.backend[backendIdx];
                if (bitSet != null)
                    next = bitSet.nextSetBit(0);
            }
        }

        return next == -1 ? -1 : backendIdx * DEFAULT_CAPACITY + next;
    }

}

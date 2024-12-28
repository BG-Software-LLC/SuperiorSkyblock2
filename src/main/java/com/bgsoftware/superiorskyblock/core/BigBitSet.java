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

        return nextSetBitInternal(backendIdx, bitSetIdx);
    }

    private int nextSetBitInternal(int backendIdx, int bitSetIdx) {
        if (backendIdx < 0 || backendIdx >= this.backend.length)
            return -1;

        BitSet bitSet = this.backend[backendIdx];
        if (bitSet != null) {
            int next = bitSet.nextSetBit(bitSetIdx);
            if (next != -1) {
                return backendIdx * DEFAULT_CAPACITY + next;
            }
        }

        return nextSetBitInternal(backendIdx + 1, 0);
    }

    public int cardinality() {
        int cardinality = 0;
        for (BitSet bitSet : backend) {
            if (bitSet != null)
                cardinality += bitSet.cardinality();
        }
        return cardinality;
    }

}

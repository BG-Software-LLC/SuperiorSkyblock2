package com.bgsoftware.superiorskyblock.missions.blocks;

import java.util.Arrays;
import java.util.BitSet;
import java.util.function.IntConsumer;

public class ChunkBitSet {

    private static final BitSet[] EMPTY_BIT_SET_ARRAY = new BitSet[0];

    private BitSet[] bitSets = EMPTY_BIT_SET_ARRAY;

    public ChunkBitSet() {

    }

    public void set(int index) {
        BitSet bitSet = getBitSetForBlock(index, true);
        if (bitSet == null)
            throw new IllegalStateException();
        int blockIdx = index & 0xFFF;
        bitSet.set(blockIdx);
    }

    public boolean clear(int index) {
        BitSet bitSet = getBitSetForBlock(index, false);
        if (bitSet == null)
            return false;
        int blockIdx = index & 0xFFF;
        boolean old = bitSet.get(blockIdx);
        bitSet.clear(blockIdx);
        return old;
    }

    public boolean get(int index) {
        BitSet bitSet = getBitSetForBlock(index, false);
        if (bitSet == null)
            return false;
        int blockIdx = index & 0xFFF;
        return bitSet.get(blockIdx);
    }

    public void forEach(IntConsumer consumer) {
        for (int i = 0; i < this.bitSets.length; ++i) {
            BitSet bitSet = this.bitSets[i];
            if (bitSet != null) {
                for (int j = bitSet.nextSetBit(0); j != -1; j = bitSet.nextSetBit(j + 1)) {
                    consumer.accept(((i << 4) << 8) | j);
                }
            }
        }
    }

    private void ensureCapacity(int capacity) {
        if (bitSets.length >= capacity)
            return;

        bitSets = Arrays.copyOf(bitSets, capacity);
    }

    private BitSet getBitSetForBlock(int block, boolean createNew) {
        int sectionIdx = (block >> 8) >> 4;

        if (bitSets.length <= sectionIdx) {
            if (!createNew)
                return null;

            ensureCapacity(sectionIdx + 1);
        }

        BitSet bitSet = bitSets[sectionIdx];

        if (bitSet == null) {
            if (!createNew)
                return null;

            bitSet = bitSets[sectionIdx] = new BitSet();
        }

        return bitSet;
    }

}

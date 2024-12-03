package com.bgsoftware.superiorskyblock.core;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class VarintArray {

    private static final byte[] VARINT_BUF = new byte[10];

    private final ByteBigArray backend;
    private int size;

    public VarintArray() {
        this.backend = new ByteBigArray();
        this.size = 0;
    }

    public VarintArray(ByteBigArray data) {
        Preconditions.checkState(data.size() == 0 || (data.get(data.size() - 1) & 0x80) == 0,
                "Last byte in data-stream cannot have its MSB on");
        this.backend = data;
        this.size = data.size();
    }

    public void add(long value) {
        byte[] varint = serializeVarint(value);
        for (byte b : varint)
            this.backend.add(b);
        this.size += varint.length;
    }

    public ByteBigArray toArray() {
        return this.backend.readOnly();
    }

    public Itr iterator() {
        return new Itr();
    }

    private static byte[] serializeVarint(long value) {
        int varintBytesCount = 0;
        while (value != 0) {
            byte nextByte = (byte) (value & 0x7f);
            value >>= 7;
            VARINT_BUF[varintBytesCount++] = value == 0 ? nextByte : (byte) (nextByte | 0x80);
        }
        return Arrays.copyOf(VARINT_BUF, varintBytesCount);
    }

    public class Itr {

        private int index = 0;

        public boolean hasNext() {
            return index < size;
        }

        public long next() {
            if (!hasNext())
                throw new NoSuchElementException();

            int i = 0;
            long value = 0;
            while (index < size) {
                byte b = backend.get(index++);
                value |= (long) (b & 0x7f) << i;
                i += 7;
                if ((b & 0x80) == 0)
                    break;
            }
            return value;
        }

    }

}

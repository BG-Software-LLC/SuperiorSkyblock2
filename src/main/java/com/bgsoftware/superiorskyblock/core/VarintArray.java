package com.bgsoftware.superiorskyblock.core;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class VarintArray {

    private static final byte[] VARINT_BUF = new byte[10];

    private byte[] backend;
    private int size;

    public VarintArray() {
        this.backend = new byte[32];
        this.size = 0;
    }

    public VarintArray(byte[] data) {
        Preconditions.checkState(data.length == 0 || (data[data.length - 1] & 0x80) == 0,
                "Last byte in data-stream cannot have its MSB on");
        this.backend = data;
        this.size = data.length;
    }

    public void add(long value) {
        byte[] varint = serializeVarint(value);
        if (this.size + varint.length >= this.backend.length) {
            this.backend = Arrays.copyOf(this.backend, this.backend.length * 2);
        }
        int oldSize = this.size;
        System.arraycopy(varint, 0, this.backend, oldSize, varint.length);
        this.size += varint.length;
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(this.backend, this.size);
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
                byte b = backend[index++];
                value |= (long) (b & 0x7f) << i;
                i += 7;
                if ((b & 0x80) == 0)
                    break;
            }
            return value;
        }

    }

}

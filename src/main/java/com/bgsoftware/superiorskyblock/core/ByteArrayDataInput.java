package com.bgsoftware.superiorskyblock.core;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.UUID;

public class ByteArrayDataInput {

    private final DataInput dataInput;

    public ByteArrayDataInput(byte[] bytes, int offset) {
        this(new DataInputStream(new ByteArrayInputStream(bytes)));
        if (offset > 0)
            runOnDataInput(() -> this.dataInput.skipBytes(offset));
    }

    public ByteArrayDataInput(DataInput dataInput) {
        this.dataInput = dataInput;
    }

    public String readString() {
        return new String(readBytes());
    }

    public boolean readBoolean() {
        return runOnDataInput(dataInput::readBoolean);
    }

    public int readInt() {
        return runOnDataInput(dataInput::readInt);
    }

    public long readLong() {
        return runOnDataInput(dataInput::readLong);
    }

    public double readDouble() {
        return runOnDataInput(dataInput::readDouble);
    }

    public float readFloat() {
        return runOnDataInput(dataInput::readFloat);
    }

    public byte readByte() {
        return runOnDataInput(dataInput::readByte);
    }

    public UUID readUUID() {
        long msb = readLong();
        long lsb = readLong();
        return new UUID(msb, lsb);
    }

    public byte[] readBytes() {
        byte[] val = new byte[readInt()];
        try {
            dataInput.readFully(val);
        } catch (IOException error) {
            throw new RuntimeException(error);
        }
        return val;
    }

    public BigDecimal readBigDecimal() {
        int scale = readInt();
        int precision = readInt();
        BigInteger bigInteger = new BigInteger(readBytes());
        return new BigDecimal(bigInteger, scale, new MathContext(precision));
    }

    private <R> R runOnDataInput(DataInputRunnableConsumer<R> runnable) {
        try {
            return runnable.run();
        } catch (IOException error) {
            throw new RuntimeException(error);
        }
    }

    interface DataInputRunnableConsumer<R> {

        R run() throws IOException;

    }

}

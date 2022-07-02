package com.bgsoftware.superiorskyblock.core;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

public class ByteArrayDataOutput {

    private final ByteArrayOutputStream byteArrayOutputStream;
    private final DataOutput dataOutput;

    public ByteArrayDataOutput() {
        this.byteArrayOutputStream = new ByteArrayOutputStream();
        this.dataOutput = new DataOutputStream(this.byteArrayOutputStream);
    }

    public void writeString(String val) {
        writeBytes(val.getBytes());
    }

    public void writeBoolean(boolean val) {
        runOnDataOutput(() -> dataOutput.writeBoolean(val));
    }

    public void writeInt(int val) {
        runOnDataOutput(() -> dataOutput.writeInt(val));
    }

    public void writeLong(long val) {
        runOnDataOutput(() -> dataOutput.writeLong(val));
    }

    public void writeDouble(double val) {
        runOnDataOutput(() -> dataOutput.writeDouble(val));
    }

    public void writeFloat(float val) {
        runOnDataOutput(() -> dataOutput.writeFloat(val));
    }

    public void writeByte(byte val) {
        runOnDataOutput(() -> dataOutput.writeByte(val));
    }

    public void writeUUID(UUID val) {
        writeLong(val.getMostSignificantBits());
        writeLong(val.getLeastSignificantBits());
    }

    public void writeBytes(byte[] val) {
        writeInt(val.length);
        runOnDataOutput(() -> dataOutput.write(val));
    }

    public void writeBigDecimal(BigDecimal val) {
        writeInt(val.scale());
        writeInt(val.precision());
        writeBytes(val.toBigInteger().toByteArray());
    }

    public byte[] toByteArray() {
        return byteArrayOutputStream.toByteArray();
    }

    private void runOnDataOutput(DataOutputRunnable runnable) {
        try {
            runnable.run();
        } catch (IOException error) {
            throw new RuntimeException(error);
        }
    }

    interface DataOutputRunnable {

        void run() throws IOException;

    }

}

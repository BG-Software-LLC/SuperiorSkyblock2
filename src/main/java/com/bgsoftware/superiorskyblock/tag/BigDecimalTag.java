package com.bgsoftware.superiorskyblock.tag;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public class BigDecimalTag extends Tag<BigDecimal> {

    private BigDecimalTag(BigDecimal value) {
        super(value);
    }

    @Override
    protected void writeData(DataOutputStream os) throws IOException {
        os.writeInt(value.scale());
        os.writeInt(value.precision());

        byte[] data = value.toBigIntegerExact().toByteArray();
        os.writeInt(data.length);
        os.write(data);
    }

    public static BigDecimalTag of(BigDecimal value) {
        return new BigDecimalTag(value);
    }

    public static BigDecimalTag fromStream(DataInputStream is) throws IOException {
        int scale = is.readInt();
        int precision = is.readInt();
        byte[] data = new byte[is.readInt()];
        is.readFully(data);

        return BigDecimalTag.of(new BigDecimal(new BigInteger(data), scale, new MathContext(precision)));
    }

}
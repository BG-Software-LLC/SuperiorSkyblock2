package com.bgsoftware.superiorskyblock.tag;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class UUIDTag extends Tag<UUID> {

    private UUIDTag(UUID value) {
        super(value);
    }

    @Override
    protected void writeData(DataOutputStream os) throws IOException {
        os.writeLong(value.getMostSignificantBits());
        os.writeLong(value.getLeastSignificantBits());
    }

    public static UUIDTag of(long mostSigBits, long leastSigBits) {
        return new UUIDTag(new UUID(mostSigBits, leastSigBits));
    }

    public static UUIDTag of(UUID value) {
        return new UUIDTag(value);
    }

    public static UUIDTag fromStream(DataInputStream is) throws IOException {
        return UUIDTag.of(is.readLong(), is.readLong());
    }

}
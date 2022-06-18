package com.bgsoftware.superiorskyblock.tag;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class UUIDTag extends Tag<UUID> {

    public UUIDTag(UUID value) {
        super(value, null);
    }

    public static UUIDTag fromStream(DataInputStream is) throws IOException {
        return new UUIDTag(new UUID(is.readLong(), is.readLong()));
    }

    @Override
    protected void writeData(DataOutputStream os) throws IOException {
        os.writeLong(value.getMostSignificantBits());
        os.writeLong(value.getLeastSignificantBits());
    }

}
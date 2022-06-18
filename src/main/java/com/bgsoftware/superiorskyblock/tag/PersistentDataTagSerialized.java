package com.bgsoftware.superiorskyblock.tag;

import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataType;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataTypeContext;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PersistentDataTagSerialized extends Tag<byte[]> {

    public PersistentDataTagSerialized(byte[] value) {
        super(value, null);
    }

    public <E> PersistentDataTag<E> getPersistentDataTag(PersistentDataType<E> type) {
        PersistentDataTypeContext<E> serializer = type.getContext();
        if (serializer == null)
            throw new IllegalArgumentException("Cannot find a valid serializer for " + type);
        return new PersistentDataTag<>(serializer.deserialize(value), serializer);
    }

    public static PersistentDataTagSerialized fromStream(DataInputStream is) throws IOException {
        int length = is.readInt();
        byte[] bytes = new byte[length];
        is.readFully(bytes);
        return new PersistentDataTagSerialized(bytes);
    }

    @Override
    protected void writeData(DataOutputStream os) throws IOException {
        os.writeInt(value.length);
        os.write(value);
    }

}
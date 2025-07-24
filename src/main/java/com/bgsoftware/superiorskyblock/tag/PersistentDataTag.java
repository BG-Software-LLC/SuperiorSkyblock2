package com.bgsoftware.superiorskyblock.tag;

import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataTypeContext;

import java.io.DataOutputStream;
import java.io.IOException;

public class PersistentDataTag<E> extends Tag<E> {

    private final PersistentDataTypeContext<E> serializer;

    public static <E> PersistentDataTag<E> of(E value, PersistentDataTypeContext<E> serializer) {
        return new PersistentDataTag<>(value, serializer);
    }

    private PersistentDataTag(E value, PersistentDataTypeContext<E> serializer) {
        super(value, null);
        this.serializer = serializer;
    }

    @Override
    protected void writeData(DataOutputStream os) throws IOException {
        byte[] data = serializer.serialize(value);
        os.writeInt(data.length);
        os.write(data);
    }

}
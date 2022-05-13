package com.bgsoftware.superiorskyblock.tag;

import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataTypeContext;

import java.io.DataOutputStream;
import java.io.IOException;

public final class PersistentDataTag<E> extends Tag<E> {

    private final PersistentDataTypeContext<E> serializer;

    public PersistentDataTag(E value, PersistentDataTypeContext<E> serializer) {
        super(value, null);
        this.serializer = serializer;
    }

    @Override
    protected void writeData(DataOutputStream os) throws IOException {
        os.write(serializer.serialize(value));
    }

}
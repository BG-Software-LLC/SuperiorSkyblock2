package com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping;

public abstract class MappedObject<T> {

    protected final T handle;

    protected MappedObject(T handle) {
        this.handle = handle;
    }

    public T getHandle() {
        return handle;
    }

}

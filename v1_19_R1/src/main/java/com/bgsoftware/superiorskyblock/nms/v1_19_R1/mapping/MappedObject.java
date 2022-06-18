package com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping;

public abstract class MappedObject<T> {

    protected T handle;

    protected MappedObject(T handle) {
        this.handle = handle;
    }

    public T getHandle() {
        return handle;
    }

    public void setHandle(T handle) {
        this.handle = handle;
    }

}

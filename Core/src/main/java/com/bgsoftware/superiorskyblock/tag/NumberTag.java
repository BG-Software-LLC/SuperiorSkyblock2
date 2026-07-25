package com.bgsoftware.superiorskyblock.tag;

public abstract class NumberTag<E extends Number> extends Tag<E> {

    protected NumberTag(E value) {
        super(value);
    }

}
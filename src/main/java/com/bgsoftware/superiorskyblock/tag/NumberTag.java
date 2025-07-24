package com.bgsoftware.superiorskyblock.tag;

public abstract class NumberTag<E extends Number> extends Tag<E> {

    protected NumberTag(E value, Class<?> clazz, Class<?>... parameterTypes) {
        super(value, clazz, parameterTypes);
    }

}
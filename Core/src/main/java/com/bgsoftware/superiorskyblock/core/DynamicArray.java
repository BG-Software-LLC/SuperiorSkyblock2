package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.common.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class DynamicArray<E> {

    private Object[] elements;

    public DynamicArray() {
        this(0);
    }

    public DynamicArray(int capacity) {
        this.elements = new Object[capacity];
    }

    @Nullable
    public E get(int index) {
        rangeCheck(index);
        return (E) elements[index];
    }

    @Nullable
    public E set(int index, E element) {
        ensureCapacity(index + 1);
        E current = (E) elements[index];
        elements[index] = element;
        return current;
    }

    public int length() {
        return this.elements.length;
    }

    public List<E> toList() {
        return Arrays.asList((E[]) this.elements);
    }

    private void rangeCheck(int index) {
        if (index < 0 || index > elements.length)
            throw new ArrayIndexOutOfBoundsException("Index: " + index + ", Size: " + elements.length);
    }

    private void ensureCapacity(int capacity) {
        if (capacity > elements.length) {
            this.elements = Arrays.copyOf(this.elements, capacity);
        }
    }

}

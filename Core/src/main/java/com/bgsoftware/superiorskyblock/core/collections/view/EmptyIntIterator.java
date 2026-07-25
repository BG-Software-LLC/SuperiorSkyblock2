package com.bgsoftware.superiorskyblock.core.collections.view;

import java.util.NoSuchElementException;

public class EmptyIntIterator implements IntIterator {

    public static final EmptyIntIterator INSTANCE = new EmptyIntIterator();

    private EmptyIntIterator() {

    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public int next() {
        throw new NoSuchElementException();
    }

    @Override
    public void remove() {
        throw new NoSuchElementException();
    }

}

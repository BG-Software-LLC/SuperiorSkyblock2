package com.bgsoftware.superiorskyblock.core.collections.view;

public interface IntSetView {

    int size();

    boolean add(int value);

    boolean remove(int value);

    boolean contains(int value);

    IntIterator iterator();

}

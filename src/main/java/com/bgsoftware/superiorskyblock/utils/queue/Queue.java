package com.bgsoftware.superiorskyblock.utils.queue;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

public final class Queue<E> {

    private List<E> list = new ArrayList<>();

    public void push(E element){
        list.add(element);
    }

    public E pop(){
        Preconditions.checkState(size() > 0, "Cannot pop an element from an empty queue!");
        E popped = this.list.get(0);
        list.remove(0);
        return popped;
    }

    public int size(){
        return list.size();
    }

}

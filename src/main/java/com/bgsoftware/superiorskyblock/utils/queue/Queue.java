package com.bgsoftware.superiorskyblock.utils.queue;

import java.util.ArrayList;
import java.util.List;

public final class Queue<E> {

    private List<E> list = new ArrayList<>();

    public void push(E element){
        list.add(element);
    }

    public E pop(){
        if(size() <= 0)
            throw new NullPointerException("Cannot pop an element from an empty queue!");
        E popped = this.list.get(0);
        list.remove(0);
        return popped;
    }

    public int size(){
        return list.size();
    }

}

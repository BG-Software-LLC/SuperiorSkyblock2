package com.bgsoftware.superiorskyblock.utils.queue;

import java.util.Comparator;
import java.util.PriorityQueue;

public final class UniquePriorityQueue<E> extends PriorityQueue<E> {

    public UniquePriorityQueue(){

    }

    public UniquePriorityQueue(Comparator<E> comparator){
        super(comparator);
    }

    @Override
    public boolean offer(E e) {
        return !super.contains(e) && super.offer(e);
    }

}

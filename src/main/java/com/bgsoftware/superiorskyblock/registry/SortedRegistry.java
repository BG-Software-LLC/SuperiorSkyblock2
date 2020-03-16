package com.bgsoftware.superiorskyblock.registry;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.function.Predicate;

public abstract class SortedRegistry<K, V, Z extends Comparator<V>> extends Registry<K, V> {

    private final Registry<Z, TreeSet<V>> sortedValues = new Registry<Z, TreeSet<V>>() {};

    protected SortedRegistry(){
        super();
    }

    public synchronized V get(int index, Z sortingType){
        ensureType(sortingType);
        return index >= sortedValues.get(sortingType).size() ? null : Iterables.get(sortedValues.get(sortingType), index);
    }

    public synchronized int indexOf(V value, Z sortingType){
        ensureType(sortingType);
        return Iterables.indexOf(sortedValues.get(sortingType), value::equals);
    }

    @Override
    public synchronized void add(K key, V value) {
        super.add(key, value);
        for(TreeSet<V> sortedTree : sortedValues.values())
            sortedTree.add(value);
    }

    @Override
    public synchronized V remove(K key) {
        V value = super.remove(key);
        if(value != null){
            for (TreeSet<V> sortedTree : sortedValues.values())
                sortedTree.remove(value);
        }
        return value;
    }

    public synchronized Iterator<V> iterator(Z sortingType){
        ensureType(sortingType);
        return Iterables.unmodifiableIterable(sortedValues.get(sortingType)).iterator();
    }

    protected synchronized void sort(Z sortingType, Predicate<V> predicate){
        ensureType(sortingType);
        TreeSet<V> sortedTree = sortedValues.get(sortingType);
        Iterator<V> clonedTree = super.iterator();
        sortedTree.clear();
        while(clonedTree.hasNext()) {
            V value = clonedTree.next();
            if(predicate == null || predicate.test(value))
                sortedTree.add(value);
        }
    }

    protected synchronized void registerSortingType(Z sortingType, boolean sort, Predicate<V> predicate){
        Preconditions.checkArgument(!sortedValues.containsKey(sortingType), "You cannot register an existing sorting type to the database.");

        sortedValues.add(sortingType, Sets.newTreeSet(sortingType));

        if(sort)
            sort(sortingType, predicate);
    }

    private void ensureType(Z sortingType){
        Preconditions.checkState(sortedValues.containsKey(sortingType), "The sorting-type " + sortingType + " doesn't exist in the database. Please contact author!");
    }

}

package com.bgsoftware.superiorskyblock.utils.registry;

import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.bukkit.Bukkit;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Predicate;

public abstract class SortedRegistry<K, V, Z extends Comparator<V>> extends Registry<K, V> {

    private final Registry<Z, Set<V>> sortedValues = createRegistry();

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
    public synchronized V add(K key, V value) {
        for(Set<V> sortedTree : sortedValues.values())
            sortedTree.add(value);
        return super.add(key, value);
    }

    @Override
    public synchronized V remove(K key) {
        V value = super.remove(key);
        if(value != null){
            for (Set<V> sortedTree : sortedValues.values())
                sortedTree.remove(value);
        }
        return value;
    }

    public synchronized Iterator<V> iterator(Z sortingType){
        ensureType(sortingType);
        return Iterables.unmodifiableIterable(sortedValues.get(sortingType)).iterator();
    }

    protected synchronized void sort(Z sortingType, Predicate<V> predicate){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> sort(sortingType, predicate));
            return;
        }

        ensureType(sortingType);
        Set<V> sortedTree = sortedValues.get(sortingType);
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

        sortedValues.add(sortingType, new ConcurrentSkipListSet<>(sortingType));

        if(sort)
            sort(sortingType, predicate);
    }

    private void ensureType(Z sortingType){
        Preconditions.checkState(sortedValues.containsKey(sortingType), "The sorting-type " + sortingType + " doesn't exist in the database. Please contact author!");
    }

}

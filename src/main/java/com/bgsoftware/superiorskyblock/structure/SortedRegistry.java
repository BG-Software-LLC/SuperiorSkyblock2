package com.bgsoftware.superiorskyblock.structure;

import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Predicate;

public final class SortedRegistry<K, V, Z extends Comparator<V>> {

    private final Map<Z, Set<V>> sortedValues = new ConcurrentHashMap<>();
    private final Map<K, V> innerMap = new ConcurrentHashMap<>();

    public V get(K key) {
        return innerMap.get(key);
    }

    public V get(int index, Z sortingType) {
        ensureType(sortingType);
        return index >= sortedValues.get(sortingType).size() ? null : Iterables.get(sortedValues.get(sortingType), index);
    }

    public int indexOf(V value, Z sortingType) {
        ensureType(sortingType);
        return Iterables.indexOf(sortedValues.get(sortingType), value::equals);
    }

    public V put(K key, V value) {
        for (Set<V> sortedTree : sortedValues.values())
            sortedTree.add(value);
        return innerMap.put(key, value);
    }

    public V remove(K key) {
        V value = innerMap.remove(key);
        if (value != null) {
            for (Set<V> sortedTree : sortedValues.values())
                sortedTree.remove(value);
        }
        return value;
    }

    public List<V> getIslands(Z sortingType) {
        return Collections.unmodifiableList(new ArrayList<>(this.sortedValues.get(sortingType)));
    }

    public void sort(Z sortingType, Predicate<V> predicate, Runnable onFinish) {
        if (Bukkit.isPrimaryThread()) {
            Executor.async(() -> sort(sortingType, predicate, onFinish));
            return;
        }

        ensureType(sortingType);

        Set<V> newSortedTree = new ConcurrentSkipListSet<>(sortingType);

        for (V element : innerMap.values()) {
            if (predicate == null || predicate.test(element))
                newSortedTree.add(element);
        }

        sortedValues.put(sortingType, newSortedTree);

        if (onFinish != null)
            onFinish.run();
    }

    public void registerSortingType(Z sortingType, boolean sort, Predicate<V> predicate) {
        Preconditions.checkArgument(!sortedValues.containsKey(sortingType), "You cannot register an existing sorting type to the database.");

        Set<V> sortedIslands = new ConcurrentSkipListSet<>(sortingType);
        sortedIslands.addAll(innerMap.values());
        sortedValues.put(sortingType, sortedIslands);

        if (sort)
            sort(sortingType, predicate, null);
    }

    private void ensureType(Z sortingType) {
        Preconditions.checkState(sortedValues.containsKey(sortingType), "The sorting-type " + sortingType + " doesn't exist in the database. Please contact author!");
    }

}

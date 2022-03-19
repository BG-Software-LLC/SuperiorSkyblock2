package com.bgsoftware.superiorskyblock.structure;

import com.bgsoftware.superiorskyblock.threads.Executor;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.bukkit.Bukkit;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class SortedRegistry<K, V, Z extends Comparator<V>> {

    private final Map<Z, Set<V>> sortedValues = new ConcurrentHashMap<>();
    private final Map<K, V> innerMap = new ConcurrentHashMap<>();
    @Nullable
    private final Predicate<V> valuesPredicate;

    public SortedRegistry(@Nullable Predicate<V> valuesPredicate) {
        this.valuesPredicate = valuesPredicate;
    }

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
        if (canAddValue(value)) {
            for (Set<V> sortedTree : sortedValues.values())
                sortedTree.add(value);
        }
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

    public void sort(Z sortingType, boolean forceSort, Runnable onFinish) {
        if (!forceSort && innerMap.size() <= 1) {
            if (onFinish != null)
                onFinish.run();
            return;
        }

        if (Bukkit.isPrimaryThread()) {
            Executor.async(() -> sort(sortingType, forceSort, onFinish));
            return;
        }

        ensureType(sortingType);

        Set<V> newSortedTree = new ConcurrentSkipListSet<>(sortingType);

        for (V element : innerMap.values()) {
            if (canAddValue(element))
                newSortedTree.add(element);
        }

        sortedValues.put(sortingType, newSortedTree);

        if (onFinish != null)
            onFinish.run();
    }

    public void registerSortingType(Z sortingType, boolean sort) {
        Preconditions.checkArgument(!sortedValues.containsKey(sortingType), "You cannot register an existing sorting type to the database.");

        Set<V> sortedIslands = new ConcurrentSkipListSet<>(sortingType);
        sortedIslands.addAll(innerMap.values().stream().filter(this::canAddValue).collect(Collectors.toList()));
        sortedValues.put(sortingType, sortedIslands);

        if (sort)
            sort(sortingType, false, null);
    }

    private void ensureType(Z sortingType) {
        Preconditions.checkState(sortedValues.containsKey(sortingType), "The sorting-type " + sortingType + " doesn't exist in the database. Please contact author!");
    }

    private boolean canAddValue(V value) {
        return valuesPredicate == null || valuesPredicate.test(value);
    }

}

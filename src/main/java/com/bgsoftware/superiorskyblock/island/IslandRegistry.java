package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public final class IslandRegistry implements Iterable<Island> {

    private Map<UUID, Island> islands = Maps.newHashMap();
    private Map<SortingType, TreeSet<UUID>> sortedTrees = new HashMap<>();

    public IslandRegistry(){
        for(SortingType sortingAlgorithm : SortingType.values())
            sortedTrees.put(sortingAlgorithm, Sets.newTreeSet(sortingAlgorithm.getComparator()));
    }

    public Island get(UUID uuid){
        return islands.get(uuid);
    }

    public Island get(int index, SortingType sortingType){
        ensureType(sortingType);
        return get(Iterables.get(sortedTrees.get(sortingType), index));
    }

    public int indexOf(Island island, SortingType sortingType){
        ensureType(sortingType);
        return Iterables.indexOf(sortedTrees.get(sortingType), uuid -> island.getOwner().getUniqueId().equals(uuid));
    }

    public synchronized void add(UUID uuid, Island island){
        islands.put(uuid, island);
        for(TreeSet<UUID> sortedTree : sortedTrees.values())
            sortedTree.add(uuid);
    }

    public synchronized void remove(UUID uuid){
        for(TreeSet<UUID> sortedTree : sortedTrees.values())
            sortedTree.remove(uuid);
        islands.remove(uuid);
    }

    public int size(){
        return islands.size();
    }

    @Override
    public Iterator<Island> iterator() {
        return islands.values().iterator();
    }

    public Iterator<UUID> iterator(SortingType sortingType){
        ensureType(sortingType);
        return Iterables.unmodifiableIterable(sortedTrees.get(sortingType)).iterator();
    }

    public synchronized void sort(SortingType sortingType){
        ensureType(sortingType);
        TreeSet<UUID> sortedTree = sortedTrees.get(sortingType);
        Iterator<UUID> worthIterator = Sets.newTreeSet(sortedTree).iterator();
        sortedTree.clear();
        while(worthIterator.hasNext())
            sortedTree.add(worthIterator.next());
    }

    public void transferIsland(UUID oldOwner, UUID newOwner){
        Island island = get(oldOwner);
        remove(oldOwner);
        add(newOwner, island);
    }

    private void ensureType(SortingType sortingType){
        if(!sortedTrees.containsKey(sortingType))
            throw new IllegalStateException("The sorting-type " + sortingType + " doesn't exist in the database. Please contact author!");
    }

}

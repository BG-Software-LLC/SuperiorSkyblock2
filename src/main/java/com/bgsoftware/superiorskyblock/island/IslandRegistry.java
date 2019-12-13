package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.menu.MenuTopIslands;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;

public final class IslandRegistry implements Iterable<Island> {

    private Map<UUID, Island> islands = Maps.newHashMap();
    private Map<IslandPosition, Island> islandsByPositions = Maps.newHashMap();
    private Map<SortingType, TreeSet<Island>> sortedTrees = new HashMap<>();

    public IslandRegistry(){
        for(SortingType sortingAlgorithm : SortingType.values())
            sortedTrees.put(sortingAlgorithm, Sets.newTreeSet(sortingAlgorithm.getComparator()));
    }

    public synchronized Island get(UUID uuid){
        return islands.get(uuid);
    }

    public synchronized Island get(int index, SortingType sortingType){
        ensureType(sortingType);
        return index >= sortedTrees.get(sortingType).size() ? null : Iterables.get(sortedTrees.get(sortingType), index);
    }

    public synchronized Island get(Location location){
        Island island = islandsByPositions.get(IslandPosition.of(location));
        return island == null || !island.isInside(location) ? null : island;
    }

    public synchronized int indexOf(Island island, SortingType sortingType){
        ensureType(sortingType);
        return Iterables.indexOf(sortedTrees.get(sortingType), island::equals);
    }

    public synchronized void add(UUID uuid, Island island){
        islands.put(uuid, island);
        islandsByPositions.put(IslandPosition.of(island), island);
        for(TreeSet<Island> sortedTree : sortedTrees.values())
            sortedTree.add(island);
    }

    public synchronized void remove(UUID uuid){
        Island island = get(uuid);
        if(island != null) {
            islandsByPositions.remove(IslandPosition.of(island), island);
            for (TreeSet<Island> sortedTree : sortedTrees.values())
                sortedTree.remove(island);
        }
        islands.remove(uuid);
    }

    public int size(){
        return islands.size();
    }

    @Override
    public synchronized Iterator<Island> iterator() {
        return islands.values().iterator();
    }

    public synchronized Iterator<Island> iterator(SortingType sortingType){
        ensureType(sortingType);
        return Iterables.unmodifiableIterable(sortedTrees.get(sortingType)).iterator();
    }

    public synchronized void sort(SortingType sortingType){
        ensureType(sortingType);
        TreeSet<Island> sortedTree = sortedTrees.get(sortingType);
        Iterator<Island> clonedTree = islands.values().iterator();
        sortedTree.clear();
        while(clonedTree.hasNext()) {
            Island island = clonedTree.next();
            if(!island.isIgnored())
                sortedTree.add(island);
        }
        Executor.sync(MenuTopIslands::refreshMenus);
    }

    public synchronized void transferIsland(UUID oldOwner, UUID newOwner){
        Island island = get(oldOwner);
        remove(oldOwner);
        add(newOwner, island);
    }

    private void ensureType(SortingType sortingType){
        if(!sortedTrees.containsKey(sortingType))
            throw new IllegalStateException("The sorting-type " + sortingType + " doesn't exist in the database. Please contact author!");
    }

}

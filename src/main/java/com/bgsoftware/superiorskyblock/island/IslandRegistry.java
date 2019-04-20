package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class IslandRegistry implements Iterable<Island> {

    private Map<UUID, Island> islands = Maps.newHashMap();
    private List<UUID> ownershipList = Lists.newArrayList();

    public Island get(UUID uuid){
        return islands.get(uuid);
    }

    public Island get(int index){
        return islands.get(ownershipList.get(index));
    }

    public void add(UUID uuid, Island island){
        islands.put(uuid, island);
        ownershipList.remove(uuid);
        ownershipList.add(uuid);
        sort();
    }

    public void remove(UUID uuid){
        islands.remove(uuid);
        ownershipList.remove(uuid);
        sort();
    }

    public int size(){
        return ownershipList.size();
    }

    @Override
    public Iterator<Island> iterator() {
        return islands.values().iterator();
    }

    public Iterator<UUID> uuidIterator(){
        return ownershipList.iterator();
    }

    public void sort(){
        //noinspection SuspiciousMethodCalls
        ownershipList.sort(Comparator.comparing(o -> islands.get(o)).reversed());
    }

    public void transfer(UUID old, UUID now) {
        Island island = islands.get(old);

        islands.remove(old);
        ownershipList.remove(old);

        islands.put(now, island);
        ownershipList.add(now);
    }

}

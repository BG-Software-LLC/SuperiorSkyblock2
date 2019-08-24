package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public final class IslandRegistry implements Iterable<Island> {

    private Map<UUID, Island> islands = Maps.newHashMap();
    private List<UUID> ownershipList = Lists.newArrayList();

    public Island get(UUID uuid){
        return islands.get(uuid);
    }

    public Island get(int index){
        return islands.get(ownershipList.get(index));
    }

    public int indexOf(Island island){
        return ownershipList.indexOf(island.getOwner().getUniqueId());
    }

    public synchronized void add(UUID uuid, Island island){
        islands.put(uuid, island);
        ownershipList.remove(uuid);
        ownershipList.add(uuid);
        sort();
    }

    public synchronized void remove(UUID uuid){
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

    public synchronized void sort(){
        //noinspection SuspiciousMethodCalls
        ownershipList.sort(Comparator.comparing(o -> islands.get(o)).reversed());
    }

    public void transferIsland(UUID oldOwner, UUID newOwner){
        Island island = islands.get(oldOwner);

        //Remove the old owner from list
        ownershipList.remove(oldOwner);

        //Add the new owner to list
        ownershipList.add(newOwner);

        //Replace owners
        islands.remove(oldOwner);
        islands.put(newOwner, island);

        sort();
    }

}

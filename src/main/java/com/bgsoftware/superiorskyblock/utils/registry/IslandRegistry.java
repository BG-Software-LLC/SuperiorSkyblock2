package com.bgsoftware.superiorskyblock.utils.registry;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.island.IslandPosition;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class IslandRegistry implements Iterable<Island> {

    private static final Predicate<Island> ISLANDS_PREDICATE = island -> !island.isIgnored();

    private final SortedRegistry<UUID, Island, SortingType> sortedIslands = new SortedRegistry<>();
    private final Map<IslandPosition, Island> islandsByPositions = new ConcurrentHashMap<>();
    private final Map<UUID, Island> islandsByUUID = new ConcurrentHashMap<>();
    private final SuperiorSkyblockPlugin plugin;

    public IslandRegistry(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
        SortingType.values().forEach(sortingType -> sortedIslands.registerSortingType(sortingType, false, ISLANDS_PREDICATE));
    }

    public Island get(UUID ownerUUID){
        return sortedIslands.get(ownerUUID);
    }

    public Island get(Location location){
        Island island = islandsByPositions.get(IslandPosition.of(location));
        return island == null || !island.isInside(location) ? null : island;
    }

    public Island get(int index, SortingType sortingType){
        return sortedIslands.get(index, sortingType);
    }

    public Island getByUUID(UUID uuid){
        return islandsByUUID.get(uuid);
    }

    public Island add(UUID uuid, Island island){
        Location islandLocation = island.getCenter(plugin.getSettings().defaultWorldEnvironment);
        islandsByPositions.put(IslandPosition.of(islandLocation), island);

        if(plugin.getProviders().hasCustomWorldsSupport()){
            runWithCustomWorld(islandLocation, island, World.Environment.NORMAL,
                    location -> islandsByPositions.put(IslandPosition.of(location), island));
            runWithCustomWorld(islandLocation, island, World.Environment.NETHER,
                    location -> islandsByPositions.put(IslandPosition.of(location), island));
            runWithCustomWorld(islandLocation, island, World.Environment.THE_END,
                    location -> islandsByPositions.put(IslandPosition.of(location), island));
        }

        islandsByUUID.put(island.getUniqueId(), island);

        return sortedIslands.put(uuid, island);
    }

    public Island remove(UUID uuid){
        Island island = sortedIslands.remove(uuid);
        if(island != null) {
            Location islandLocation = island.getCenter(plugin.getSettings().defaultWorldEnvironment);
            islandsByPositions.remove(IslandPosition.of(islandLocation));

            if(plugin.getProviders().hasCustomWorldsSupport()){
                runWithCustomWorld(islandLocation, island, World.Environment.NORMAL,
                        location -> islandsByPositions.remove(IslandPosition.of(location)));
                runWithCustomWorld(islandLocation, island, World.Environment.NETHER,
                        location -> islandsByPositions.remove(IslandPosition.of(location)));
                runWithCustomWorld(islandLocation, island, World.Environment.THE_END,
                        location -> islandsByPositions.remove(IslandPosition.of(location)));
            }

            islandsByUUID.remove(island.getUniqueId());
        }
        return island;
    }

    public void sort(SortingType sortingType, Runnable onFinish) {
        sortedIslands.sort(sortingType, ISLANDS_PREDICATE, onFinish);
    }

    public void registerSortingType(SortingType sortingType, boolean sort) {
        sortedIslands.registerSortingType(sortingType, sort, ISLANDS_PREDICATE);
    }

    public void transferIsland(UUID oldOwner, UUID newOwner){
        Island island = sortedIslands.get(oldOwner);
        sortedIslands.remove(oldOwner);
        sortedIslands.put(newOwner, island);
    }

    public int size(){
        return islandsByUUID.size();
    }

    public int indexOf(Island island, SortingType sortingType){
        return sortedIslands.indexOf(island, sortingType);
    }

    public Iterator<Island> iterator(SortingType sortingType){
        return sortedIslands.iterator(sortingType);
    }

    @Override
    public Iterator<Island> iterator(){
        return islandsByUUID.values().iterator();
    }

    public Collection<Island> values(){
        return islandsByUUID.values();
    }

    private void runWithCustomWorld(Location islandLocation, Island island, World.Environment environment, Consumer<Location> onSuccess){
        try{
            Location location = island.getCenter(environment);
            if(!location.getWorld().equals(islandLocation.getWorld()))
                onSuccess.accept(location);
        }catch (Exception ignored){}
    }

}

package com.bgsoftware.superiorskyblock.registry;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.island.IslandPosition;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.registry.SortedRegistry;
import org.bukkit.Location;

import java.util.UUID;
import java.util.function.Predicate;

public final class IslandRegistry extends SortedRegistry<UUID, Island, SortingType> {

    private static final Predicate<Island> ISLANDS_PREDICATE = island -> !island.isIgnored();

    private final Registry<IslandPosition, Island> islandsByPositions = createRegistry();

    public IslandRegistry(){
        SortingType.values().forEach(sortingType -> registerSortingType(sortingType, false, ISLANDS_PREDICATE));
    }

    public Island get(Location location){
        Island island = islandsByPositions.get(IslandPosition.of(location));
        return island == null || !island.isInside(location) ? null : island;
    }

    public Island add(UUID uuid, Island island){
        islandsByPositions.add(IslandPosition.of(island), island);
        return super.add(uuid, island);
    }

    @Override
    public Island remove(UUID uuid){
        Island island = super.remove(uuid);
        if(island != null)
            islandsByPositions.remove(IslandPosition.of(island));
        return island;
    }

    public void sort(SortingType sortingType, Runnable onFinish) {
        super.sort(sortingType, ISLANDS_PREDICATE, onFinish);
    }

    public void registerSortingType(SortingType sortingType, boolean sort) {
        super.registerSortingType(sortingType, sort, ISLANDS_PREDICATE);
    }

    public void transferIsland(UUID oldOwner, UUID newOwner){
        Island island = get(oldOwner);
        remove(oldOwner);
        add(newOwner, island);
    }

}

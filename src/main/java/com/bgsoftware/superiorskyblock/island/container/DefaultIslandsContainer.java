package com.bgsoftware.superiorskyblock.island.container;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.island.IslandPosition;
import com.bgsoftware.superiorskyblock.structure.SortedRegistry;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class DefaultIslandsContainer implements IslandsContainer {

    private static final Predicate<Island> ISLANDS_PREDICATE = island -> !island.isIgnored();

    private final SortedRegistry<UUID, Island, SortingType> sortedIslands = new SortedRegistry<>();
    private final Map<IslandPosition, Island> islandsByPositions = new ConcurrentHashMap<>();
    private final Map<UUID, Island> islandsByUUID = new ConcurrentHashMap<>();

    private final SuperiorSkyblockPlugin plugin;

    public DefaultIslandsContainer(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
        SortingType.values().forEach(sortingType -> addSortingType(sortingType, false));
    }

    @Override
    public void addIsland(Island island) {
        Location islandLocation = island.getCenter(plugin.getSettings().getWorlds().getDefaultWorld());
        this.islandsByPositions.put(IslandPosition.of(islandLocation), island);

        if(plugin.getProviders().hasCustomWorldsSupport()){
            runWithCustomWorld(islandLocation, island, World.Environment.NORMAL,
                    location -> this.islandsByPositions.put(IslandPosition.of(location), island));
            runWithCustomWorld(islandLocation, island, World.Environment.NETHER,
                    location -> this.islandsByPositions.put(IslandPosition.of(location), island));
            runWithCustomWorld(islandLocation, island, World.Environment.THE_END,
                    location -> this.islandsByPositions.put(IslandPosition.of(location), island));
        }

        this.islandsByUUID.put(island.getUniqueId(), island);
        this.sortedIslands.put(island.getOwner().getUniqueId(), island);
    }

    @Override
    public void removeIsland(Island island) {
        Location islandLocation = island.getCenter(plugin.getSettings().getWorlds().getDefaultWorld());

        sortedIslands.remove(island.getOwner().getUniqueId());
        islandsByUUID.remove(island.getUniqueId());
        islandsByPositions.remove(IslandPosition.of(islandLocation));

        if(plugin.getProviders().hasCustomWorldsSupport()){
            runWithCustomWorld(islandLocation, island, World.Environment.NORMAL,
                    location -> islandsByPositions.remove(IslandPosition.of(location)));
            runWithCustomWorld(islandLocation, island, World.Environment.NETHER,
                    location -> islandsByPositions.remove(IslandPosition.of(location)));
            runWithCustomWorld(islandLocation, island, World.Environment.THE_END,
                    location -> islandsByPositions.remove(IslandPosition.of(location)));
        }
    }

    @Nullable
    @Override
    public Island getIslandByUUID(UUID uuid) {
        return this.islandsByUUID.get(uuid);
    }

    @Nullable
    @Override
    public Island getIslandByOwner(UUID uuid) {
        return this.sortedIslands.get(uuid);
    }

    @Nullable
    @Override
    public Island getIslandAtPosition(int position, SortingType sortingType) {
        return position < 0 || position > getIslandsAmount() ? null : this.sortedIslands.get(position, sortingType);
    }

    @Override
    public int getIslandPosition(Island island, SortingType sortingType) {
        return this.sortedIslands.indexOf(island, sortingType);
    }

    @Override
    public int getIslandsAmount() {
        return this.islandsByUUID.size();
    }

    @Nullable
    @Override
    public Island getIslandAt(Location location) {
        Island island = this.islandsByPositions.get(IslandPosition.of(location));
        return island == null || !island.isInside(location) ? null : island;
    }

    @Override
    public void transferIsland(UUID oldOwner, UUID newOwner){
        Island island = sortedIslands.get(oldOwner);
        sortedIslands.remove(oldOwner);
        sortedIslands.put(newOwner, island);
    }

    @Override
    public void sortIslands(SortingType sortingType, Runnable onFinish) {
        this.sortedIslands.sort(sortingType, ISLANDS_PREDICATE, onFinish);
    }

    @Override
    public List<Island> getSortedIslands(SortingType sortingType) {
        return this.sortedIslands.getIslands(sortingType);
    }

    @Override
    public List<Island> getIslandsUnsorted() {
        return Collections.unmodifiableList(new ArrayList<>(this.islandsByUUID.values()));
    }

    @Override
    public void addSortingType(SortingType sortingType, boolean sort) {
        this.sortedIslands.registerSortingType(sortingType, sort, ISLANDS_PREDICATE);
    }

    private void runWithCustomWorld(Location islandLocation, Island island, World.Environment environment, Consumer<Location> onSuccess){
        try{
            Location location = island.getCenter(environment);
            if(!location.getWorld().equals(islandLocation.getWorld()))
                onSuccess.accept(location);
        }catch (Exception ignored){}
    }

}

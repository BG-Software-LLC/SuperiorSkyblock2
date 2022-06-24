package com.bgsoftware.superiorskyblock.island.container;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.island.container.IslandsContainer;
import com.bgsoftware.superiorskyblock.core.IslandPosition;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class DefaultIslandsContainer implements IslandsContainer {

    private final Map<IslandPosition, Island> islandsByPositions = new ConcurrentHashMap<>();
    private final Map<UUID, Island> islandsByUUID = new ConcurrentHashMap<>();

    private final Map<SortingType, Set<Island>> sortedIslands = new ConcurrentHashMap<>();

    private final SuperiorSkyblockPlugin plugin;

    public DefaultIslandsContainer(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void addIsland(Island island) {
        Location islandLocation = island.getCenter(plugin.getSettings().getWorlds().getDefaultWorld());
        this.islandsByPositions.put(IslandPosition.of(islandLocation), island);

        if (plugin.getProviders().hasCustomWorldsSupport()) {
            runWithCustomWorld(islandLocation, island, World.Environment.NORMAL,
                    location -> this.islandsByPositions.put(IslandPosition.of(location), island));
            runWithCustomWorld(islandLocation, island, World.Environment.NETHER,
                    location -> this.islandsByPositions.put(IslandPosition.of(location), island));
            runWithCustomWorld(islandLocation, island, World.Environment.THE_END,
                    location -> this.islandsByPositions.put(IslandPosition.of(location), island));
        }

        this.islandsByUUID.put(island.getUniqueId(), island);

        sortedIslands.values().forEach(sortedIslands -> sortedIslands.add(island));
    }

    @Override
    public void removeIsland(Island island) {
        Location islandLocation = island.getCenter(plugin.getSettings().getWorlds().getDefaultWorld());

        islandsByUUID.remove(island.getUniqueId());
        islandsByPositions.remove(IslandPosition.of(islandLocation));

        sortedIslands.values().forEach(sortedIslands -> sortedIslands.remove(island));

        if (plugin.getProviders().hasCustomWorldsSupport()) {
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
    public Island getIslandAtPosition(int position, SortingType sortingType) {
        ensureSortingType(sortingType);

        Set<Island> sortedIslands = this.sortedIslands.get(sortingType);

        return position < 0 || position > sortedIslands.size() ? null : Iterables.get(sortedIslands, position);
    }

    @Override
    public int getIslandPosition(Island island, SortingType sortingType) {
        ensureSortingType(sortingType);

        Set<Island> sortedIslands = this.sortedIslands.get(sortingType);

        return Iterables.indexOf(sortedIslands, island::equals);
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
    public void sortIslands(SortingType sortingType, Runnable onFinish) {
        this.sortIslands(sortingType, false, onFinish);
    }

    @Override
    public void sortIslands(SortingType sortingType, boolean forceSort, Runnable onFinish) {
        ensureSortingType(sortingType);

        Set<Island> sortedIslands = this.sortedIslands.get(sortingType);

        if (!forceSort && sortedIslands.size() <= 1) {
            if (onFinish != null)
                onFinish.run();
            return;
        }

        if (Bukkit.isPrimaryThread()) {
            BukkitExecutor.async(() -> sortIslandsInternal(sortingType, onFinish));
        } else {
            sortIslandsInternal(sortingType, onFinish);
        }
    }

    @Override
    public List<Island> getSortedIslands(SortingType sortingType) {
        ensureSortingType(sortingType);
        return new SequentialListBuilder<Island>()
                .build(this.sortedIslands.get(sortingType));
    }

    @Override
    public List<Island> getIslandsUnsorted() {
        return new SequentialListBuilder<Island>().build(this.islandsByUUID.values());
    }

    @Override
    public void addSortingType(SortingType sortingType, boolean sort) {
        Preconditions.checkArgument(!sortedIslands.containsKey(sortingType), "You cannot register an existing sorting type to the database.");
        sortIslandsInternal(sortingType, null);
    }

    private void runWithCustomWorld(Location islandLocation, Island island, World.Environment environment, Consumer<Location> onSuccess) {
        try {
            Location location = island.getCenter(environment);
            if (!location.getWorld().equals(islandLocation.getWorld()))
                onSuccess.accept(location);
        } catch (Exception ignored) {
        }
    }

    private void ensureSortingType(SortingType sortingType) {
        Preconditions.checkState(sortedIslands.containsKey(sortingType), "The sorting-type " + sortingType + " doesn't exist in the database. Please contact author!");
    }

    private void sortIslandsInternal(SortingType sortingType, Runnable onFinish) {
        Set<Island> newSortedTree = new TreeSet<>(sortingType);

        for (Island island : islandsByUUID.values()) {
            if (!island.isIgnored())
                newSortedTree.add(island);
        }

        this.sortedIslands.put(sortingType, newSortedTree);

        if (onFinish != null)
            onFinish.run();
    }

}

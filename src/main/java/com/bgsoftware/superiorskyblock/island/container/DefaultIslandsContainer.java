package com.bgsoftware.superiorskyblock.island.container;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.island.container.IslandsContainer;
import com.bgsoftware.superiorskyblock.core.IslandPosition;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.collections.EnumerateSet;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.core.threads.Synchronized;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class DefaultIslandsContainer implements IslandsContainer {

    private final Map<IslandPosition, Island> islandsByPositions = new ConcurrentHashMap<>();
    private final Map<UUID, Island> islandsByUUID = new ConcurrentHashMap<>();

    private final Map<SortingType, Synchronized<List<Island>>> sortedIslands = new ConcurrentHashMap<>();

    private final EnumerateSet<SortingType> notifiedValues = new EnumerateSet<>(SortingType.values());

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

        sortedIslands.values().forEach(sortedIslands -> {
            sortedIslands.write(_sortedIslands -> _sortedIslands.add(island));
        });
    }

    @Override
    public void removeIsland(Island island) {
        Location islandLocation = island.getCenter(plugin.getSettings().getWorlds().getDefaultWorld());

        islandsByUUID.remove(island.getUniqueId());
        islandsByPositions.remove(IslandPosition.of(islandLocation));

        sortedIslands.values().forEach(sortedIslands -> {
            sortedIslands.write(_sortedIslands -> _sortedIslands.remove(island));
        });

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
        return this.sortedIslands.get(sortingType).readAndGet(sortedIslands -> {
            return position < 0 || position >= sortedIslands.size() ? null : sortedIslands.get(position);
        });
    }

    @Override
    public int getIslandPosition(Island island, SortingType sortingType) {
        ensureSortingType(sortingType);
        return this.sortedIslands.get(sortingType).readAndGet(sortedIslands -> {
            return sortedIslands.indexOf(island);
        });
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

        Synchronized<List<Island>> sortedIslands = this.sortedIslands.get(sortingType);

        if (!forceSort && (sortedIslands.readAndGet(List::size) <= 1 || !notifiedValues.remove(sortingType))) {
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
    public void notifyChange(SortingType sortingType, Island island) {
        notifiedValues.add(sortingType);
    }

    @Override
    public List<Island> getSortedIslands(SortingType sortingType) {
        ensureSortingType(sortingType);
        return this.sortedIslands.get(sortingType).readAndGet(sortedIslands ->
                new SequentialListBuilder<Island>().build(sortedIslands));
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
        List<Island> newIslandsList = new ArrayList<>(islandsByUUID.values());
        newIslandsList.removeIf(Island::isIgnored);

        newIslandsList.sort(sortingType);

        this.sortedIslands.put(sortingType, Synchronized.of(newIslandsList));

        if (onFinish != null)
            onFinish.run();
    }

}

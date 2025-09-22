package com.bgsoftware.superiorskyblock.island.container;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.island.container.IslandsContainer;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.core.IslandPosition;
import com.bgsoftware.superiorskyblock.core.LazyWorldLocation;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.collections.EnumerateSet;
import com.bgsoftware.superiorskyblock.core.collections.IslandPosition2ObjectMap;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.core.threads.Synchronized;
import com.bgsoftware.superiorskyblock.island.IslandNames;
import com.bgsoftware.superiorskyblock.island.top.SortingComparators;
import com.bgsoftware.superiorskyblock.island.top.SortingTypes;
import com.bgsoftware.superiorskyblock.island.top.metadata.IslandSortMetadata;
import com.bgsoftware.superiorskyblock.island.top.metadata.IslandSortPlayerMetadata;
import com.bgsoftware.superiorskyblock.island.top.metadata.IslandSortRatingMetadata;
import com.bgsoftware.superiorskyblock.island.top.metadata.IslandSortValueMetadata;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class DefaultIslandsContainer implements IslandsContainer {

    private final Synchronized<IslandPosition2ObjectMap<Island>> islandsByPositions = Synchronized.of(new IslandPosition2ObjectMap<>());
    private final Map<UUID, Island> islandsByUUID = new ConcurrentHashMap<>();
    private final Map<String, Island> islandsByNames = new ConcurrentHashMap<>();

    private final Map<SortingType, Synchronized<List<Island>>> sortedIslands = new ConcurrentHashMap<>();

    private final EnumerateSet<SortingType> notifiedValues = new EnumerateSet<>(SortingType.values());

    private final IslandsCache islandsCache = new IslandsCache();

    private final SuperiorSkyblockPlugin plugin;

    public DefaultIslandsContainer(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void addIsland(Island island) {
        BlockPosition center = island.getCenterPosition();
        WorldInfo defaultWorld = plugin.getGrid().getIslandsWorldInfo(island,
                plugin.getSettings().getWorlds().getDefaultWorldDimension());

        Preconditions.checkNotNull(defaultWorld, "Default world information cannot be null!");

        long packedPos = IslandPosition.calculatePackedPosFromLocation(center.getX(), center.getZ());

        // Insert to cache if we can access it
        accessIslandsCache(islandsCache -> islandsCache.insert(defaultWorld.getName(), packedPos, island));

        this.islandsByPositions.write(islandsByPositions -> {
            islandsByPositions.put(defaultWorld.getName(), packedPos, island);

            if (plugin.getProviders().hasCustomWorldsSupport()) {
                // We don't know the logic of the custom worlds support, therefore we add a position
                // for every possible world, so there won't be issues with detecting islands later.
                for (Dimension dimension : Dimension.values()) {
                    if (plugin.getProviders().getWorldsProvider().isDimensionEnabled(dimension)) {
                        runWithCustomWorld(defaultWorld, center, island, dimension, (worldName, customWorldPackedPos) ->
                                islandsByPositions.put(worldName, customWorldPackedPos, island));
                    }
                }
            }
        });

        this.islandsByUUID.put(island.getUniqueId(), island);
        this.islandsByNames.put(IslandNames.getNameForLookup(island.getStrippedName()), island);

        sortedIslands.values().forEach(sortedIslands -> {
            sortedIslands.write(_sortedIslands -> _sortedIslands.add(island));
        });
    }

    @Override
    public void removeIsland(Island island) {
        BlockPosition center = island.getCenterPosition();
        WorldInfo defaultWorld = plugin.getGrid().getIslandsWorldInfo(island,
                plugin.getSettings().getWorlds().getDefaultWorldDimension());

        Preconditions.checkNotNull(defaultWorld, "Default world information cannot be null!");

        long packedPos = IslandPosition.calculatePackedPosFromLocation(center.getX(), center.getZ());

        // Remove from cache if we can access it
        accessIslandsCache(islandsCache -> islandsCache.remove(defaultWorld.getName(), packedPos));

        this.islandsByPositions.write(islandsByPositions -> {
            islandsByPositions.remove(defaultWorld.getName(), packedPos);

            if (plugin.getProviders().hasCustomWorldsSupport()) {
                for (Dimension dimension : Dimension.values()) {
                    if (plugin.getProviders().getWorldsProvider().isDimensionEnabled(dimension)) {
                        runWithCustomWorld(defaultWorld, center, island, dimension, islandsByPositions::remove);
                    }
                }
            }
        });

        this.islandsByUUID.remove(island.getUniqueId());
        this.islandsByNames.remove(IslandNames.getNameForLookup(island.getStrippedName()));

        sortedIslands.values().forEach(sortedIslands -> {
            sortedIslands.write(_sortedIslands -> _sortedIslands.remove(island));
        });
    }

    @Nullable
    @Override
    public Island getIslandByUUID(UUID uuid) {
        return this.islandsByUUID.get(uuid);
    }

    @Override
    public Island getIslandByName(String name) {
        return this.islandsByNames.get(IslandNames.getNameForLookup(name));
    }

    @Override
    public void updateIslandName(Island island, String oldName) {
        Island currentIsland = this.islandsByNames.remove(IslandNames.getNameForLookup(oldName));
        if (currentIsland == island)
            this.islandsByNames.put(IslandNames.getNameForLookup(island.getStrippedName()), island);
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
        long packedPos = IslandPosition.calculatePackedPosFromLocation(location.getBlockX(), location.getBlockZ());
        String worldName = LazyWorldLocation.getWorldName(location);
        Island island = readIslandsCache(islandsCache -> islandsCache.get(worldName, packedPos),
                () -> this.islandsByPositions.readAndGet(islandsByPositions ->
                        islandsByPositions.get(worldName, packedPos)));
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

    private void ensureSortingType(SortingType sortingType) {
        Preconditions.checkState(sortedIslands.containsKey(sortingType), "The sorting-type " + sortingType + " doesn't exist in the database. Please contact author!");
    }

    private void sortIslandsInternal(SortingType sortingType, Runnable onFinish) {
        List<Island> existingIslands = new LinkedList<>(islandsByUUID.values());
        existingIslands.removeIf(Island::isIgnored);

        List<Island> sortedIslands;

        if (existingIslands.size() <= 1) {
            sortedIslands = existingIslands;
        } else try {
            if (sortingType == SortingTypes.BY_LEVEL || sortingType == SortingTypes.BY_WORTH ||
                    sortingType == SortingTypes.BY_PLAYERS || sortingType == SortingTypes.BY_RATING) {
                sortedIslands = sortIslandsBuiltinSortingType(existingIslands, sortingType);
            } else {
                sortedIslands = existingIslands;
                sortedIslands.sort(sortingType);
            }
        } catch (Throwable error) {
            Log.warn("An error occurred while sorting islands for sorting-type ", sortingType.getName(), ":");
            throw error;
        }

        this.sortedIslands.put(sortingType, Synchronized.of(sortedIslands));

        if (onFinish != null)
            onFinish.run();
    }

    private List<Island> sortIslandsBuiltinSortingType(List<Island> existingIslands, SortingType sortingType) {
        List<IslandSortMetadata<?>> islandMetadatas = new LinkedList<>();

        if (sortingType == SortingTypes.BY_WORTH)
            existingIslands.forEach(island -> islandMetadatas.add(new IslandSortValueMetadata(island, island.getWorth())));
        else if (sortingType == SortingTypes.BY_LEVEL)
            existingIslands.forEach(island -> islandMetadatas.add(new IslandSortValueMetadata(island, island.getIslandLevel())));
        else if (sortingType == SortingTypes.BY_RATING)
            existingIslands.forEach(island -> islandMetadatas.add(new IslandSortRatingMetadata(island)));
        else /* BY_PLAYERS */
            existingIslands.forEach(island -> islandMetadatas.add(new IslandSortPlayerMetadata(island)));

        if (islandMetadatas.size() > 1) {
            islandMetadatas.sort(SortingComparators.ISLAND_METADATA_COMPARATOR);
        }

        existingIslands.clear();
        islandMetadatas.forEach(islandMetadata -> existingIslands.add(islandMetadata.getIsland()));

        return existingIslands;
    }

    private void runWithCustomWorld(WorldInfo defaultWorld, BlockPosition center, Island island,
                                    Dimension dimension, CustomWorldConsumer consumer) {
        WorldInfo worldInfo = plugin.getGrid().getIslandsWorldInfo(island, dimension);
        if (worldInfo != null && !worldInfo.equals(defaultWorld)) {
            consumer.apply(worldInfo.getName(), IslandPosition.calculatePackedPosFromLocation(center.getX(), center.getZ()));
        }
    }

    private void accessIslandsCache(Consumer<IslandsCache> consumer) {
        if (Bukkit.isPrimaryThread()) {
            consumer.accept(islandsCache);
        }
    }

    private <R> R readIslandsCache(Function<IslandsCache, R> function, Supplier<R> onInvalidAccess) {
        if (Bukkit.isPrimaryThread()) {
            return function.apply(islandsCache);
        }

        return onInvalidAccess.get();
    }

    private interface CustomWorldConsumer {

        void apply(String worldName, long packedPos);

    }

    private class IslandsCache {

        // Implemented with CacheHolder so IslandPositions with no islands will not trigger access to global `islandsByPositions`
        private final IslandPosition2ObjectMap<CacheHolder> islandsByPositionCache = new IslandPosition2ObjectMap<>();

        @Nullable
        private Island get(String worldName, long packedPos) {
            CacheHolder holder = islandsByPositionCache.get(worldName, packedPos);
            if (holder != null)
                return holder.island.get();

            Island island = islandsByPositions.readAndGet(islandsByPositions ->
                    islandsByPositions.get(worldName, packedPos));

            insert(worldName, packedPos, island);

            return island;
        }

        private void insert(String worldName, long packedPos, @Nullable Island island) {
            islandsByPositionCache.put(worldName, packedPos, island == null ? CacheHolder.NULL : new CacheHolder(island));
        }

        private void remove(String worldName, long packedPos) {
            this.islandsByPositionCache.remove(worldName, packedPos);
        }

    }

    private static class CacheHolder {

        private static final CacheHolder NULL = new CacheHolder(null);

        private final WeakReference<Island> island;

        CacheHolder(Island island) {
            this.island = new WeakReference<>(island);
        }

    }

}

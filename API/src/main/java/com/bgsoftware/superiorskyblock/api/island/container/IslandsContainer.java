package com.bgsoftware.superiorskyblock.api.island.container;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.IslandBase;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface IslandsContainer {

    /**
     * Add an island to the islands container.
     *
     * @param island The island to add.
     */
    default void addIsland(Island island) {
        this.addIsland((IslandBase) island);
    }

    /**
     * Add an island to the islands container.
     *
     * @param island The island to add.
     */
    void addIsland(IslandBase island);

    /**
     * Remove an island from the islands container.
     *
     * @param island The island to remove.
     */
    default void removeIsland(Island island) {
        this.removeIsland((IslandBase) island);
    }

    /**
     * Remove an island from the islands container.
     *
     * @param island The island to remove.
     */
    void removeIsland(IslandBase island);

    /**
     * Get an island by its uuid.
     * Warning! This method will load all the island from cache!
     *
     * @param uuid The uuid of the island.
     * @deprecated See {@link #getBaseIslandByUUID(UUID)}
     */
    @Nullable
    Island getIslandByUUID(UUID uuid);

    /**
     * Get an island by its uuid.
     *
     * @param uuid The uuid of the island.
     */
    @Nullable
    IslandBase getBaseIslandByUUID(UUID uuid);

    /**
     * Get an island by its leader's uuid.
     *
     * @param uuid The uuid of the island's leader.
     * @deprecated Not supported anymore.
     */
    @Nullable
    @Deprecated
    default Island getIslandByLeader(UUID uuid) {
        return SuperiorSkyblockAPI.getGrid().getIsland(uuid);
    }

    /**
     * Get an island by its position in the top-islands.
     * Warning! This method will load the island from cache!
     *
     * @param position    The position of the island.
     * @param sortingType The sorting-type to get islands from.
     * @deprecated See {@link #getBaseIslandAtPosition(int, SortingType)}
     */
    @Nullable
    @Deprecated
    Island getIslandAtPosition(int position, SortingType sortingType);

    /**
     * Get an island by its position in the top-islands.
     *
     * @param position    The position of the island.
     * @param sortingType The sorting-type to get islands from.
     */
    @Nullable
    IslandBase getBaseIslandAtPosition(int position, SortingType sortingType);

    /**
     * Get the position of an island in the top-islands.
     *
     * @param island      The island to get position of.
     * @param sortingType The sorting-type to get islands from.
     */
    default int getIslandPosition(Island island, SortingType sortingType) {
        return this.getIslandPosition((IslandBase) island, sortingType);
    }

    /**
     * Get the position of an island in the top-islands.
     *
     * @param island      The island to get position of.
     * @param sortingType The sorting-type to get islands from.
     */
    int getIslandPosition(IslandBase island, SortingType sortingType);

    /**
     * Get the amount of islands on the server.
     */
    int getIslandsAmount();

    /**
     * Get an island at a location.
     * Warning! This method will load the island from cache!
     *
     * @param location The location to get island in.
     * @deprecated See {@link #getBaseIslandAt(Location)}
     */
    @Nullable
    @Deprecated
    Island getIslandAt(Location location);

    /**
     * Get an island at a location.
     *
     * @param location The location to get island in.
     */
    @Nullable
    IslandBase getBaseIslandAt(Location location);

    /**
     * Transfer an island from a player to another one.
     * Warning: If you don't know what you're doing, do not use this method.
     * Instead, use {@link Island#transferIsland(SuperiorPlayer)}.
     *
     * @param oldLeader The uuid of the current leader.
     * @param newLeader The uuid of the new leader.
     * @deprecated Not supported anymore.
     */
    @Deprecated
    default void transferIsland(UUID oldLeader, UUID newLeader) {

    }

    /**
     * Sort islands for the top-islands.
     * The islands will not get sorted if only one island exists, or no changes
     * were tracked by {@link #notifyChange(SortingType, Island)}
     *
     * @param sortingType The type of sorting to use.
     * @param onFinish    Callback method
     */
    void sortIslands(SortingType sortingType, @Nullable Runnable onFinish);

    /**
     * Sort islands for the top-islands.
     *
     * @param sortingType The type of sorting to use.
     * @param forceSort   Whether to force-sort the islands.
     *                    When true, islands will get sorted even if only one island exists.
     * @param onFinish    Callback method
     */
    void sortIslands(SortingType sortingType, boolean forceSort, @Nullable Runnable onFinish);

    /**
     * Notify about a change of a value for a specific sorting type for an island.
     *
     * @param sortingType The sorting-type.
     * @param island      The island that had its value changed.
     */
    default void notifyChange(SortingType sortingType, Island island) {
        this.notifyChange(sortingType, (IslandBase) island);
    }

    /**
     * Notify about a change of a value for a specific sorting type for an island.
     *
     * @param sortingType The sorting-type.
     * @param island      The island that had its value changed.
     */
    void notifyChange(SortingType sortingType, IslandBase island);

    /**
     * Get all islands sorted by a specific sorting-type.
     * Warning! This method will load all islands from cache!
     *
     * @param sortingType The type of sorting to use.
     * @deprecated See {@link #getSortedBaseIslands(SortingType)}
     */
    @Deprecated
    List<Island> getSortedIslands(SortingType sortingType);

    /**
     * Get all islands sorted by a specific sorting-type.
     *
     * @param sortingType The type of sorting to use.
     */
    List<IslandBase> getSortedBaseIslands(SortingType sortingType);

    /**
     * Get all islands.
     * Warning! This method will load all islands from cache!
     *
     * @deprecated See {@link #getBaseIslandsUnsorted()} ()}
     */
    @Deprecated
    List<Island> getIslandsUnsorted();

    /**
     * Get all islands.
     */
    List<IslandBase> getBaseIslandsUnsorted();

    /**
     * Add a new sorting-type.
     *
     * @param sortingType The sorting-type to add.
     * @param sort        Whether to sort the islands or not when the sorting-type is added.
     */
    void addSortingType(SortingType sortingType, boolean sort);

}

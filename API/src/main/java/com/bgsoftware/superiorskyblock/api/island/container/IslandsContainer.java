package com.bgsoftware.superiorskyblock.api.island.container;

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
    void addIsland(Island island);

    /**
     * Remove an island from the islands container.
     *
     * @param island The island to remove.
     */
    void removeIsland(Island island);

    /**
     * Get an island by its uuid.
     *
     * @param uuid The uuid of the island.
     */
    @Nullable
    Island getIslandByUUID(UUID uuid);

    /**
     * Get an island by its leader's uuid.
     *
     * @param uuid The uuid of the island's leader.
     */
    @Nullable
    Island getIslandByLeader(UUID uuid);

    /**
     * Get an island by its position in the top-islands.
     *
     * @param position    The position of the island.
     * @param sortingType The sorting-type to get islands from.
     */
    @Nullable
    Island getIslandAtPosition(int position, SortingType sortingType);

    /**
     * Get the position of an island in the top-islands.
     *
     * @param island      The island to get position of.
     * @param sortingType The sorting-type to get islands from.
     */
    int getIslandPosition(Island island, SortingType sortingType);

    /**
     * Get the amount of islands on the server.
     */
    int getIslandsAmount();

    /**
     * Get an island at a location.
     *
     * @param location The location to get island in.
     */
    @Nullable
    Island getIslandAt(Location location);

    /**
     * Transfer an island from a player to another one.
     * Warning: If you don't know what you're doing, do not use this method.
     * Instead, use {@link Island#transferIsland(SuperiorPlayer)}.
     *
     * @param oldLeader The uuid of the current leader.
     * @param newLeader The uuid of the new leader.
     */
    void transferIsland(UUID oldLeader, UUID newLeader);

    /**
     * Sort islands for the top-islands.
     * The islands will not get sorted if only one island exists.
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
     * Get all islands sorted by a specific sorting-type.
     *
     * @param sortingType The type of sorting to use.
     */
    List<Island> getSortedIslands(SortingType sortingType);

    /**
     * Get all islands.
     */
    List<Island> getIslandsUnsorted();

    /**
     * Add a new sorting-type.
     *
     * @param sortingType The sorting-type to add.
     * @param sort        Whether to sort the islands or not when the sorting-type is added.
     */
    void addSortingType(SortingType sortingType, boolean sort);

}

package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.data.IDatabaseBridgeHolder;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPreview;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.island.container.IslandsContainer;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import com.bgsoftware.superiorskyblock.api.world.algorithm.IslandCreationAlgorithm;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface GridManager extends IDatabaseBridgeHolder {

    /**
     * Create a new island.
     *
     * @param superiorPlayer The new owner for the island.
     * @param schemName      The schematic that should be used.
     * @param bonus          A starting worth for the island.
     * @param biome          A starting biome for the island.
     * @param islandName     The name of the new island.
     */
    void createIsland(SuperiorPlayer superiorPlayer, String schemName, BigDecimal bonus, Biome biome, String islandName);

    /**
     * Create a new island.
     *
     * @param superiorPlayer The new owner for the island.
     * @param schemName      The schematic that should be used.
     * @param bonus          A starting worth for the island.
     * @param biome          A starting biome for the island.
     * @param islandName     The name of the new island.
     * @param offset         Should the island have an offset for it's values? If disabled, the bonus will be given.
     */
    void createIsland(SuperiorPlayer superiorPlayer, String schemName, BigDecimal bonus, Biome biome, String islandName, boolean offset);

    /**
     * Create a new island.
     *
     * @param superiorPlayer The new owner for the island.
     * @param schemName      The schematic that should be used.
     * @param bonusWorth     A starting worth for the island.
     * @param bonusLevel     A starting level for the island.
     * @param biome          A starting biome for the island.
     * @param islandName     The name of the new island.
     * @param offset         Should the island have an offset for it's values? If disabled, the bonus will be given.
     */
    void createIsland(SuperiorPlayer superiorPlayer, String schemName, BigDecimal bonusWorth, BigDecimal bonusLevel,
                      Biome biome, String islandName, boolean offset);

    /**
     * Create a new island.
     *
     * @param builder The builder for the island.
     * @param biome   A starting biome for the island.
     * @param offset  Should the island have an offset for its values? If disabled, the bonus will be given.
     */
    void createIsland(Island.Builder builder, Biome biome, boolean offset);

    /**
     * Set the creation algorithm of islands.
     *
     * @param islandCreationAlgorithm The new algorithm to set.
     *                                If null, the default one will be used.
     */
    void setIslandCreationAlgorithm(@Nullable IslandCreationAlgorithm islandCreationAlgorithm);

    /**
     * Get the currently used island creation algorithm.
     */
    IslandCreationAlgorithm getIslandCreationAlgorithm();

    /**
     * Checks if a player has an active request for creating an island.
     *
     * @param superiorPlayer The player to check.
     */
    boolean hasActiveCreateRequest(SuperiorPlayer superiorPlayer);

    /**
     * Start the island preview task for a specific player.
     *
     * @param superiorPlayer The player to start preview for.
     * @param schemName      The schematic to preview.
     * @param islandName     The requested island name by the player.
     */
    void startIslandPreview(SuperiorPlayer superiorPlayer, String schemName, String islandName);

    /**
     * Cancel the island preview for a specific player.
     *
     * @param superiorPlayer The player to cancel preview for.
     */
    void cancelIslandPreview(SuperiorPlayer superiorPlayer);

    /**
     * Cancel all active island previews.
     */
    void cancelAllIslandPreviews();

    /**
     * Check if a player has an ongoing island preview task.
     *
     * @param superiorPlayer The player to check.
     */
    @Nullable
    IslandPreview getIslandPreview(SuperiorPlayer superiorPlayer);

    /**
     * Delete an island.
     *
     * @param island The island to delete.
     */
    void deleteIsland(Island island);

    /**
     * Get the island of a specific player.
     *
     * @param superiorPlayer The player to check.
     * @return The island of the player. May be null.
     * @deprecated See {@link SuperiorPlayer#getIsland()}
     */
    @Nullable
    @Deprecated
    Island getIsland(SuperiorPlayer superiorPlayer);

    /**
     * Get the island in a specific position from one of the top lists.
     * Positions are starting from 0.
     *
     * @param position    The position to check.
     * @param sortingType The sorting type that should be considered.
     * @return The island in that position. May be null.
     */
    @Nullable
    Island getIsland(int position, SortingType sortingType);

    /**
     * Get the position of an island.
     * Positions are starting from 0.
     *
     * @param island      The island to check.
     * @param sortingType The sorting type that should be considered.
     * @return The position of the island.
     */
    int getIslandPosition(Island island, SortingType sortingType);

    /**
     * Get an island by it's owner uuid.
     *
     * @param uuid The uuid of the owner.
     * @return The island of the owner. May be null.
     * @deprecated See {@link SuperiorPlayer#getIsland()}
     */
    @Nullable
    @Deprecated
    Island getIsland(UUID uuid);

    /**
     * Get an island by it's uuid.
     *
     * @param uuid The uuid of the island.
     * @return The island with that UUID. May be null.
     */
    @Nullable
    Island getIslandByUUID(UUID uuid);

    /**
     * Get an island by it's name.
     *
     * @param islandName The name to check.
     * @return The island with that name. May be null.
     */
    @Nullable
    Island getIsland(String islandName);

    /**
     * Get an island at an exact position in the world.
     *
     * @param location The position to check.
     * @return The island at that position. May be null.
     */
    @Nullable
    Island getIslandAt(@Nullable Location location);

    /**
     * Get an island from a chunk.
     *
     * @param chunk The chunk to check.
     * @return The island at that position. May be null.
     * @deprecated See {@link #getIslandsAt(Chunk)}
     */
    @Nullable
    @Deprecated
    Island getIslandAt(@Nullable Chunk chunk);

    /**
     * Get all the islands from a chunk.
     *
     * @param chunk The chunk to check.
     * @return The islands at that position.
     */
    @Nullable
    List<Island> getIslandsAt(@Nullable Chunk chunk);

    /**
     * Transfer an island's leadership to another owner.
     *
     * @param oldOwner The old owner of the island.
     * @param newOwner The new owner of the island.
     */
    void transferIsland(UUID oldOwner, UUID newOwner);

    /**
     * Get the amount of islands.
     */
    int getSize();

    /**
     * Sort the islands.
     *
     * @param sortingType The sorting type to use.
     */
    void sortIslands(SortingType sortingType);

    /**
     * Sort the islands.
     *
     * @param sortingType The sorting type to use.
     * @param onFinish    Callback runnable.
     */
    void sortIslands(SortingType sortingType, @Nullable Runnable onFinish);

    /**
     * Get the spawn island object.
     */
    Island getSpawnIsland();

    /**
     * Get the world of an island by the environment.
     * If the environment is not the normal and that environment is disabled in config, null will be returned.
     *
     * @param environment The world environment.
     * @param island      The island to check.
     */
    @Nullable
    World getIslandsWorld(Island island, World.Environment environment);

    /**
     * Get the {@link WorldInfo} of the world of an island by the environment.
     * The world might not be loaded at the time of calling this method.
     *
     * @param island      The island to check.
     * @param environment The world environment.
     * @return The world info for the given environment, or null if this environment is not enabled.
     */
    @Nullable
    WorldInfo getIslandsWorldInfo(Island island, World.Environment environment);

    /**
     * Get the {@link WorldInfo} of the world of an island by its name.
     * The world might not be loaded at the time of calling this method.
     *
     * @param island    The island to check.
     * @param worldName The name of the world.
     * @return The world info for the given name, or null if this name is not an islands world.
     */
    @Nullable
    WorldInfo getIslandsWorldInfo(Island island, String worldName);

    /**
     * Checks if the given world is an islands world.
     * Can be the normal world, the nether world (if enabled in config) or the end world (if enabled in config)
     */
    boolean isIslandsWorld(World world);

    /**
     * Register a world as a islands world.
     * This will add all protections to that world, however - no islands will by physically there.
     *
     * @param world The world to register as an islands world.
     */
    void registerIslandWorld(World world);

    /**
     * Get all registered worlds.
     */
    List<World> getRegisteredWorlds();

    /**
     * Get all the islands ordered by a specific sorting type.
     *
     * @param sortingType The sorting type to order the list by.
     * @return A list of uuids of the island owners.
     * @deprecated See {@link #getIslands(SortingType)}
     */
    @Deprecated
    List<UUID> getAllIslands(SortingType sortingType);

    /**
     * Get all the islands unordered.
     */
    List<Island> getIslands();

    /**
     * Get all the islands ordered by a specific sorting type.
     *
     * @param sortingType The sorting type to order the list by.
     * @return A list of uuids of the island owners.
     */
    List<Island> getIslands(SortingType sortingType);

    /**
     * Get the block amount of a specific block.
     *
     * @param block The block to check.
     * @deprecated see {@link StackedBlocksManager}
     */
    @Deprecated
    int getBlockAmount(Block block);

    /**
     * Get the block amount of a specific location.
     *
     * @param location The location to check.
     * @deprecated see {@link StackedBlocksManager}
     */
    @Deprecated
    int getBlockAmount(Location location);

    /**
     * Set a new amount for a specific block.
     *
     * @param block  The block to set the amount to.
     * @param amount The new amount of the block.
     * @deprecated see {@link StackedBlocksManager}
     */
    @Deprecated
    void setBlockAmount(Block block, int amount);

    /**
     * Get all the stacked blocks on the server.
     *
     * @deprecated see {@link StackedBlocksManager}
     */
    @Deprecated
    List<Location> getStackedBlocks();

    /**
     * Calculate the worth of all the islands on the server.
     */
    void calcAllIslands();

    /**
     * Calculate the worth of all the islands on the server.
     *
     * @param callback Runnable that will be ran when process is finished.
     */
    void calcAllIslands(@Nullable Runnable callback);

    /**
     * Make the island to be deleted when server stops.
     *
     * @param island The island to delete.
     */
    void addIslandToPurge(Island island);

    /**
     * Remove the island from being deleted when server stops.
     *
     * @param island The island to keep.
     */
    void removeIslandFromPurge(Island island);

    /**
     * Check if the island will be deleted when the server stops?
     */
    boolean isIslandPurge(Island island);

    /**
     * Get all the islands that will be deleted when the server stops.
     */
    List<Island> getIslandsToPurge();

    /**
     * Add a new sorting type to the registry of islands.
     *
     * @param sortingType The new sorting type to register.
     */
    void registerSortingType(SortingType sortingType);

    /**
     * Get the total worth of all the islands.
     * This value is updated every minute, so it might not be 100% accurate.
     */
    BigDecimal getTotalWorth();

    /**
     * Get the total level of all the islands.
     * This value is updated every minute, so it might not be 100% accurate.
     */
    BigDecimal getTotalLevel();

    /**
     * Get the location of the last island that was generated.
     */
    Location getLastIslandLocation();

    /**
     * Set the location of the last island.
     * Warning: Do not use this method unless you know what you're doing
     *
     * @param location The location to set.
     */
    void setLastIslandLocation(Location location);

    /**
     * Get the islands container.
     */
    IslandsContainer getIslandsContainer();

    /**
     * Set a new islands container.
     *
     * @param islandsContainer The new islands container to set.
     */
    void setIslandsContainer(IslandsContainer islandsContainer);

}

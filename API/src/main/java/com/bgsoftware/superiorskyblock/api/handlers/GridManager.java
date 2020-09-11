package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface GridManager {

    /**
     * Create a new island.
     * @param superiorPlayer The new owner for the island.
     * @param schemName The schematic that should be used.
     * @param bonus A starting worth for the island.
     * @param biome A starting biome for the island.
     * @param islandName The name of the new island.
     */
    void createIsland(SuperiorPlayer superiorPlayer, String schemName, BigDecimal bonus, Biome biome, String islandName);

    /**
     * Create a new island.
     * @param superiorPlayer The new owner for the island.
     * @param schemName The schematic that should be used.
     * @param bonus A starting worth for the island.
     * @param biome A starting biome for the island.
     * @param islandName The name of the new island.
     * @param offset Should the island have an offset for it's values? If disabled, the bonus will be given.
     */
    void createIsland(SuperiorPlayer superiorPlayer, String schemName, BigDecimal bonus, Biome biome, String islandName, boolean offset);

    /**
     * Create a new island.
     * @param superiorPlayer The new owner for the island.
     * @param schemName The schematic that should be used.
     * @param bonusWorth A starting worth for the island.
     * @param bonusLevel A starting level for the island.
     * @param biome A starting biome for the island.
     * @param islandName The name of the new island.
     * @param offset Should the island have an offset for it's values? If disabled, the bonus will be given.
     */
    void createIsland(SuperiorPlayer superiorPlayer, String schemName, BigDecimal bonusWorth, BigDecimal bonusLevel, Biome biome, String islandName, boolean offset);

    /**
     * Checks if a player has an active request for creating an island.
     * @param superiorPlayer The player to check.
     */
    boolean hasActiveCreateRequest(SuperiorPlayer superiorPlayer);

    /**
     * Delete an island.
     * @param island The island to delete.
     */
    void deleteIsland(Island island);

    /**
     * Get the island of a specific player.
     * @param superiorPlayer The player to check.
     * @return The island of the player. May be null.
     */
    Island getIsland(SuperiorPlayer superiorPlayer);

    /**
     * Get the island in a specific position from the top-worth list.
     * Positions are starting from 0.
     * @param position The position to check.
     * @return The island in that position. May be null.
     *
     * @deprecated See getIsland(Integer, SortingType)
     */
    @Deprecated
    Island getIsland(int position);

    /**
     * Get the island in a specific position from one of the top lists.
     * Positions are starting from 0.
     * @param position The position to check.
     * @param sortingType The sorting type that should be considered.
     * @return The island in that position. May be null.
     */
    Island getIsland(int position, SortingType sortingType);

    /**
     * Get the position of an island.
     * Positions are starting from 0.
     * @param island The island to check.
     * @param sortingType The sorting type that should be considered.
     * @return The position of the island.
     */
    int getIslandPosition(Island island, SortingType sortingType);

    /**
     * Get an island by it's owner uuid.
     * @param uuid The uuid of the owner.
     * @return The island of the owner. May be null.
     */
    Island getIsland(UUID uuid);

    /**
     * Get an island by it's uuid.
     * @param uuid The uuid of the island.
     * @return The island with that UUID. May be null.
     */
    Island getIslandByUUID(UUID uuid);

    /**
     * Get an island by it's name.
     * @param islandName The name to check.
     * @return The island with that name. May be null.
     */
    Island getIsland(String islandName);

    /**
     * Get an island at an exact position in the world.
     * @param location The position to check.
     * @return The island at that position. May be null.
     */
    Island getIslandAt(Location location);

    /**
     * Get an island from a chunk.
     * @param chunk The chunk to check.
     * @return The island at that position. May be null.
     */
    Island getIslandAt(Chunk chunk);

    /**
     * Transfer an island's leadership to another owner.
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
     * @param sortingType The sorting type to use.
     */
    void sortIslands(SortingType sortingType);

    /**
     * Get the spawn island object.
     */
    Island getSpawnIsland();

    /**
     * Get the islands world.
     *
     * @deprecated See getIslandsWorld(Environment)
     */
    @Deprecated
    World getIslandsWorld();

    /**
     * Get the islands world by the environment.
     * If the environment is not the normal and that environment is disabled in config, null will be returned.
     * @param environment The world environment.
     * @deprecated Unexpected behavior. Check getIslandsWorld(Island, Environment)
     */
    @Deprecated
    World getIslandsWorld(World.Environment environment);

    /**
     * Get the world of an island by the environment.
     * If the environment is not the normal and that environment is disabled in config, null will be returned.
     * @param environment The world environment.
     * @param island The island to check.
     */
    World getIslandsWorld(Island island, World.Environment environment);

    /**
     * Checks if the given world is an islands world.
     * Can be the normal world, the nether world (if enabled in config) or the end world (if enabled in config)
     */
    boolean isIslandsWorld(World world);

    /**
     * Register a world as a islands world.
     * This will add all protections to that world, however - no islands will by physically there.
     * @param world The world to register as an islands world.
     */
    void registerIslandWorld(World world);

    /**
     * Get all registered worlds.
     */
    List<World> getRegisteredWorlds();

    /**
     * Get the next location for a new island.
     * @deprecated Moved to the separated WorldsProvider interface.
     * Using this method can give NullPointerExceptions and many other unexpected behaviors.
     */
    @Deprecated
    Location getNextLocation();

    /**
     * Get all the islands ordered by their worth.
     * @return A list of uuids of the island owners.
     *
     * @deprecated See getAllIslands(SortingType)
     */
    @Deprecated
    List<UUID> getAllIslands();

    /**
     * Get all the islands ordered by a specific sorting type.
     * @param sortingType The sorting type to order the list by.
     * @return A list of uuids of the island owners.
     */
    List<UUID> getAllIslands(SortingType sortingType);

    /**
     * Get all the islands unordered.
     */
    List<Island> getIslands();

    /**
     * Get all the islands ordered by a specific sorting type.
     * @param sortingType The sorting type to order the list by.
     * @return A list of uuids of the island owners.
     */
    List<Island> getIslands(SortingType sortingType);

    /**
     * Open the top islands menu for a player.
     * @param superiorPlayer The player to open the menu for.
     */
    @Deprecated
    void openTopIslands(SuperiorPlayer superiorPlayer);

    /**
     * Get the block amount of a specific block.
     * @param block The block to check.
     */
    int getBlockAmount(Block block);

    /**
     * Get the block amount of a specific location.
     * @param location The location to check.
     */
    int getBlockAmount(Location location);

    /**
     * Set a new amount for a specific block.
     * @param block The block to set the amount to.
     * @param amount The new amount of the block.
     */
    void setBlockAmount(Block block, int amount);

    /**
     * Calculate the worth of all the islands on the server.
     */
    void calcAllIslands();

    /**
     * Calculate the worth of all the islands on the server.
     * @param callback Runnable that will be ran when process is finished.
     */
    void calcAllIslands(Runnable callback);

    /**
     * Checks whether or not the material is a spawner.
     * @param material The material to check.
     *
     * @deprecated See KeysManager.
     */
    @Deprecated
    boolean isSpawner(Material material);

    /**
     * Make the island to be deleted when server stops.
     * @param island The island to delete.
     */
    void addIslandToPurge(Island island);

    /**
     * Remove the island from being deleted when server stops.
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

}

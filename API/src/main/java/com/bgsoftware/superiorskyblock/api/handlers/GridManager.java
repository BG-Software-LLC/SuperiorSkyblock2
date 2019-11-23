package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
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
     */
    World getIslandsWorld(World.Environment environment);

    /**
     * Checks if the given world is an islands world.
     * Can be the normal world, the nether world (if enabled in config) or the end world (if enabled in config)
     */
    boolean isIslandsWorld(World world);

    /**
     * Get the next location for a new island.
     */
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
     * Checks whether or not the material is a spawner.
     * @param material The material to check.
     *
     * @deprecated See KeysManager.
     */
    @Deprecated
    boolean isSpawner(Material material);

}

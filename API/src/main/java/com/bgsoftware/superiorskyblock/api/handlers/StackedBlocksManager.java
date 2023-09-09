package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.data.IDatabaseBridgeHolder;
import com.bgsoftware.superiorskyblock.api.key.Key;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Map;

public interface StackedBlocksManager extends IDatabaseBridgeHolder {

    /**
     * Get the block amount in a specific block.
     *
     * @param block The block to check.
     */
    int getStackedBlockAmount(Block block);

    /**
     * Get the block amount in a specific location.
     *
     * @param location The location to check.
     */
    int getStackedBlockAmount(Location location);

    /**
     * Get the block's key in a specific location.
     *
     * @param location The location to check.
     */
    @Nullable
    Key getStackedBlockKey(Location location);

    /**
     * Set a new amount for a specific block.
     *
     * @param block  The block to set the amount to.
     * @param amount The new amount of the block.
     */
    boolean setStackedBlock(Block block, int amount);

    /**
     * Set a new amount for a specific block.
     *
     * @param location The location of the block.
     * @param blockKey The key of the block.
     * @param amount   The new amount of the block.
     * @return true on success.
     */
    boolean setStackedBlock(Location location, Key blockKey, int amount);

    /**
     * Remove stacked block at a specific location.
     *
     * @param location The location of the stacked block.
     * @return The amount of the removed block, or 1 if there were no blocks in the specified location.
     */
    int removeStackedBlock(Location location);

    /**
     * Remove stacked blocks at a specific chunk.
     *
     * @param chunk The chunk to remove stacked blocks from.
     * @return The stacked blocks in the provided chunk.
     */
    Map<Location, Integer> removeStackedBlocks(Chunk chunk);

    /**
     * Remove stacked blocks at a specific chunk.
     *
     * @param world  The world of the chunk.
     * @param chunkX The x-coords value of the chunk.
     * @param chunkZ The z-coords value of the chunk.
     * @return The stacked blocks in the provided chunk.
     */
    Map<Location, Integer> removeStackedBlocks(World world, int chunkX, int chunkZ);

    /**
     * Get all the stacked blocks in a specific chunk.
     *
     * @param chunk The chunk to get stacked blocks from.
     */
    Map<Location, Integer> getStackedBlocks(Chunk chunk);

    /**
     * Get all the stacked blocks in a specific chunk.
     *
     * @param world  The world of the chunk.
     * @param chunkX The x-coords value of the chunk.
     * @param chunkZ The z-coords value of the chunk.
     */
    Map<Location, Integer> getStackedBlocks(World world, int chunkX, int chunkZ);

    /**
     * Get all the stacked blocks on the server.
     */
    Map<Location, Integer> getStackedBlocks();

    /**
     * Update the hologram of a stacked block.
     *
     * @param location The location of the stacked block.
     */
    void updateStackedBlockHologram(Location location);

    /**
     * Update the holograms of stacked-blocks in a specific chunk.
     *
     * @param chunk The chunk to update holograms in.
     */
    void updateStackedBlockHolograms(Chunk chunk);

    /**
     * Remove the hologram of a stacked block.
     *
     * @param location The location of the stacked block.
     */
    void removeStackedBlockHologram(Location location);

    /**
     * Remove the holograms of stacked-blocks in a specific chunk.
     *
     * @param chunk The chunk to update holograms in.
     */
    void removeStackedBlockHolograms(Chunk chunk);

}

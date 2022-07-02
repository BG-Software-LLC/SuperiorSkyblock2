package com.bgsoftware.superiorskyblock.api.island;

import com.bgsoftware.superiorskyblock.api.data.IDatabaseBridgeHolder;
import com.bgsoftware.superiorskyblock.api.island.level.IslandLoadLevel;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface IslandBase extends IDatabaseBridgeHolder {

    /*
     *  General related methods
     */

    /**
     * Get the owner of the island.
     */
    SuperiorPlayer getOwner();

    /**
     * Get the unique-id of the island.
     */
    UUID getUniqueId();

    /**
     * Get the creation time of the island.
     */
    long getCreationTime();

    /**
     * Get the creation time of the island, in a formatted string.
     */
    String getCreationTimeDate();

    /**
     * Re-sync the island with a new dates formatter.
     */
    void updateDatesFormatter();

    /**
     * Get the last time the island was updated.
     */
    long getLastTimeUpdate();

    /**
     * Checks whether the island is the spawn island.
     */
    boolean isSpawn();

    /**
     * Get the name of the island.
     */
    String getName();

    /**
     * Set the name of the island.
     *
     * @param islandName The name to set.
     */
    void setName(String islandName);

    /**
     * Get the name of the island, unformatted.
     */
    String getRawName();

    /**
     * Load this island with a specific load level.
     *
     * @param loadLevel The load level to load the island.
     * @param <T>       The type of the island.
     */
    <T extends IslandBase> T loadIsland(IslandLoadLevel<T> loadLevel);

    /*
     * Location related methods
     */

    /**
     * Get the center location of the island, depends on the world environment.
     *
     * @param environment The environment.
     */
    Location getCenter(World.Environment environment);

    /**
     * Get the island radius of the island.
     */
    int getIslandSize();

    /**
     * Set the radius of the island.
     *
     * @param islandSize The radius for the island.
     */
    void setIslandSize(int islandSize);

    /**
     * Get the island radius of the island that was set with a command.
     */
    int getIslandSizeRaw();

    /**
     * Get the minimum location of the island.
     */
    Location getMinimum();

    /**
     * Get the minimum protected location of the island.
     */
    Location getMinimumProtected();

    /**
     * Get the maximum location of the island.
     */
    Location getMaximum();

    /**
     * Get the minimum protected location of the island.
     */
    Location getMaximumProtected();

    /**
     * Get all the chunks of the island from all the environments.
     */
    List<Chunk> getAllChunks();

    /**
     * Get all the chunks of the island from all the environments.
     *
     * @param onlyProtected Whether only chunks inside the protected area should be returned.
     */
    List<Chunk> getAllChunks(boolean onlyProtected);

    /**
     * Get all the chunks of the island.
     *
     * @param environment The environment to get the chunks from.
     */
    List<Chunk> getAllChunks(World.Environment environment);

    /**
     * Get all the chunks of the island, including empty ones.
     *
     * @param environment   The environment to get the chunks from.
     * @param onlyProtected Whether only chunks inside the protected area should be returned.
     */
    List<Chunk> getAllChunks(World.Environment environment, boolean onlyProtected);

    /**
     * Get all the chunks of the island.
     *
     * @param environment   The environment to get the chunks from.
     * @param onlyProtected Whether only chunks inside the protected area should be returned.
     * @param noEmptyChunks Should empty chunks be loaded or not?
     */
    List<Chunk> getAllChunks(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks);

    /**
     * Get all the loaded chunks of the island.
     *
     * @param onlyProtected Whether only chunks inside the protected area should be returned.
     * @param noEmptyChunks Should empty chunks be loaded or not?
     */
    List<Chunk> getLoadedChunks(boolean onlyProtected, boolean noEmptyChunks);

    /**
     * Get all the loaded chunks of the island.
     *
     * @param environment   The environment to get the chunks from.
     * @param onlyProtected Whether only chunks inside the protected area should be returned.
     * @param noEmptyChunks Should empty chunks be loaded or not?
     */
    List<Chunk> getLoadedChunks(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks);

    /**
     * Get all the chunks of the island asynchronized, including empty chunks.
     *
     * @param environment   The environment to get the chunks from.
     * @param onlyProtected Whether only chunks inside the protected area should be returned.
     * @param onChunkLoad   A consumer that will be ran when the chunk is loaded. Can be null.
     */
    List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected, @Nullable Consumer<Chunk> onChunkLoad);

    /**
     * Get all the chunks of the island asynchronized.
     *
     * @param environment   The environment to get the chunks from.
     * @param onlyProtected Whether only chunks inside the protected area should be returned.
     * @param noEmptyChunks Should empty chunks be loaded or not?
     * @param onChunkLoad   A consumer that will be ran when the chunk is loaded. Can be null.
     */
    List<CompletableFuture<Chunk>> getAllChunksAsync(World.Environment environment, boolean onlyProtected, boolean noEmptyChunks, @Nullable Consumer<Chunk> onChunkLoad);

    /**
     * Check if the location is inside the island's area.
     *
     * @param location The location to check.
     */
    boolean isInside(Location location);

    /**
     * Check if the location is inside the island's protected area.
     *
     * @param location The location to check.
     */
    boolean isInsideRange(Location location);

    /**
     * Check if the location is inside the island's protected area.
     *
     * @param location    The location to check.
     * @param extraRadius Add extra radius to the protected range.
     */
    boolean isInsideRange(Location location, int extraRadius);

    /**
     * Check if the chunk is inside the island's protected area.
     *
     * @param chunk The chunk to check.
     */
    boolean isInsideRange(Chunk chunk);

    /*
     * Schematics related methods
     */

    /**
     * Checks if a schematic was generated already.
     *
     * @param environment The environment to check.
     */
    boolean wasSchematicGenerated(World.Environment environment);

    /**
     * Set schematic generated flag to true.
     *
     * @param environment The environment to set.
     */
    void setSchematicGenerate(World.Environment environment);

    /**
     * Set schematic generated flag.
     *
     * @param environment The environment to set.
     * @param generated   The flag to set.
     */
    void setSchematicGenerate(World.Environment environment, boolean generated);

    /**
     * Get the generated schematics flag.
     */
    int getGeneratedSchematicsFlag();

    /*
     * Top-islands related methods
     */

    /**
     * Checks whether the island is ignored in the top islands.
     */
    boolean isIgnored();

    /**
     * Set whether the island should be ignored in the top islands.
     */
    void setIgnored(boolean ignored);

    /**
     * Get the position of the island.
     * Positions are starting from 0.
     *
     * @param sortingType The sorting type that should be considered.
     * @return The position of the island.
     */
    int getPosition(SortingType sortingType);

}

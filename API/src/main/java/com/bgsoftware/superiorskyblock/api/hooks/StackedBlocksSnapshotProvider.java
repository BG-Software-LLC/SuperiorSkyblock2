package com.bgsoftware.superiorskyblock.api.hooks;


import org.bukkit.Chunk;
import org.bukkit.World;

/**
 * StackedBlocksProvider based on snapshots, similar concept to ChunkSnapshot from Bukkit.
 * The plugin will take a snapshot when calculating a chunk, and will release it once it's done
 * calculation of that chunk. Snapshots are taken sync, however reading them is done async.
 * Thread-safety must be implemented in order to not get weird issues.
 */
public interface StackedBlocksSnapshotProvider extends StackedBlocksProvider {

    /**
     * Take a snapshot of a chunk.
     *
     * @param chunk The chunk to take a snapshot of.
     */
    void takeSnapshot(Chunk chunk);

    /**
     * Release a snapshot of a chunk.
     *
     * @param world  The world of the chunk.
     * @param chunkX The x-coords of the chunk.
     * @param chunkZ The z-coords of the chunk.
     */
    void releaseSnapshot(World world, int chunkX, int chunkZ);

}

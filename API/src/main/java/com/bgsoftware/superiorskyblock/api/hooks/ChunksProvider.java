package com.bgsoftware.superiorskyblock.api.hooks;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.concurrent.CompletableFuture;

public interface ChunksProvider {

    /**
     * Load a chunk in the world.
     *
     * @param world  The world to load chunk from.
     * @param chunkX X-coords of the chunk.
     * @param chunkZ Z-coords of the chunk.
     * @return CompletableFuture of the chunk instance.
     */
    CompletableFuture<Chunk> loadChunk(World world, int chunkX, int chunkZ);

}

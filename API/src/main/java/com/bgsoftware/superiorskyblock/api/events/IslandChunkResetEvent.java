package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.World;

/**
 * IslandChunkResetEvent is called when a chunk is reset inside an island.
 */
public class IslandChunkResetEvent extends IslandEvent {

    private final World world;
    private final int chunkX;
    private final int chunkZ;

    /**
     * The constructor of the event.
     *
     * @param island The island that the chunk was reset in.
     * @param world  The world of the chunk.
     * @param chunkX The x-coords of the chunk.
     * @param chunkZ The z-coords of the chunk.
     */
    public IslandChunkResetEvent(Island island, World world, int chunkX, int chunkZ) {
        super(island);
        this.world = world;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    /**
     * Get the world of the chunk.
     */
    public World getWorld() {
        return world;
    }

    /**
     * Get the x-coords of the chunk.
     */
    public int getChunkX() {
        return chunkX;
    }

    /**
     * Get the z-coords of the chunk.
     */
    public int getChunkZ() {
        return chunkZ;
    }

}

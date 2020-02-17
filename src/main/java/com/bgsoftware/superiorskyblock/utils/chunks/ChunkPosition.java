package com.bgsoftware.superiorskyblock.utils.chunks;

import org.bukkit.Chunk;
import org.bukkit.World;

public final class ChunkPosition {

    private World world;
    private int x, z;

    private ChunkPosition(World world, int x, int z){
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public Chunk loadChunk(){
        return world.getChunkAt(x, z);
    }

    public World getWorld() {
        return world;
    }

    public static ChunkPosition of(World world, int x, int z){
        return new ChunkPosition(world, x, z);
    }

}

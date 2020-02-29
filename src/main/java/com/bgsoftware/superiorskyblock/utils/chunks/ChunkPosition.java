package com.bgsoftware.superiorskyblock.utils.chunks;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.Objects;

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

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkPosition that = (ChunkPosition) o;
        return x == that.x &&
                z == that.z &&
                world.equals(that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, z);
    }

    @Override
    public String toString() {
        return "ChunkPosition{" +
                "world=" + world.getName() +
                ", x=" + x +
                ", z=" + z +
                '}';
    }

    public static ChunkPosition of(World world, int x, int z){
        return new ChunkPosition(world, x, z);
    }

}

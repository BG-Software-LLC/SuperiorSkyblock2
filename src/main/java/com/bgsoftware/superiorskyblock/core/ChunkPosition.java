package com.bgsoftware.superiorskyblock.core;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Objects;

public class ChunkPosition {

    private final String worldName;
    private final int x;
    private final int z;

    private ChunkPosition(String worldName, int x, int z) {
        this.worldName = worldName;
        this.x = x;
        this.z = z;
    }

    public static ChunkPosition of(Block block) {
        return of(block.getLocation());
    }

    public static ChunkPosition of(Location location) {
        return of(LazyWorldLocation.getWorldName(location), location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public static ChunkPosition of(Chunk chunk) {
        return of(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public static ChunkPosition of(SBlockPosition blockPosition) {
        return of(blockPosition.getWorldName(), blockPosition.getX() >> 4, blockPosition.getZ() >> 4);
    }

    public static ChunkPosition of(World world, int x, int z) {
        return of(world.getName(), x, z);
    }

    public static ChunkPosition of(String worldName, int x, int z) {
        return new ChunkPosition(worldName, x, z);
    }

    public Chunk loadChunk() {
        return getWorld().getChunkAt(x, z);
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    public String getWorldName() {
        return worldName;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public boolean isInsideChunk(Location location) {
        return location.getWorld().getName().equals(worldName) && location.getBlockX() >> 4 == x && location.getBlockZ() >> 4 == z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName, x, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkPosition that = (ChunkPosition) o;
        return x == that.x &&
                z == that.z &&
                worldName.equals(that.worldName);
    }

    @Override
    public String toString() {
        return worldName + ", " + x + ", " + z;
    }

}

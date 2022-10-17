package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Objects;

public class ChunkPosition {

    private final WorldInfo worldInfo;
    private final int x;
    private final int z;

    private long pairedXZ = -1;

    private ChunkPosition(WorldInfo worldInfo, int x, int z) {
        this.worldInfo = worldInfo;
        this.x = x;
        this.z = z;
    }

    public static ChunkPosition of(Block block) {
        return of(WorldInfo.of(block.getWorld()), block.getX() >> 4, block.getZ() >> 4);
    }

    public static ChunkPosition of(Location location) {
        return of(WorldInfo.of(location.getWorld()), location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public static ChunkPosition of(Chunk chunk) {
        return of(WorldInfo.of(chunk.getWorld()), chunk.getX(), chunk.getZ());
    }

    public static ChunkPosition of(World world, int x, int z) {
        return of(WorldInfo.of(world), x, z);
    }

    public static ChunkPosition of(WorldInfo worldInfo, int x, int z) {
        return new ChunkPosition(worldInfo, x, z);
    }

    public World getWorld() {
        return Bukkit.getWorld(getWorldName());
    }

    public WorldInfo getWorldsInfo() {
        return this.worldInfo;
    }

    public String getWorldName() {
        return this.worldInfo.getName();
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public long asPair() {
        if (this.pairedXZ < 0)
            pairedXZ = (long) this.x & 4294967295L | ((long) this.z & 4294967295L) << 32;

        return pairedXZ;
    }

    public boolean isInsideChunk(Location location) {
        return location.getWorld().getName().equals(worldInfo.getName()) &&
                location.getBlockX() >> 4 == x && location.getBlockZ() >> 4 == z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldInfo.getName(), x, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkPosition that = (ChunkPosition) o;
        return x == that.x &&
                z == that.z &&
                worldInfo.equals(that.worldInfo);
    }

    @Override
    public String toString() {
        return worldInfo.getName() + ", " + x + ", " + z;
    }

}

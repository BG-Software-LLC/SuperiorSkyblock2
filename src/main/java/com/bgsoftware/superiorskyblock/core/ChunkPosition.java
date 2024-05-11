package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Objects;
import java.util.Optional;

public class ChunkPosition {

    private final WorldInfo worldInfo;
    private final int x;
    private final int z;

    private long pairedXZ = -1;
    @Nullable
    private World cachedBukkitWorld;

    private ChunkPosition(WorldInfo worldInfo, int x, int z) {
        this.worldInfo = worldInfo;
        this.x = x;
        this.z = z;
    }

    public static ChunkPosition of(Block block) {
        World world = block.getWorld();
        return of(WorldInfo.of(world), block.getX() >> 4, block.getZ() >> 4).withBukkitWorld(world);
    }

    public static ChunkPosition of(Location location) {
        World world = location.getWorld();
        return of(WorldInfo.of(world), location.getBlockX() >> 4, location.getBlockZ() >> 4).withBukkitWorld(world);
    }

    public static ChunkPosition of(Chunk chunk) {
        World world = chunk.getWorld();
        return of(WorldInfo.of(world), chunk.getX(), chunk.getZ()).withBukkitWorld(world);
    }

    public static ChunkPosition of(World world, int x, int z) {
        return of(WorldInfo.of(world), x, z).withBukkitWorld(world);
    }

    public static ChunkPosition of(WorldInfo worldInfo, int x, int z) {
        return new ChunkPosition(worldInfo, x, z);
    }

    public World getWorld() {
        return this.cachedBukkitWorld == null ? (this.cachedBukkitWorld = Bukkit.getWorld(getWorldName())) : this.cachedBukkitWorld;
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

    public int distanceSquared(ChunkPosition other) {
        int deltaX = this.x - other.x;
        int deltaZ = this.z - other.z;
        return (deltaX * deltaX) + (deltaZ * deltaZ);
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
                worldInfo.getName().equals(that.worldInfo.getName());
    }

    @Override
    public String toString() {
        return worldInfo.getName() + ", " + x + ", " + z;
    }

    private ChunkPosition withBukkitWorld(World world) {
        this.cachedBukkitWorld = world;
        return this;
    }

    public static Optional<Chunk> getLoadedChunk(ChunkPosition chunkPosition) {
        boolean isChunkLoaded = chunkPosition.getWorld().isChunkLoaded(chunkPosition.getX(), chunkPosition.getZ());
        if (!isChunkLoaded) return Optional.empty();
        return Optional.of(chunkPosition.getWorld().getChunkAt(chunkPosition.getX(), chunkPosition.getZ()));
    }

}

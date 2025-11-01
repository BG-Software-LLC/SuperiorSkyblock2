package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.WorldPosition;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Optional;

public class ChunkPosition implements ObjectsPool.Releasable, AutoCloseable {

    private static final ObjectsPool<ChunkPosition> POOL = new ObjectsPool<>(ChunkPosition::new);

    protected WorldInfo worldInfo;
    protected int x;
    protected int z;

    protected long pairedXZ = -1;
    @Nullable
    protected WeakReference<World> cachedBukkitWorld = new WeakReference<>(null);
    private final boolean isPool;

    public static ChunkPosition of(Location location) {
        World world = location.getWorld();
        return of(WorldInfo.of(world), location.getBlockX() >> 4, location.getBlockZ() >> 4).withBukkitWorld(world);
    }

    public static ChunkPosition of(WorldInfo worldInfo, WorldPosition worldPosition) {
        BlockPosition blockPosition = worldPosition.toBlockPosition();
        return of(worldInfo, blockPosition.getX() >> 4, blockPosition.getZ() >> 4);
    }

    public static ChunkPosition of(Chunk chunk) {
        return of(chunk, true);
    }

    public static ChunkPosition of(Chunk chunk, boolean fromPool) {
        World world = chunk.getWorld();
        return of(WorldInfo.of(world), chunk.getX(), chunk.getZ(), fromPool).withBukkitWorld(world);
    }

    public static ChunkPosition of(World world, int x, int z) {
        return of(world, x, z, true);
    }

    public static ChunkPosition of(World world, int x, int z, boolean fromPool) {
        return of(WorldInfo.of(world), x, z, fromPool).withBukkitWorld(world);
    }

    public static ChunkPosition of(WorldInfo worldInfo, int x, int z) {
        return of(worldInfo, x, z, true);
    }

    public static ChunkPosition of(WorldInfo worldInfo, int x, int z, boolean fromPool) {
        ChunkPosition chunkPosition = fromPool ? POOL.obtain() : new ChunkPosition(false);
        return chunkPosition.initialize(worldInfo, x, z);
    }

    public static ChunkPosition of(IslandWarp islandWarp) {
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            return of(islandWarp.getLocation(wrapper.getHandle()));
        }
    }

    protected ChunkPosition() {
        this(true);
    }

    protected ChunkPosition(boolean isPool) {
        this.isPool = isPool;
    }

    private ChunkPosition initialize(WorldInfo worldInfo, int x, int z) {
        this.worldInfo = worldInfo;
        this.x = x;
        this.z = z;
        return this;
    }

    public World getWorld() {
        World cachedBukkitWorld = this.cachedBukkitWorld.get();
        if (cachedBukkitWorld == null) {
            cachedBukkitWorld = Bukkit.getWorld(getWorldName());
            this.cachedBukkitWorld = new WeakReference<>(cachedBukkitWorld);
        }

        return cachedBukkitWorld;
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
    public void release() {
        if (!isPool)
            return;

        this.worldInfo = null;
        this.pairedXZ = -1;
        this.cachedBukkitWorld.clear();
        POOL.release(this);
    }

    public ChunkPosition copy() {
        return new ChunkPosition(false).initialize(this.worldInfo, this.x, this.z);
    }

    @Override
    public void close() {
        release();
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
        this.cachedBukkitWorld = new WeakReference<>(world);
        return this;
    }

    public static Optional<Chunk> getLoadedChunk(ChunkPosition chunkPosition) {
        boolean isChunkLoaded = chunkPosition.getWorld().isChunkLoaded(chunkPosition.getX(), chunkPosition.getZ());
        if (!isChunkLoaded) return Optional.empty();
        return Optional.of(chunkPosition.getWorld().getChunkAt(chunkPosition.getX(), chunkPosition.getZ()));
    }

}

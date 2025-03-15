package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
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

    private Thread holder = Thread.currentThread();
    private StackTraceElement[] releaseStackTrace;

    public static ChunkPosition of(Location location) {
        World world = location.getWorld();
        return of(WorldInfo.of(world), location.getBlockX() >> 4, location.getBlockZ() >> 4).withBukkitWorld(world);
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
        this.holder = Thread.currentThread();
        return this;
    }

    private void checkAccess() {
        if (isPool && Thread.currentThread() != this.holder) {
            StringBuilder builder = new StringBuilder("Accessed ChunkPosition from " + Thread.currentThread() + " but holder is " + this.holder);
            if (this.releaseStackTrace != null && this.releaseStackTrace.length > 0) {
                builder.append("\n\n\tRelease stacktrace:");
                for (StackTraceElement traceElement : this.releaseStackTrace) {
                    builder.append("\n\t\tat ").append(traceElement);
                }
                builder.append("\n");
            }
            throw new RuntimeException(builder.toString());
        }
    }

    public World getWorld() {
        checkAccess();

        World cachedBukkitWorld = this.cachedBukkitWorld.get();
        if (cachedBukkitWorld == null) {
            cachedBukkitWorld = Bukkit.getWorld(getWorldName());
            this.cachedBukkitWorld = new WeakReference<>(cachedBukkitWorld);
        }

        return cachedBukkitWorld;
    }

    public WorldInfo getWorldsInfo() {
        checkAccess();
        return this.worldInfo;
    }

    public String getWorldName() {
        checkAccess();
        return this.worldInfo.getName();
    }

    public int getX() {
        checkAccess();
        return x;
    }

    public int getZ() {
        checkAccess();
        return z;
    }

    public long asPair() {
        checkAccess();
        if (this.pairedXZ < 0)
            pairedXZ = (long) this.x & 4294967295L | ((long) this.z & 4294967295L) << 32;

        return pairedXZ;
    }

    public boolean isInsideChunk(Location location) {
        checkAccess();
        return location.getWorld().getName().equals(worldInfo.getName()) &&
                location.getBlockX() >> 4 == x && location.getBlockZ() >> 4 == z;
    }

    public int distanceSquared(ChunkPosition other) {
        checkAccess();
        int deltaX = this.x - other.x;
        int deltaZ = this.z - other.z;
        return (deltaX * deltaX) + (deltaZ * deltaZ);
    }

    @Override
    public void release() {
        if (!isPool)
            return;

        checkAccess();

        this.worldInfo = null;
        this.pairedXZ = -1;
        this.cachedBukkitWorld.clear();
        this.holder = null;
        this.releaseStackTrace = Thread.currentThread().getStackTrace();
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
        checkAccess();
        this.cachedBukkitWorld = new WeakReference<>(world);
        return this;
    }

    public static Optional<Chunk> getLoadedChunk(ChunkPosition chunkPosition) {
        boolean isChunkLoaded = chunkPosition.getWorld().isChunkLoaded(chunkPosition.getX(), chunkPosition.getZ());
        if (!isChunkLoaded) return Optional.empty();
        return Optional.of(chunkPosition.getWorld().getChunkAt(chunkPosition.getX(), chunkPosition.getZ()));
    }

}

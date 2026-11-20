package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.WorldPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class MutableBlockPosition implements BlockPosition, ObjectsPool.Releasable, AutoCloseable {

    public static final ObjectsPool<MutableBlockPosition> POOL = new ObjectsPool<>(MutableBlockPosition::new);

    private int x;
    private int y;
    private int z;

    private MutableBlockPosition() {

    }

    public MutableBlockPosition set(Location location) {
        return set(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public MutableBlockPosition set(Block block) {
        return set(block.getX(), block.getY(), block.getZ());
    }

    public MutableBlockPosition set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getZ() {
        return this.z;
    }

    @Override
    public BlockPosition offset(int x, int y, int z) {
        return SBlockPosition.of(this.x + x, this.y + y, this.z + z);
    }

    @Override
    public Location toLocation(@Nullable World world) {
        return new Location(world, this.x, this.y, this.z);
    }

    @Override
    public Location toLocation(@Nullable World world, @Nullable Location location) {
        if (location != null) {
            location.setWorld(world);
            location.setX(this.x);
            location.setY(this.y);
            location.setZ(this.z);
            location.setYaw(0f);
            location.setPitch(0f);
        }

        return location;
    }

    @Override
    public Location toLocation(WorldInfo worldInfo) {
        return LazyWorldLocation.of(worldInfo, this);
    }

    @Override
    public Location toLocation(WorldInfo worldInfo, @Nullable Location location) {
        if (location != null) {
            location.setX(this.x);
            location.setY(this.y);
            location.setZ(this.z);
            location.setYaw(0f);
            location.setPitch(0f);

            if (location instanceof LazyWorldLocation) {
                ((LazyWorldLocation) location).setWorldName(worldInfo.getName());
            } else {
                World world = Bukkit.getWorld(worldInfo.getName());
                location.setWorld(world);
            }
        }

        return location;
    }

    @Override
    public WorldPosition toWorldPosition() {
        // Never cache: this object is mutable and reused across lookups.
        return SWorldPosition.of(this);
    }

    @Override
    public void release() {
        POOL.release(this);
    }

    @Override
    public void close() {
        release();
    }

}

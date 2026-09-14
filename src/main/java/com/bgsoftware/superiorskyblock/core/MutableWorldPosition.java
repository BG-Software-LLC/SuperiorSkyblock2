package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.WorldPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.function.Consumer;

/**
 * A mutable, pooled {@link WorldPosition} used to avoid allocating a new position per lookup on
 * hot paths (e.g. {@code getIslandAt}). Obtain an instance from {@link ObjectsPools#WORLD_POSITION},
 * set its coordinates and release it back (preferably via try-with-resources).
 */
public class MutableWorldPosition implements WorldPosition, ObjectsPool.Releasable, AutoCloseable {

    private final Consumer<MutableWorldPosition> releaseMethod;

    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    MutableWorldPosition(Consumer<MutableWorldPosition> releaseMethod) {
        this.releaseMethod = releaseMethod;
    }

    public MutableWorldPosition set(double x, double y, double z) {
        return set(x, y, z, 0f, 0f);
    }

    public MutableWorldPosition set(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        return this;
    }

    @Override
    public double getX() {
        return this.x;
    }

    @Override
    public double getY() {
        return this.y;
    }

    @Override
    public double getZ() {
        return this.z;
    }

    @Override
    public float getYaw() {
        return this.yaw;
    }

    @Override
    public float getPitch() {
        return this.pitch;
    }

    @Override
    public WorldPosition offset(double x, double y, double z) {
        return SWorldPosition.of(this.x + x, this.y + y, this.z + z, this.yaw, this.pitch);
    }

    @Override
    public WorldPosition rotate(float yaw, float pitch) {
        return SWorldPosition.of(this.x, this.y, this.z, this.yaw + yaw, this.pitch + pitch);
    }

    @Override
    public WorldPosition offset(double x, double y, double z, float yaw, float pitch) {
        return SWorldPosition.of(this.x + x, this.y + y, this.z + z, this.yaw + yaw, this.pitch + pitch);
    }

    @Override
    public Location toLocation(@Nullable World world) {
        return new Location(world, this.x, this.y, this.z, this.yaw, this.pitch);
    }

    @Override
    public Location toLocation(@Nullable World world, @Nullable Location location) {
        if (location != null) {
            location.setWorld(world);
            location.setX(this.x);
            location.setY(this.y);
            location.setZ(this.z);
            location.setYaw(this.yaw);
            location.setPitch(this.pitch);
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
            location.setYaw(this.yaw);
            location.setPitch(this.pitch);

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
    public BlockPosition toBlockPosition() {
        // Never cache: this object is mutable and reused across lookups.
        return SBlockPosition.of(this);
    }

    @Override
    public void release() {
        this.releaseMethod.accept(this);
    }

    @Override
    public void close() {
        release();
    }

}

package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.WorldPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Objects;

public class SWorldPosition implements WorldPosition {

    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    @Nullable
    private BlockPosition cachedBlockPosition;

    public static SWorldPosition of(Location location) {
        return of(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public static SWorldPosition of(Block block) {
        return of(block.getX(), block.getY(), block.getZ());
    }

    public static SWorldPosition of(double x, double y, double z) {
        return of(x, y, z, 0f, 0f);
    }

    public static SWorldPosition of(BlockPosition blockPosition) {
        return of(blockPosition.getX() + 0.5, blockPosition.getY(), blockPosition.getZ() + 0.5);
    }

    public static SWorldPosition of(double x, double y, double z, float yaw, float pitch) {
        return new SWorldPosition(x, y, z, yaw, pitch);
    }

    private SWorldPosition(double x, double y, double z, float yaw, float pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
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
        return of(this.x + x, this.y + y, this.z + z, this.yaw, this.pitch);
    }

    @Override
    public WorldPosition rotate(float yaw, float pitch) {
        return of(this.x, this.y, this.z, this.yaw + yaw, this.pitch + pitch);
    }

    @Override
    public WorldPosition offset(double x, double y, double z, float yaw, float pitch) {
        return of(this.x + x, this.y + y, this.z + z, this.yaw + yaw, this.pitch + pitch);
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
        if (this.cachedBlockPosition == null)
            this.cachedBlockPosition = SBlockPosition.of(this);

        return this.cachedBlockPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SWorldPosition that = (SWorldPosition) o;
        return x == that.x && y == that.y && z == that.z && yaw == that.yaw && pitch == that.pitch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, yaw, pitch);
    }

    @Override
    public String toString() {
        return x + ", " + y + ", " + z + ", " + yaw + ", " + pitch;
    }

}

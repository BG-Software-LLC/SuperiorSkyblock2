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

public class SBlockPosition implements BlockPosition {

    private final int x;
    private final int y;
    private final int z;

    @Nullable
    private WorldPosition cachedWorldPosition;

    public static SBlockPosition of(Location location) {
        return of(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static SBlockPosition of(Block block) {
        return of(block.getX(), block.getY(), block.getZ());
    }

    public static SBlockPosition of(WorldPosition worldPosition) {
        return new SBlockPosition(Location.locToBlock(worldPosition.getX()),
                Location.locToBlock(worldPosition.getY()), Location.locToBlock(worldPosition.getZ()));
    }

    public static SBlockPosition of(int x, int y, int z) {
        return new SBlockPosition(x, y, z);
    }

    private SBlockPosition(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getZ() {
        return z;
    }

    @Override
    public BlockPosition offset(int x, int y, int z) {
        return new SBlockPosition(this.x + x, this.y + y, this.z + z);
    }

    @Override
    public Location toLocation(@Nullable World world) {
        return new Location(world, x, y, z);
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
        if (this.cachedWorldPosition == null)
            this.cachedWorldPosition = SWorldPosition.of(this);

        return this.cachedWorldPosition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SBlockPosition that = (SBlockPosition) o;
        return x == that.x && y == that.y && z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public String toString() {
        return x + ", " + y + ", " + z;
    }

}

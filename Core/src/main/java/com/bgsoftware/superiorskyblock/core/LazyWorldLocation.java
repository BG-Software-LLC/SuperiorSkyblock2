package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.WorldPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * LazyWorldLocation will update the world again if it's null on initialize.
 */
public class LazyWorldLocation extends Location {

    @Nullable
    private String worldName;
    private boolean updatedWorld = false;

    public static LazyWorldLocation of(Location location) {
        if (location instanceof LazyWorldLocation)
            return (LazyWorldLocation) ((LazyWorldLocation) location).clone(true);

        return new LazyWorldLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch());
    }

    public static LazyWorldLocation of(WorldInfo worldInfo, BlockPosition blockPosition) {
        return new LazyWorldLocation(worldInfo.getName(), blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(),
                0f, 0f);
    }

    public static LazyWorldLocation of(WorldInfo worldInfo, WorldPosition worldPosition) {
        return new LazyWorldLocation(worldInfo.getName(), worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                worldPosition.getYaw(), worldPosition.getPitch());
    }

    public LazyWorldLocation(World world, double x, double y, double z) {
        super(world, x, y, z);
        this.worldName = null;
        this.updatedWorld = false;
    }

    public LazyWorldLocation(String worldName, double x, double y, double z, float yaw, float pitch) {
        super(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
        this.worldName = worldName;
        this.updatedWorld = false;
    }

    public String getWorldName() {
        return this.worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
        this.updatedWorld = false;
    }

    @Override
    public World getWorld() {
        if (!this.updatedWorld) {
            updateWorldInternal(worldName == null ? null : Bukkit.getWorld(worldName));
        }

        return super.getWorld();
    }

    @Override
    public void setWorld(World world) {
        updateWorldInternal(world);
        this.worldName = world == null ? null : world.getName();
    }

    private void updateWorldInternal(@Nullable World world) {
        super.setWorld(world);
        this.updatedWorld = true;
    }

    @Override
    public Location clone() {
        return clone(false);
    }

    public Location clone(boolean keepLazy) {
        return keepLazy || getWorld() == null ? new LazyWorldLocation(this.worldName, getX(), getY(), getZ(), getYaw(), getPitch()) :
                super.clone();
    }

    public static String getWorldName(Location location) {
        if (location instanceof LazyWorldLocation)
            return ((LazyWorldLocation) location).getWorldName();

        World world = location.getWorld();
        return world == null ? "null" : world.getName();
    }

}

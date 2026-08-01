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

    /**
     * Incremented every time a world is unloaded. Cached world references that were resolved
     * with an older generation must be resolved again, as they may point to an unloaded world.
     */
    private static long worldsGeneration = Long.MIN_VALUE;

    @Nullable
    private String worldName;
    private boolean updatedWorld = false;
    private long cachedWorldsGeneration = Long.MIN_VALUE;

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
        if (!this.updatedWorld || this.cachedWorldsGeneration != worldsGeneration) {
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
        this.cachedWorldsGeneration = worldsGeneration;
    }

    @Override
    public Location clone() {
        return clone(false);
    }

    public Location clone(boolean keepLazy) {
        return keepLazy || getWorld() == null ? new LazyWorldLocation(this.worldName, getX(), getY(), getZ(), getYaw(), getPitch()) :
                super.clone();
    }

    /**
     * Invalidates all cached world references of lazy locations.
     * Must be called whenever a world is unloaded, as accessing blocks or chunks of an unloaded
     * world throws an exception - its chunk-system is shut down.
     */
    public static void notifyWorldUnloaded() {
        ++worldsGeneration;
    }

    /**
     * Checks whether the given world is currently loaded on the server.
     * A reference of an unloaded world may still be held by objects, but must never be accessed.
     */
    public static boolean isWorldLoaded(@Nullable World world) {
        return world != null && Bukkit.getWorld(world.getName()) == world;
    }

    public static String getWorldName(Location location) {
        if (location instanceof LazyWorldLocation)
            return ((LazyWorldLocation) location).getWorldName();

        World world = location.getWorld();
        return world == null ? "null" : world.getName();
    }

}

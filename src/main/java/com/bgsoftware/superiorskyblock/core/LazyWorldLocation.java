package com.bgsoftware.superiorskyblock.core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * LazyWorldLocation will update the world again if it's null on initialize.
 */
public class LazyWorldLocation extends Location {

    private final String worldName;

    public static LazyWorldLocation of(Location location) {
        if (location instanceof LazyWorldLocation)
            return (LazyWorldLocation) ((LazyWorldLocation) location).clone(true);

        return new LazyWorldLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch());
    }

    public LazyWorldLocation(String worldName, double x, double y, double z, float yaw, float pitch) {
        super(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
        this.worldName = worldName;
    }

    @Override
    public World getWorld() {
        if (worldName != null)
            setWorld(Bukkit.getWorld(worldName));

        return super.getWorld();
    }

    @Override
    public Location clone() {
        return clone(false);
    }

    public Location clone(boolean keepLazy) {
        return keepLazy || getWorld() == null ? new LazyWorldLocation(this.worldName, getX(), getY(), getZ(), getYaw(), getPitch()) :
                super.clone();
    }

    public String getWorldName() {
        return worldName;
    }

    public static String getWorldName(Location location) {
        if (location instanceof LazyWorldLocation)
            return ((LazyWorldLocation) location).getWorldName();

        World world = location.getWorld();
        return world == null ? "null" : world.getName();
    }

}

package com.bgsoftware.superiorskyblock.core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * LazyWorldLocation will update the world again if it's null on initialize.
 */
public class LazyWorldLocation extends Location {

    private final String worldName;

    public LazyWorldLocation(String worldName, double x, double y, double z, float pitch, float yaw) {
        super(Bukkit.getWorld(worldName), x, y, z, pitch, yaw);
        this.worldName = worldName;
    }

    @Override
    public World getWorld() {
        if (worldName != null)
            setWorld(Bukkit.getWorld(worldName));

        return super.getWorld();
    }

    public String getWorldName() {
        return worldName;
    }

    public static String getWorldName(Location location) {
        return location instanceof LazyWorldLocation ? ((LazyWorldLocation) location).getWorldName() :
                location.getWorld().getName();
    }

}

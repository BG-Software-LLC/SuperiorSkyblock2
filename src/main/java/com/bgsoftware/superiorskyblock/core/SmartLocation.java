package com.bgsoftware.superiorskyblock.core;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Smart location will update the world again if it's null on initialize.
 */
public class SmartLocation extends Location {

    private String worldName;

    public SmartLocation(String worldName, double x, double y, double z, float pitch, float yaw) {
        super(Bukkit.getWorld(worldName), x, y, z, pitch, yaw);
        this.worldName = super.getWorld() == null ? worldName : null;
    }

    @Override
    public World getWorld() {
        if (worldName != null)
            setWorld(Bukkit.getWorld(worldName));

        return super.getWorld();
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        if (world != null)
            worldName = null;
    }

    public String getWorldName() {
        return worldName;
    }

}

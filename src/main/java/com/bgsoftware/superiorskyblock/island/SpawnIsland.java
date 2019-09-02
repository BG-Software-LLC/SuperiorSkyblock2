package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.Location;


public final class SpawnIsland extends SIsland {

    private static SuperiorSkyblockPlugin plugin;

    private Location center;
    private String world;

    public SpawnIsland(SuperiorSkyblockPlugin plugin) {
        super(null, SBlockPosition.of(plugin.getSettings().spawnLocation), "");
        SpawnIsland.plugin = plugin;

        String[] loc = plugin.getSettings().spawnLocation.split(", ");
        this.world = loc[0];
        double x = ((int) Double.parseDouble(loc[1])) + 0.5;
        double y = Integer.parseInt(loc[2]);
        double z = ((int) Double.parseDouble(loc[3])) + 0.5;
        float yaw = loc.length == 6 ? Float.parseFloat(loc[4]) : 0;
        float pitch = loc.length == 6 ? Float.parseFloat(loc[5]) : 0;
        this.center = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }

    @Override
    public Location getCenter() {
        if(center.getWorld() == null)
            center.setWorld(Bukkit.getWorld(world));

        return center.clone();
    }

    @Override
    public Location getTeleportLocation() {
        return getCenter();
    }

    @Override
    public boolean isMember(SuperiorPlayer superiorPlayer) {
        return false;
    }

    @Override
    public SSuperiorPlayer getOwner() {
        return null;
    }

    @Override
    public boolean isSpawn() {
        return true;
    }

    @Override
    public int getIslandSize() {
        return plugin.getSettings().maxIslandSize;
    }

    @Override
    public boolean hasPermission(SuperiorPlayer superiorPlayer, IslandPermission islandPermission) {
        return !plugin.getSettings().spawnProtection || super.hasPermission(superiorPlayer, islandPermission);
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}

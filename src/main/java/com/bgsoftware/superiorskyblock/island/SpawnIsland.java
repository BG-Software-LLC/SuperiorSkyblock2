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

    public SpawnIsland(SuperiorSkyblockPlugin plugin) {
        super(null, SBlockPosition.of(plugin.getSettings().spawnLocation), "");
        SpawnIsland.plugin = plugin;

        String[] loc = plugin.getSettings().spawnLocation.split(", ");
        int x = (int) (double) Double.valueOf(loc[1]);
        int y = Integer.valueOf(loc[2]);
        int z = (int) (double) Double.valueOf(loc[3]);
        center = loc.length == 4 ?
            new Location(Bukkit.getWorld(loc[0]), x + 0.5, y, z + 0.5) :
            new Location(Bukkit.getWorld(loc[0]), x + 0.5, y, z + 0.5, Float.valueOf(loc[4]), Float.valueOf(loc[5]));
    }

    @Override
    public Location getCenter() {
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

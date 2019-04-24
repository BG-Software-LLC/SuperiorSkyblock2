package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import org.bukkit.Location;


public final class SpawnIsland extends SIsland {

    private Location preciseCenter;

    public SpawnIsland(SBlockPosition wrappedLocation){
        super(null, wrappedLocation);
    }

    public SpawnIsland() {
        super(null, SBlockPosition.of(SuperiorSkyblockPlugin.getPlugin().getSettings().spawnLocation));
        preciseCenter = SuperiorSkyblockPlugin.getPlugin().getSettings().getSpawnAsBukkitLocation();
        setTeleportLocation(preciseCenter);
    }

    @Override
    public Location getCenter() {
        return preciseCenter == null ? super.getCenter() : preciseCenter;
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

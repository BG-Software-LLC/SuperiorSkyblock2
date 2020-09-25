package com.bgsoftware.superiorskyblock.island.warps;

import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import org.bukkit.Location;

public final class SIslandWarp implements IslandWarp {

    private final Location location;
    private final boolean privateFlag;

    public SIslandWarp(Location location, boolean privateFlag){
        this.location = new Location(location.getWorld(), location.getBlockX() + 0.5, location.getBlockY(),
                location.getBlockZ() + 0.5, location.getYaw(), location.getPitch());
        this.privateFlag = privateFlag;
    }

    public Location getLocation() {
        return location.clone();
    }

    public boolean hasPrivateFlag() {
        return privateFlag;
    }

}

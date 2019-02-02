package com.bgsoftware.superiorskyblock.schematics;

import org.bukkit.Location;

public final class SchematicBlock {

    private final int combinedId;
    private final Location location;

    public SchematicBlock(int combinedId, Location location){
        this.combinedId = combinedId;
        this.location = location.clone();
    }

    public int getCombinedId() {
        return combinedId;
    }

    public Location getLocation() {
        return location;
    }
}

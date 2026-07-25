package com.bgsoftware.superiorskyblock.core.formatting.impl;

import com.bgsoftware.superiorskyblock.core.LazyWorldLocation;
import com.bgsoftware.superiorskyblock.core.formatting.IFormatter;
import org.bukkit.Location;

public class LocationFormatter implements IFormatter<Location> {

    private static final LocationFormatter INSTANCE = new LocationFormatter();

    public static LocationFormatter getInstance() {
        return INSTANCE;
    }

    private LocationFormatter() {

    }

    @Override
    public String format(Location value) {
        return LazyWorldLocation.getWorldName(value) + ", " + value.getBlockX() + ", " + value.getBlockY() + ", " + value.getBlockZ();
    }
}

package com.bgsoftware.superiorskyblock.core;

import org.bukkit.Location;

import java.util.Objects;

public class LocationKey {

    private final String worldName;
    private final double x;
    private final double y;
    private final double z;

    public LocationKey(Location location) {
        this(LazyWorldLocation.getWorldName(location), location.getX(), location.getY(), location.getZ());
    }

    public LocationKey(LazyWorldLocation location) {
        this(location.getWorldName(), location.getX(), location.getY(), location.getZ());
    }

    public LocationKey(String worldName, double x, double y, double z) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationKey that = (LocationKey) o;
        return Double.compare(that.x, x) == 0 && Double.compare(that.y, y) == 0 && Double.compare(that.z, z) == 0 && worldName.equals(that.worldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName, x, y, z);
    }

    @Override
    public String toString() {
        return "LocationKey{" +
                "worldName='" + worldName + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

}

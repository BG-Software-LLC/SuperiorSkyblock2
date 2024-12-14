package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import org.bukkit.Location;

import java.util.Objects;

public class LocationKey implements ObjectsPool.Releasable, AutoCloseable {

    private static final ObjectsPool<LocationKey> POOL = new ObjectsPool<>(LocationKey::new);

    private String worldName;
    private double x;
    private double y;
    private double z;

    public static LocationKey of(Location location) {
        return of(location, true);
    }

    public static LocationKey of(Location location, boolean fromPool) {
        return of(LazyWorldLocation.getWorldName(location), location.getX(), location.getY(), location.getZ(), fromPool);
    }

    public static LocationKey of(LazyWorldLocation location) {
        return of(location, true);
    }

    public static LocationKey of(LazyWorldLocation location, boolean fromPool) {
        return of(location.getWorldName(), location.getX(), location.getY(), location.getZ(), fromPool);
    }

    public static LocationKey of(String worldName, double x, double y, double z) {
        return of(worldName, x, y, z, true);
    }

    public static LocationKey of(String worldName, double x, double y, double z, boolean fromPool) {
        LocationKey locationKey = fromPool ? POOL.obtain() : new LocationKey();
        return locationKey.initialize(worldName, x, y, z);
    }

    public static LocationKey of(IslandWarp islandWarp) {
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            return of(islandWarp.getLocation(wrapper.getHandle()));
        }
    }

    private LocationKey() {

    }

    private LocationKey initialize(String worldName, double x, double y, double z) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public LocationKey copy() {
        return of(worldName, x, y, z, false);
    }

    @Override
    public void release() {
        this.worldName = null;
        POOL.release(this);
    }

    @Override
    public void close() {
        release();
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

package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.Location;

public class IslandPosition {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final int x;
    private final int z;

    private IslandPosition(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public static IslandPosition of(Location location) {
        int radius = plugin.getSettings().getMaxIslandSize() * 3;
        int x = (Math.abs(location.getBlockX()) + (radius / 2)) / radius;
        int z = (Math.abs(location.getBlockZ()) + (radius / 2)) / radius;
        return new IslandPosition(location.getBlockX() < 0 ? -x : x, location.getBlockZ() < 0 ? -z : z);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(((long) this.x << 32) | this.z);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IslandPosition && x == ((IslandPosition) obj).x && z == ((IslandPosition) obj).z;
    }

    @Override
    public String toString() {
        return "IslandPosition{x=" + x + ",z=" + z + "}";
    }

}

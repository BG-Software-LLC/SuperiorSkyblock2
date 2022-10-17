package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.google.common.base.Objects;
import org.bukkit.Location;

public class IslandPosition {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final int x;
    private final int z;
    private final String worldName;

    private IslandPosition(int x, int z, String worldName) {
        this.x = x;
        this.z = z;
        this.worldName = worldName;
    }

    public static IslandPosition of(Location location) {
        int radius = plugin.getSettings().getMaxIslandSize() * 3;
        int x = (Math.abs(location.getBlockX()) + (radius / 2)) / radius;
        int z = (Math.abs(location.getBlockZ()) + (radius / 2)) / radius;
        String worldName = plugin.getProviders().hasCustomWorldsSupport() && location.getWorld() != null ?
                location.getWorld().getName() : "";
        return new IslandPosition(location.getBlockX() < 0 ? -x : x, location.getBlockZ() < 0 ? -z : z, worldName);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(((long) this.x << 32) | this.z);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IslandPosition && x == ((IslandPosition) obj).x && z == ((IslandPosition) obj).z &&
                worldName.equals(((IslandPosition) obj).worldName);
    }

    @Override
    public String toString() {
        return "IslandPosition{x=" + x + ",z=" + z + ", world=" + worldName + "}";
    }

}

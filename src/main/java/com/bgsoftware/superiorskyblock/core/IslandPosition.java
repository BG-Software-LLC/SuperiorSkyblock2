package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
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
        return fromXZ(location.getBlockX(), location.getBlockZ());
    }

    public static IslandPosition of(Island island) {
        BlockPosition center = island.getCenterPosition();
        return fromXZ(center.getX(), center.getZ());
    }

    private static IslandPosition fromXZ(int locX, int locZ) {
        int radius = plugin.getSettings().getMaxIslandSize() * 3;
        int x = (Math.abs(locX) + (radius / 2)) / radius;
        int z = (Math.abs(locZ) + (radius / 2)) / radius;
        return new IslandPosition(locX < 0 ? -x : x, locZ < 0 ? -z : z);
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

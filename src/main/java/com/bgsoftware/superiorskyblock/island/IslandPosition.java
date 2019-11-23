package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Location;
import org.bukkit.World;

public final class IslandPosition {

    private static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private int x, z;

    private IslandPosition(int x, int z){
        this.x = x;
        this.z = z;
    }

    @Override
    public int hashCode() {
        int hash = 19 * 3 + (int)(Double.doubleToLongBits(this.x) ^ Double.doubleToLongBits(this.x) >>> 32);
        hash = 19 * hash + (int)(Double.doubleToLongBits(this.z) ^ Double.doubleToLongBits(this.z) >>> 32);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IslandPosition && x == ((IslandPosition) obj).x && z == ((IslandPosition) obj).z;
    }

    @Override
    public String toString() {
        return "IslandPosition{x=" + x + ",z=" + z + "}";
    }

    public static IslandPosition of(Island island){
        return of(island.getCenter(World.Environment.NORMAL));
    }

    public static IslandPosition of(Location location){
        int radius = plugin.getSettings().maxIslandSize * 3;
        int x = (Math.abs(location.getBlockX()) + (radius / 2)) / radius;
        int z = (Math.abs(location.getBlockZ()) + (radius / 2)) / radius;
        return new IslandPosition(location.getBlockX() < 0 ? -x : x, location.getBlockZ() < 0 ? -z : z);
    }

}

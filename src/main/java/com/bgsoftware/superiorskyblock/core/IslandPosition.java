package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.Location;

import java.util.Objects;

public class IslandPosition {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    @Nullable
    private final String worldName;
    private final int x;
    private final int z;

    private IslandPosition(@Nullable String worldName, int x, int z) {
        this.worldName = worldName;
        this.x = x;
        this.z = z;
    }

    public static IslandPosition of(Location location) {
        return of(LazyWorldLocation.getWorldName(location), location.getBlockX(), location.getBlockZ());
    }

    public static IslandPosition of(String worldName, int locX, int locZ) {
        int radius = plugin.getSettings().getMaxIslandSize() * 3;
        int x = (Math.abs(locX) + (radius / 2)) / radius;
        int z = (Math.abs(locZ) + (radius / 2)) / radius;
        return new IslandPosition(plugin.getProviders().hasCustomWorldsSupport() ? worldName : null, locX < 0 ? -x : x, locZ < 0 ? -z : z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IslandPosition that = (IslandPosition) o;
        return x == that.x && z == that.z && Objects.equals(worldName, that.worldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName, x, z);
    }

    @Override
    public String toString() {
        return "IslandPosition{x=" + x + ",z=" + z + "}";
    }

}

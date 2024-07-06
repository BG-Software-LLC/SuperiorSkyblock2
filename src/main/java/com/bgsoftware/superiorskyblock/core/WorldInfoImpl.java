package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import org.bukkit.World;

import java.util.Objects;

public class WorldInfoImpl implements WorldInfo {

    private final String worldName;
    private final Dimension dimension;

    public WorldInfoImpl(String worldName, Dimension dimension) {
        this.worldName = worldName;
        this.dimension = dimension;
    }

    @Override
    public String getName() {
        return this.worldName;
    }

    @Override
    @Deprecated
    public World.Environment getEnvironment() {
        return this.dimension.getEnvironment();
    }

    @Override
    public Dimension getDimension() {
        return this.dimension;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldInfoImpl worldInfo = (WorldInfoImpl) o;
        return dimension.equals(worldInfo.dimension) && worldName.equals(worldInfo.worldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName, dimension);
    }

}

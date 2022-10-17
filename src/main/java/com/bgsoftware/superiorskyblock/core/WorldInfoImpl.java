package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import org.bukkit.World;

import java.util.Objects;

public class WorldInfoImpl implements WorldInfo {

    private final String worldName;
    private final World.Environment environment;

    public WorldInfoImpl(String worldName, World.Environment environment) {
        this.worldName = worldName;
        this.environment = environment;
    }

    @Override
    public String getName() {
        return this.worldName;
    }

    @Override
    public World.Environment getEnvironment() {
        return this.environment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldInfoImpl worldInfo = (WorldInfoImpl) o;
        return worldName.equals(worldInfo.worldName) && environment == worldInfo.environment;
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName, environment);
    }

}

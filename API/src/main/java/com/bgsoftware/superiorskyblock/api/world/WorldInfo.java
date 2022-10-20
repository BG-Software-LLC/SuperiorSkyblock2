package com.bgsoftware.superiorskyblock.api.world;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.google.common.base.Preconditions;
import org.bukkit.World;

public interface WorldInfo {

    /**
     * Get the name of this world.
     */
    String getName();

    /**
     * Get the environment of this world.
     */
    World.Environment getEnvironment();

    /**
     * Create a new world info.
     *
     * @param world The world.
     */
    static WorldInfo of(World world) {
        Preconditions.checkNotNull(world, "world parameter cannot be null");
        return of(world.getName(), world.getEnvironment());
    }

    /**
     * Create a new world info.
     *
     * @param worldName   The name of the world.
     * @param environment The environment of the world.
     */
    static WorldInfo of(String worldName, World.Environment environment) {
        Preconditions.checkNotNull(worldName, "worldName parameter cannot be null");
        Preconditions.checkNotNull(environment, "environment parameter cannot be null");
        return SuperiorSkyblockAPI.getFactory().createWorldInfo(worldName, environment);
    }

}

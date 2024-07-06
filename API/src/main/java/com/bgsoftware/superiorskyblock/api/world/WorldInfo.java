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
    @Deprecated
    World.Environment getEnvironment();

    /**
     * Get the environment of this world.
     */
    Dimension getDimension();

    /**
     * Create a new world info.
     *
     * @param world The world.
     */
    static WorldInfo of(World world) {
        Preconditions.checkNotNull(world, "world parameter cannot be null");
        Dimension dimension = SuperiorSkyblockAPI.getProviders().getWorldsProvider().getIslandsWorldDimension(world);
        if (dimension == null)
            dimension = Dimension.getByName(world.getEnvironment().name());

        return of(world.getName(), dimension);
    }

    /**
     * Create a new world info.
     *
     * @param worldName   The name of the world.
     * @param environment The environment of the world.
     * @deprecated See {@link #of(String, Dimension)}
     */
    @Deprecated
    static WorldInfo of(String worldName, World.Environment environment) {
        Preconditions.checkNotNull(worldName, "worldName parameter cannot be null");
        Preconditions.checkNotNull(environment, "environment parameter cannot be null");
        return SuperiorSkyblockAPI.getFactory().createWorldInfo(worldName, environment);
    }

    /**
     * Create a new world info.
     *
     * @param worldName The name of the world.
     * @param dimension The dimension of the world.
     */
    static WorldInfo of(String worldName, Dimension dimension) {
        Preconditions.checkNotNull(worldName, "worldName parameter cannot be null");
        Preconditions.checkNotNull(dimension, "dimension parameter cannot be null");
        return SuperiorSkyblockAPI.getFactory().createWorldInfo(worldName, dimension);
    }

}

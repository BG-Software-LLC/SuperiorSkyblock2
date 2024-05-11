package com.bgsoftware.superiorskyblock.api.hooks;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import org.bukkit.World;

public interface LazyWorldsProvider extends WorldsProvider {

    /**
     * Prepare world for teleportation.
     *
     * @param island         The target island.
     * @param environment    The environment of the world to prepare.
     * @param finishCallback Callback function after the preparation is finished.
     */
    void prepareWorld(Island island, World.Environment environment, Runnable finishCallback);

    /**
     * Get the {@link WorldInfo} of the world of an island by the environment.
     * The world does not have to be loaded.
     *
     * @param island      The island to check.
     * @param environment The world environment.
     * @return The world info for the given environment, or null if this environment is not enabled.
     */
    @Nullable
    WorldInfo getIslandsWorldInfo(Island island, World.Environment environment);

    /**
     * Get the {@link WorldInfo} of the world of an island by its name.
     * The world does not have to be loaded.
     *
     * @param island    The island to check.
     * @param worldName The name of the world.
     * @return The world info for the given name, or null if this name is not an islands world.
     */
    @Nullable
    WorldInfo getIslandsWorldInfo(Island island, String worldName);

}

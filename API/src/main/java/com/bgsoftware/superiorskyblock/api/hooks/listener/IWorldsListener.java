package com.bgsoftware.superiorskyblock.api.hooks.listener;

/**
 * Listener for updates of worlds.
 */
public interface IWorldsListener {

    /**
     * Load a world.
     *
     * @param worldName the name of the world to load.
     */
    void loadWorld(String worldName);

}

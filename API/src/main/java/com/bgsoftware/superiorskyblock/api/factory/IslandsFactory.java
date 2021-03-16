package com.bgsoftware.superiorskyblock.api.factory;

import com.bgsoftware.superiorskyblock.api.island.Island;

public interface IslandsFactory {

    /**
     * Create a new island.
     * @param original The original island that was created.
     */
    Island createIsland(Island original);

}

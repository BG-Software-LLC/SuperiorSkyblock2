package com.bgsoftware.superiorskyblock.api.factory;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandCalculationAlgorithm;

public interface IslandsFactory {

    /**
     * Create a new island.
     * @param original The original island that was created.
     */
    Island createIsland(Island original);

    /**
     * Create a calculation algorithm for an island.
     * @param island The island to set the algorithm to.
     */
    IslandCalculationAlgorithm createIslandCalculationAlgorithm(Island island);

}

package com.bgsoftware.superiorskyblock.api.factory;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandBlocksTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandCalculationAlgorithm;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandEntitiesTrackerAlgorithm;

public interface IslandsFactory {

    /**
     * Create a new island.
     *
     * @param original The original island that was created.
     */
    Island createIsland(Island original);

    /**
     * Create a calculation algorithm for an island.
     *
     * @param island The island to set the algorithm to.
     * @deprecated Use {@link #createIslandCalculationAlgorithm(Island, IslandCalculationAlgorithm)}
     */
    @Deprecated
    default IslandCalculationAlgorithm createIslandCalculationAlgorithm(Island island) {
        throw new UnsupportedOperationException("Unsupported operation.");
    }

    /**
     * Create a blocks-tracking algorithm for an island.
     *
     * @param island The island to set the algorithm to.
     * @deprecated Use {@link #createIslandBlocksTrackerAlgorithm(Island, IslandBlocksTrackerAlgorithm)}
     */
    @Deprecated
    default IslandBlocksTrackerAlgorithm createIslandBlocksTrackerAlgorithm(Island island) {
        throw new UnsupportedOperationException("Unsupported operation.");
    }

    /**
     * Create an entities-tracking algorithm for an island.
     *
     * @param island The island to set the algorithm to.
     * @deprecated Use {@link #createIslandEntitiesTrackerAlgorithm(Island, IslandEntitiesTrackerAlgorithm)}
     */
    @Deprecated
    default IslandEntitiesTrackerAlgorithm createIslandEntitiesTrackerAlgorithm(Island island) {
        throw new UnsupportedOperationException("Unsupported operation.");
    }

    /**
     * Create a calculation algorithm for an island.
     *
     * @param island   The island to set the algorithm to.
     * @param original The original calculation algorithm.
     */
    IslandCalculationAlgorithm createIslandCalculationAlgorithm(Island island, IslandCalculationAlgorithm original);

    /**
     * Create a blocks-tracking algorithm for an island.
     *
     * @param island   The island to set the algorithm to.
     * @param original The original blocks tracking algorithm.
     */
    IslandBlocksTrackerAlgorithm createIslandBlocksTrackerAlgorithm(Island island, IslandBlocksTrackerAlgorithm original);

    /**
     * Create an entities-tracking algorithm for an island.
     *
     * @param island   The island to set the algorithm to.
     * @param original The original entities tracking algorithm.
     */
    IslandEntitiesTrackerAlgorithm createIslandEntitiesTrackerAlgorithm(Island island, IslandEntitiesTrackerAlgorithm original);

}

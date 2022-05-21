package com.bgsoftware.superiorskyblock.api.factory;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandBlocksTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandCalculationAlgorithm;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandEntitiesTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataContainer;

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

    /**
     * Create a new persistent data container for an island.
     *
     * @param island   The island to create the container for.
     * @param original The original persistent data container that was created.
     */
    PersistentDataContainer createPersistentDataContainer(Island island, PersistentDataContainer original);

}

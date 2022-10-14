package com.bgsoftware.superiorskyblock.core.factory;

import com.bgsoftware.superiorskyblock.api.factory.IslandsFactory;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandBlocksTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandCalculationAlgorithm;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandEntitiesTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataContainer;

public class DefaultIslandsFactory implements IslandsFactory {

    private static final DefaultIslandsFactory INSTANCE = new DefaultIslandsFactory();

    public static DefaultIslandsFactory getInstance() {
        return INSTANCE;
    }

    private DefaultIslandsFactory() {
    }

    @Override
    public Island createIsland(Island original) {
        return original;
    }

    @Override
    public IslandCalculationAlgorithm createIslandCalculationAlgorithm(Island island, IslandCalculationAlgorithm original) {
        return original;
    }

    @Override
    public IslandBlocksTrackerAlgorithm createIslandBlocksTrackerAlgorithm(Island island, IslandBlocksTrackerAlgorithm original) {
        return original;
    }

    @Override
    public IslandEntitiesTrackerAlgorithm createIslandEntitiesTrackerAlgorithm(Island island, IslandEntitiesTrackerAlgorithm original) {
        return original;
    }

    @Override
    public PersistentDataContainer createPersistentDataContainer(Island island, PersistentDataContainer original) {
        return original;
    }

}

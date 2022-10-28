package com.bgsoftware.superiorskyblock.api.factory;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandBlocksTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandCalculationAlgorithm;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandEntitiesTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataContainer;

public class DelegateIslandsFactory implements IslandsFactory {

    protected final IslandsFactory handle;

    protected DelegateIslandsFactory(IslandsFactory handle) {
        this.handle = handle;
    }

    @Override
    public Island createIsland(Island original) {
        return this.handle.createIsland(original);
    }

    @Override
    @Deprecated
    public IslandCalculationAlgorithm createIslandCalculationAlgorithm(Island island) {
        return this.handle.createIslandCalculationAlgorithm(island);
    }

    @Override
    @Deprecated
    public IslandBlocksTrackerAlgorithm createIslandBlocksTrackerAlgorithm(Island island) {
        return this.handle.createIslandBlocksTrackerAlgorithm(island);
    }

    @Override
    @Deprecated
    public IslandEntitiesTrackerAlgorithm createIslandEntitiesTrackerAlgorithm(Island island) {
        return this.handle.createIslandEntitiesTrackerAlgorithm(island);
    }

    @Override
    public IslandCalculationAlgorithm createIslandCalculationAlgorithm(Island island, IslandCalculationAlgorithm original) {
        return this.handle.createIslandCalculationAlgorithm(island, original);
    }

    @Override
    public IslandBlocksTrackerAlgorithm createIslandBlocksTrackerAlgorithm(Island island, IslandBlocksTrackerAlgorithm original) {
        return this.handle.createIslandBlocksTrackerAlgorithm(island, original);
    }

    @Override
    public IslandEntitiesTrackerAlgorithm createIslandEntitiesTrackerAlgorithm(Island island, IslandEntitiesTrackerAlgorithm original) {
        return this.handle.createIslandEntitiesTrackerAlgorithm(island, original);
    }

    @Override
    public PersistentDataContainer createPersistentDataContainer(Island island, PersistentDataContainer original) {
        return this.handle.createPersistentDataContainer(island, original);
    }

}

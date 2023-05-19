package com.bgsoftware.superiorskyblock.api.island.algorithms;

import com.bgsoftware.superiorskyblock.api.key.Key;

import java.util.Map;

public class DelegateIslandEntitiesTrackerAlgorithm implements IslandEntitiesTrackerAlgorithm {

    protected final IslandEntitiesTrackerAlgorithm handle;

    protected DelegateIslandEntitiesTrackerAlgorithm(IslandEntitiesTrackerAlgorithm handle) {
        this.handle = handle;
    }

    @Override
    public boolean trackEntity(Key key, int amount) {
        return this.handle.trackEntity(key, amount);
    }

    @Override
    public boolean untrackEntity(Key key, int amount) {
        return this.handle.untrackEntity(key, amount);
    }

    @Override
    public int getEntityCount(Key key) {
        return this.handle.getEntityCount(key);
    }

    @Override
    public Map<Key, Integer> getEntitiesCounts() {
        return this.handle.getEntitiesCounts();
    }

    @Override
    public void clearEntityCounts() {
        this.handle.clearEntityCounts();
    }

    @Override
    public void recalculateEntityCounts() {
        this.handle.recalculateEntityCounts();
    }

    @Override
    public boolean canRecalculateEntityCounts() {
        return this.handle.canRecalculateEntityCounts();
    }

}

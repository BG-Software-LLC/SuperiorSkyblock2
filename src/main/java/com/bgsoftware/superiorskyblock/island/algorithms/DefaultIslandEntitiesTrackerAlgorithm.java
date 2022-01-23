package com.bgsoftware.superiorskyblock.island.algorithms;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandEntitiesTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.key.dataset.KeyMap;

import java.math.BigInteger;
import java.util.Map;

public final class DefaultIslandEntitiesTrackerAlgorithm implements IslandEntitiesTrackerAlgorithm {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final KeyMap<BigInteger> entityCounts = new KeyMap<>();

    private final Island island;

    public DefaultIslandEntitiesTrackerAlgorithm(Island island) {
        this.island = island;
    }

    @Override
    public boolean trackEntity(Key key, BigInteger amount) {
        return false;
    }

    @Override
    public boolean untrackEntity(Key key, BigInteger amount) {
        return false;
    }

    @Override
    public BigInteger getEntityCount(Key key) {
        return null;
    }

    @Override
    public Map<Key, BigInteger> getEntitiesCounts() {
        return null;
    }

    @Override
    public void clearEntityCounts() {

    }

}

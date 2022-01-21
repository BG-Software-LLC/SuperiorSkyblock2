package com.bgsoftware.superiorskyblock.island.algorithms;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandEntitiesTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.key.dataset.KeyMap;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.google.common.base.Preconditions;

import java.util.Map;

public final class DefaultIslandEntitiesTrackerAlgorithm implements IslandEntitiesTrackerAlgorithm {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final KeyMap<Integer> entityCounts = new KeyMap<>();

    private final Island island;

    public DefaultIslandEntitiesTrackerAlgorithm(Island island) {
        this.island = island;
    }

    @Override
    public boolean trackEntity(Key key, Integer amount) {
        return false;
    }

    @Override
    public boolean untrackEntity(Key key, Integer amount) {
        return false;
    }

    @Override
    public Integer getEntityCount(Key key) {
        return null;
    }

    @Override
    public Map<Key, Integer> getEntitiesCounts() {
        return null;
    }

    @Override
    public void clearEntityCounts() {

    }

}

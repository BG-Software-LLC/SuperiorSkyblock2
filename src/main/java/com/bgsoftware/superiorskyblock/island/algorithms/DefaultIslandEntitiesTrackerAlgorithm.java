package com.bgsoftware.superiorskyblock.island.algorithms;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandEntitiesTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.key.dataset.KeyMap;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.Map;

public final class DefaultIslandEntitiesTrackerAlgorithm implements IslandEntitiesTrackerAlgorithm {

    private final KeyMap<Integer> entityCounts = new KeyMap<>();

    private final Island island;

    public DefaultIslandEntitiesTrackerAlgorithm(Island island) {
        this.island = island;
    }

    @Override
    public boolean trackEntity(Key key, Integer amount) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        Preconditions.checkNotNull(amount, "amount parameter cannot be null.");

        if (amount <= 0)
            return false;

        if (island.getEntityLimit(key) == IslandUtils.NO_LIMIT.get())
            return false;

        PluginDebugger.debug("Action: Entity Spawn, Island: " + island.getOwner().getName() +
                ", Entity: " + key + ", Amount: " + amount);

        int currentAmount = entityCounts.getOrDefault(key, 0);
        entityCounts.put(key, currentAmount + amount);

        return true;
    }

    @Override
    public boolean untrackEntity(Key key, Integer amount) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        Preconditions.checkNotNull(amount, "amount parameter cannot be null.");

        if (amount <= 0)
            return false;

        if (island.getEntityLimit(key) == IslandUtils.NO_LIMIT.get())
            return false;

        int currentAmount = entityCounts.getOrDefault(key, -1);

        if (currentAmount != -1) {
            if (currentAmount > amount) {
                entityCounts.put(key, currentAmount - amount);
            } else {
                entityCounts.remove(key);
            }
        }

        return true;
    }

    @Override
    public Integer getEntityCount(Key key) {
        return entityCounts.getOrDefault(key, 0);
    }

    @Override
    public Map<Key, Integer> getEntitiesCounts() {
        return Collections.unmodifiableMap(entityCounts);
    }

    @Override
    public void clearEntityCounts() {
        this.entityCounts.clear();
    }

}

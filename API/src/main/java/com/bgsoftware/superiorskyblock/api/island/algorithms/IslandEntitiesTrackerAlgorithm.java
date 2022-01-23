package com.bgsoftware.superiorskyblock.api.island.algorithms;

import com.bgsoftware.superiorskyblock.api.key.Key;

import java.math.BigInteger;
import java.util.Map;

public interface IslandEntitiesTrackerAlgorithm {

    /**
     * Track a new entity with a specific amount.
     *
     * @param key    The entity's key that should be tracked.
     * @param amount The amount of the entity.
     * @return Whether the entity was successfully tracked.
     */
    boolean trackEntity(Key key, BigInteger amount);

    /**
     * Untrack a entity with a specific amount.
     *
     * @param key    The entity's key that should be untracked.
     * @param amount The amount of the entity.
     * @return Whether the entity was successfully untracked.
     */
    boolean untrackEntity(Key key, BigInteger amount);

    /**
     * Get the amount of entities that are on the island.
     *
     * @param key The entity's key to check.
     */
    BigInteger getEntityCount(Key key);

    /**
     * Get all the entities that are on the island.
     */
    Map<Key, BigInteger> getEntitiesCounts();

    /**
     * Clear all the entity counts of the island.
     */
    void clearEntityCounts();

}

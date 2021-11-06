package com.bgsoftware.superiorskyblock.api.island.algorithms;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface IslandCalculationAlgorithm {

    /**
     * Calculate the island blocks of the island.
     *
     * @return CompletableFuture instance of the result.
     * @deprecated See {@link #calculateIsland(Island)}
     */
    @Deprecated
    default CompletableFuture<IslandCalculationResult> calculateIsland() {
        throw new UnsupportedOperationException("This method is not supported anymore. Use calculateIsland(Island) instead.");
    }

    /**
     * Calculate the island blocks of the island.
     *
     * @param island The island to calculate blocks for.
     * @return CompletableFuture instance of the result.
     */
    CompletableFuture<IslandCalculationResult> calculateIsland(Island island);

    /**
     * Represents calculation result.
     */
    interface IslandCalculationResult {

        /**
         * Get all block-counts that were calculated.
         */
        Map<Key, BigInteger> getBlockCounts();

    }

}

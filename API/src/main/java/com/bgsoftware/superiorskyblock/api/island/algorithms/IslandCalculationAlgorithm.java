package com.bgsoftware.superiorskyblock.api.island.algorithms;

import com.bgsoftware.superiorskyblock.api.key.Key;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface IslandCalculationAlgorithm {

    CompletableFuture<IslandCalculationResult> calculateIsland();

    interface IslandCalculationResult{

        Map<Key, BigInteger> getBlockCounts();

    }

}

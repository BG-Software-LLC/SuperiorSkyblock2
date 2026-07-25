package com.bgsoftware.superiorskyblock.island.algorithm;

import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandBlocksTrackerAlgorithm;
import com.bgsoftware.superiorskyblock.api.key.Key;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;

public class SpawnIslandBlocksTrackerAlgorithm implements IslandBlocksTrackerAlgorithm {

    private static final SpawnIslandBlocksTrackerAlgorithm INSTANCE = new SpawnIslandBlocksTrackerAlgorithm();

    private SpawnIslandBlocksTrackerAlgorithm() {
    }

    public static SpawnIslandBlocksTrackerAlgorithm getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean trackBlock(Key key, BigInteger amount) {
        return false;
    }

    @Override
    public boolean untrackBlock(Key key, BigInteger amount) {
        return false;
    }

    @Override
    public BigInteger getBlockCount(Key key) {
        return BigInteger.ZERO;
    }

    @Override
    public BigInteger getExactBlockCount(Key key) {
        return BigInteger.ZERO;
    }

    @Override
    public Map<Key, BigInteger> getBlockCounts() {
        return Collections.emptyMap();
    }

    @Override
    public void clearBlockCounts() {
        // Do nothing.
    }

    @Override
    public void setLoadingDataMode(boolean loadingDataMode) {
        // Do nothing.
    }

}

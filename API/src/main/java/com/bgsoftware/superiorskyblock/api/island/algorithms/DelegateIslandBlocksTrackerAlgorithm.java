package com.bgsoftware.superiorskyblock.api.island.algorithms;

import com.bgsoftware.superiorskyblock.api.key.Key;

import java.math.BigInteger;
import java.util.Map;

public class DelegateIslandBlocksTrackerAlgorithm implements IslandBlocksTrackerAlgorithm {

    protected final IslandBlocksTrackerAlgorithm handle;

    protected DelegateIslandBlocksTrackerAlgorithm(IslandBlocksTrackerAlgorithm handle) {
        this.handle = handle;
    }

    @Override
    public boolean trackBlock(Key key, BigInteger amount) {
        return this.handle.trackBlock(key, amount);
    }

    @Override
    public boolean untrackBlock(Key key, BigInteger amount) {
        return this.handle.untrackBlock(key, amount);
    }

    @Override
    public BigInteger getBlockCount(Key key) {
        return this.handle.getBlockCount(key);
    }

    @Override
    public BigInteger getExactBlockCount(Key key) {
        return this.handle.getExactBlockCount(key);
    }

    @Override
    public Map<Key, BigInteger> getBlockCounts() {
        return this.handle.getBlockCounts();
    }

    @Override
    public void clearBlockCounts() {
        this.handle.clearBlockCounts();
    }

    @Override
    public void setLoadingDataMode(boolean loadingDataMode) {
        this.handle.setLoadingDataMode(loadingDataMode);
    }

}

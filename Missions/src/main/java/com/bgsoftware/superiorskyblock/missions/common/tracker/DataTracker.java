package com.bgsoftware.superiorskyblock.missions.common.tracker;

import com.bgsoftware.superiorskyblock.core.Counter;
import com.bgsoftware.superiorskyblock.missions.common.requirements.IRequirements;

import java.util.Collections;
import java.util.Map;

public abstract class DataTracker<K, R extends IRequirements<K>> {

    private final Map<K, Counter> trackedData;
    private final Counter globalCounter = new Counter(0);

    protected DataTracker(Map<K, Counter> trackedData) {
        this.trackedData = trackedData;
    }

    public void track(K key, int amount) {
        this.trackedData.computeIfAbsent(key, k -> new Counter(0)).inc(amount);
        globalCounter.inc(amount);
    }

    public void load(K blockKey, int amount) {
        this.trackedData.put(blockKey, new Counter(amount));
        globalCounter.inc(amount);
    }

    public int getCount(K blockKey) {
        Counter counter = this.trackedData.get(blockKey);
        return counter == null ? 0 : counter.get();
    }

    public int getGlobalCounter() {
        return this.globalCounter.get();
    }

    public int getCounts(R blocks) {
        if (blocks.isContainsAll())
            return getGlobalCounter();

        int blocksCount = 0;
        for (K block : blocks)
            blocksCount += getCount(block);
        return blocksCount;
    }

    public void clear() {
        this.trackedData.clear();
    }

    public Map<K, Counter> getCounts() {
        return Collections.unmodifiableMap(this.trackedData);
    }

}

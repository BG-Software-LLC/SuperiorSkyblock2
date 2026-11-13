package com.bgsoftware.superiorskyblock.missions.common.tracker;

import com.bgsoftware.superiorskyblock.core.Counter;
import com.bgsoftware.superiorskyblock.missions.common.requirements.IRequirements;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class DataTracker<K, R extends IRequirements<K>> {

    private final Map<K, Counter> trackedData;
    private final Counter globalCounter = new Counter(0);

    protected DataTracker(Map<K, Counter> trackedData) {
        this.trackedData = trackedData;
    }

    public synchronized void track(K key, int amount) {
        this.trackedData.computeIfAbsent(key, k -> new Counter(0)).inc(amount);
        globalCounter.inc(amount);
    }

    public synchronized void load(K blockKey, int amount) {
        this.trackedData.put(blockKey, new Counter(amount));
        globalCounter.inc(amount);
    }

    public synchronized int getCount(K blockKey) {
        return Optional.ofNullable(this.trackedData.get(blockKey)).map(Counter::get).orElse(0);
    }

    public synchronized int getGlobalCounter() {
        return this.globalCounter.get();
    }

    public synchronized int getCounts(R blocks) {
        if (blocks.isContainsAll())
            return getGlobalCounter();

        Counter blocksCount = new Counter(0);
        blocks.forEach(block -> blocksCount.inc(getCount(block)));
        return blocksCount.get();
    }

    public synchronized void clear() {
        this.trackedData.clear();
    }

    public synchronized Map<K, Counter> getCounts() {
        Map<K, Counter> snapshot = createSnapshotMap();
        this.trackedData.forEach((key, count) -> snapshot.put(key, new Counter(count.get())));
        return Collections.unmodifiableMap(snapshot);
    }

    protected Map<K, Counter> createSnapshotMap() {
        return new HashMap<>();
    }

}

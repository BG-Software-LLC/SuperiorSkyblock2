package com.bgsoftware.superiorskyblock.missions.farming;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrackedPlantsData {

    private final Map<Long, Map<Integer, UUID>> trackedData = new HashMap<>();

    public synchronized void track(long chunkKey, int block, UUID placer) {
        trackedData.computeIfAbsent(chunkKey, i -> new HashMap<>()).put(block, placer);
    }

    public synchronized void untrack(long chunkKey, int block) {
        Map<Integer, UUID> chunkTrackedData = this.trackedData.get(chunkKey);
        if (chunkTrackedData != null)
            chunkTrackedData.remove(block);
    }

    public synchronized UUID getPlacer(long chunkKey, int block) {
        Map<Integer, UUID> chunkTrackedData = this.trackedData.get(chunkKey);
        return chunkTrackedData == null ? null : chunkTrackedData.get(block);
    }

    public synchronized Map<Long, Map<Integer, UUID>> getPlants() {
        Map<Long, Map<Integer, UUID>> snapshot = new HashMap<>();
        this.trackedData.forEach((chunkKey, blocks) ->
                snapshot.put(chunkKey, Collections.unmodifiableMap(new HashMap<>(blocks))));
        return Collections.unmodifiableMap(snapshot);
    }

}

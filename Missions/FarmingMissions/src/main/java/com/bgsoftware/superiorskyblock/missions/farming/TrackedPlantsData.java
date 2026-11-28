package com.bgsoftware.superiorskyblock.missions.farming;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrackedPlantsData {

    private final Map<Long, Map<Integer, UUID>> trackedData = new HashMap<>();

    public void track(long chunkKey, int block, UUID placer) {
        trackedData.computeIfAbsent(chunkKey, i -> new HashMap<>()).put(block, placer);
    }

    public void untrack(long chunkKey, int block) {
        Map<Integer, UUID> chunkTrackedData = this.trackedData.get(chunkKey);
        if (chunkTrackedData != null)
            chunkTrackedData.remove(block);
    }

    public UUID getPlacer(long chunkKey, int block) {
        Map<Integer, UUID> chunkTrackedData = this.trackedData.get(chunkKey);
        return chunkTrackedData == null ? null : chunkTrackedData.get(block);
    }

    public Map<Long, Map<Integer, UUID>> getPlants() {
        return trackedData;
    }

}

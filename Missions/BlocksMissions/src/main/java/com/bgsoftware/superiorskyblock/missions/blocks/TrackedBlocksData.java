package com.bgsoftware.superiorskyblock.missions.blocks;

import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntSupplier;
import java.util.concurrent.ConcurrentHashMap;

public class TrackedBlocksData {

    private final Map<Long, ChunkBitSet> TRACKED_BLOCKS = new ConcurrentHashMap<>();

    public TrackedBlocksData(ConfigurationSection section) {
        for (String chunkKey : section.getKeys(false)) {
            List<Integer> trackedBlocks = section.getIntegerList(chunkKey);
            ChunkBitSet bitSet = new ChunkBitSet();
            trackedBlocks.forEach(bitSet::set);
            try {
                TRACKED_BLOCKS.put(Long.parseLong(chunkKey), bitSet);
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public TrackedBlocksData() {

    }

    public void track(long chunkKey, int block) {
        TRACKED_BLOCKS.computeIfAbsent(chunkKey, key -> new ChunkBitSet()).set(block);
    }

    public boolean untrack(long chunkKey, IntSupplier block) {
        return Optional.ofNullable(TRACKED_BLOCKS.get(chunkKey))
                .map(bitSet -> bitSet.clear(block.getAsInt()))
                .orElse(false);
    }

    public boolean contains(long chunkKey, IntSupplier block) {
        return Optional.ofNullable(TRACKED_BLOCKS.get(chunkKey))
                .map(bitSet -> bitSet.get(block.getAsInt()))
                .orElse(false);
    }

    public Map<Long, ChunkBitSet> getBlocks() {
        Map<Long, ChunkBitSet> snapshot = new HashMap<>();
        TRACKED_BLOCKS.forEach((chunkKey, blocks) -> snapshot.put(chunkKey, blocks.copy()));
        return Collections.unmodifiableMap(snapshot);
    }

}

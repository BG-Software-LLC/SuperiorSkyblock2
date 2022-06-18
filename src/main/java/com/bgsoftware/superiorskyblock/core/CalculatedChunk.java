package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import org.bukkit.Location;

import java.util.Set;

public class CalculatedChunk {

    private final ChunkPosition chunkPosition;
    private final KeyMap<Integer> blockCounts;
    private final Set<Location> spawners;

    public CalculatedChunk(ChunkPosition chunkPosition, KeyMap<Integer> blockCounts, Set<Location> spawners) {
        this.chunkPosition = chunkPosition;
        this.blockCounts = blockCounts;
        this.spawners = spawners;
    }

    public ChunkPosition getPosition() {
        return chunkPosition;
    }

    public KeyMap<Integer> getBlockCounts() {
        return blockCounts;
    }

    public Set<Location> getSpawners() {
        return spawners;
    }

}

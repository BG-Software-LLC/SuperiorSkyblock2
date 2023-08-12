package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import org.bukkit.Location;

import java.util.List;

public class CalculatedChunk {

    private final ChunkPosition chunkPosition;
    private final KeyMap<Counter> blockCounts;
    private final List<Location> spawners;

    public CalculatedChunk(ChunkPosition chunkPosition, KeyMap<Counter> blockCounts, List<Location> spawners) {
        this.chunkPosition = chunkPosition;
        this.blockCounts = blockCounts;
        this.spawners = spawners;
    }

    public ChunkPosition getPosition() {
        return chunkPosition;
    }

    public KeyMap<Counter> getBlockCounts() {
        return blockCounts;
    }

    public List<Location> getSpawners() {
        return spawners;
    }

}

package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import org.bukkit.Location;

import java.util.List;

public class CalculatedChunk {

    private final ChunkPosition chunkPosition;

    protected CalculatedChunk(ChunkPosition chunkPosition) {
        this.chunkPosition = chunkPosition;
    }

    public ChunkPosition getPosition() {
        return chunkPosition;
    }

    public static class Blocks extends CalculatedChunk {

        private final KeyMap<Counter> blockCounts;
        private final List<Location> spawners;

        public Blocks(ChunkPosition chunkPosition, KeyMap<Counter> blockCounts, List<Location> spawners) {
            super(chunkPosition);
            this.blockCounts = blockCounts;
            this.spawners = spawners;
        }

        public KeyMap<Counter> getBlockCounts() {
            return blockCounts;
        }

        public List<Location> getSpawners() {
            return spawners;
        }

    }

    public static class Entities extends CalculatedChunk {

        private final KeyMap<Counter> entityCounts;

        public Entities(ChunkPosition chunkPosition, KeyMap<Counter> entityCounts) {
            super(chunkPosition);
            this.entityCounts = entityCounts;
        }

        public KeyMap<Counter> getEntityCounts() {
            return entityCounts;
        }

    }

}

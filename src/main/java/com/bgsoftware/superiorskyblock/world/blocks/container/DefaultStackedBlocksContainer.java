package com.bgsoftware.superiorskyblock.world.blocks.container;

import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.world.blocks.StackedBlock;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class DefaultStackedBlocksContainer implements StackedBlocksContainer {

    private final Map<ChunkPosition, Map<Location, StackedBlock>> stackedBlocks = new HashMap<>();

    @Nullable
    @Override
    public StackedBlock getStackedBlock(Location location) {
        ChunkPosition chunkPosition = ChunkPosition.of(location);
        Map<Location, StackedBlock> chunkStackedBlocks = this.stackedBlocks.get(chunkPosition);
        return chunkStackedBlocks == null ? null : chunkStackedBlocks.get(location);
    }

    @Override
    public StackedBlock createStackedBlock(Location location) {
        ChunkPosition chunkPosition = ChunkPosition.of(location);
        return this.stackedBlocks.computeIfAbsent(chunkPosition, c -> new HashMap<>())
                .computeIfAbsent(location, StackedBlock::new);
    }

    @Override
    public StackedBlock removeStackedBlock(Location location) {
        ChunkPosition chunkPosition = ChunkPosition.of(location);
        Map<Location, StackedBlock> chunkStackedBlocks = this.stackedBlocks.get(chunkPosition);
        StackedBlock removedStackedBlock = chunkStackedBlocks == null ? null : chunkStackedBlocks.remove(location);
        if(chunkStackedBlocks != null && chunkStackedBlocks.isEmpty())
            this.stackedBlocks.remove(chunkPosition);
        return removedStackedBlock;
    }

    @Override
    public Map<Location, StackedBlock> getStackedBlocks(ChunkPosition chunkPosition) {
        return this.stackedBlocks.getOrDefault(chunkPosition, Collections.emptyMap());
    }

    @Override
    public Map<Location, StackedBlock> getStackedBlocks() {
        Map<Location, StackedBlock> stackedBlockMap = new HashMap<>();
        this.stackedBlocks.values().forEach(stackedBlockMap::putAll);
        return stackedBlockMap;
    }

    @Override
    public Map<Location, StackedBlock> removeStackedBlocks(ChunkPosition chunkPosition) {
        Map<Location, StackedBlock> chunkStackedBlocks = this.stackedBlocks.remove(chunkPosition);
        return chunkStackedBlocks == null ? Collections.emptyMap() : chunkStackedBlocks;
    }

}

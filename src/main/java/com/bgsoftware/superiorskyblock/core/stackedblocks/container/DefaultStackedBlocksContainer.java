package com.bgsoftware.superiorskyblock.core.stackedblocks.container;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.collections.Location2ObjectMap;
import com.bgsoftware.superiorskyblock.core.stackedblocks.StackedBlock;
import org.bukkit.Location;

import java.util.function.Consumer;

public class DefaultStackedBlocksContainer implements StackedBlocksContainer {

    private final Location2ObjectMap<StackedBlock> stackedBlocks = new Location2ObjectMap<>();

    @Nullable
    @Override
    public StackedBlock getStackedBlock(Location location) {
        return this.stackedBlocks.get(location);
    }

    @Override
    public StackedBlock createStackedBlock(Location location) {
        StackedBlock stackedBlock = new StackedBlock(location);
        StackedBlock oldStackedBlock = this.stackedBlocks.put(location, stackedBlock);
        if (oldStackedBlock != null)
            oldStackedBlock.markAsRemoved();
        return stackedBlock;
    }

    @Override
    public StackedBlock removeStackedBlock(Location location) {
        StackedBlock removedStackedBlock = this.stackedBlocks.remove(location);
        if (removedStackedBlock != null)
            removedStackedBlock.markAsRemoved();
        return removedStackedBlock;
    }

    @Override
    public void forEach(ChunkPosition chunkPosition, Consumer<StackedBlock> consumer) {
        this.stackedBlocks.forEach(chunkPosition, consumer);
    }

    @Override
    public void forEach(Consumer<StackedBlock> consumer) {
        this.stackedBlocks.values().forEach(consumer);
    }

    @Override
    public void removeStackedBlocks(ChunkPosition chunkPosition, Consumer<StackedBlock> consumer) {
        this.stackedBlocks.removeAll(chunkPosition, stackedBlock -> {
            stackedBlock.markAsRemoved();
            consumer.accept(stackedBlock);
        });
    }

}

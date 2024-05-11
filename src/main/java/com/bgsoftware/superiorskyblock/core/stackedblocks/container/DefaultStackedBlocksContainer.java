package com.bgsoftware.superiorskyblock.core.stackedblocks.container;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.LazyWorldLocation;
import com.bgsoftware.superiorskyblock.core.LocationKey;
import com.bgsoftware.superiorskyblock.core.WorldInfoImpl;
import com.bgsoftware.superiorskyblock.core.stackedblocks.StackedBlock;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class DefaultStackedBlocksContainer implements StackedBlocksContainer {

    private final Map<ChunkPosition, Map<LocationKey, StackedBlock>> stackedBlocks = new HashMap<>();

    @Nullable
    @Override
    public StackedBlock getStackedBlock(Location location) {
        ChunkPosition chunkPosition = ChunkPosition.of(location);
        Map<LocationKey, StackedBlock> chunkStackedBlocks = this.stackedBlocks.get(chunkPosition);
        return chunkStackedBlocks == null ? null : chunkStackedBlocks.get(new LocationKey(location));
    }

    @Override
    public StackedBlock createStackedBlock(Location location) {
        World world = location.getWorld();

        ChunkPosition chunkPosition;

        if (world != null) {
            chunkPosition = ChunkPosition.of(location);
        } else if (location instanceof LazyWorldLocation) {
            String worldName = ((LazyWorldLocation) location).getWorldName();
            // We do not care about the environment here.
            WorldInfo worldInfo = new WorldInfoImpl(worldName, null);
            chunkPosition = ChunkPosition.of(worldInfo, location.getBlockX() >> 4, location.getBlockZ() >> 4);
        } else {
            throw new IllegalArgumentException("Cannot load stacked block with invalid world " +
                    LazyWorldLocation.getWorldName(location) + ", skipping...");
        }

        return this.stackedBlocks.computeIfAbsent(chunkPosition, c -> new HashMap<>())
                .computeIfAbsent(new LocationKey(location), l -> new StackedBlock(location.clone()));
    }

    @Override
    public StackedBlock removeStackedBlock(Location location) {
        ChunkPosition chunkPosition = ChunkPosition.of(location);
        Map<LocationKey, StackedBlock> chunkStackedBlocks = this.stackedBlocks.get(chunkPosition);
        StackedBlock removedStackedBlock = chunkStackedBlocks == null ? null : chunkStackedBlocks.remove(new LocationKey(location));
        if (chunkStackedBlocks != null && chunkStackedBlocks.isEmpty())
            this.stackedBlocks.remove(chunkPosition);
        if (removedStackedBlock != null)
            removedStackedBlock.markAsRemoved();
        return removedStackedBlock;
    }

    @Override
    public void forEach(ChunkPosition chunkPosition, Consumer<StackedBlock> consumer) {
        Map<LocationKey, StackedBlock> chunkStackedBlocks = this.stackedBlocks.get(chunkPosition);
        if (chunkStackedBlocks != null)
            chunkStackedBlocks.values().forEach(consumer);
    }

    @Override
    public void forEach(Consumer<StackedBlock> consumer) {
        this.stackedBlocks.values().forEach(chunkStackedBlocks -> chunkStackedBlocks.values().forEach(consumer));
    }

    @Override
    public void removeStackedBlocks(ChunkPosition chunkPosition, Consumer<StackedBlock> consumer) {
        Map<LocationKey, StackedBlock> chunkStackedBlocks = this.stackedBlocks.remove(chunkPosition);
        if (chunkStackedBlocks != null)
            chunkStackedBlocks.values().forEach(consumer);
    }

}

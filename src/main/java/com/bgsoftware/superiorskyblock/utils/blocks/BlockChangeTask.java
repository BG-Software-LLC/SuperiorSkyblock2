package com.bgsoftware.superiorskyblock.utils.blocks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksProvider;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class BlockChangeTask {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final Map<ChunkPosition, List<BlockData>> blocksCache = Maps.newConcurrentMap();
    private final Island island;

    private boolean submitted = false;

    public BlockChangeTask(Island island){
        this.island = island;
    }

    public void setBlock(Location location, int combinedId, CompoundTag statesTag, CompoundTag tileEntity){
        Preconditions.checkArgument(!submitted, "This MultiBlockChange was already submitted.");
        blocksCache.computeIfAbsent(ChunkPosition.of(location), pairs -> new ArrayList<>())
                .add(new BlockData(location, combinedId, statesTag, tileEntity));
    }

    public void submitUpdate(Runnable onFinish){
        try {
            Preconditions.checkArgument(!submitted, "This MultiBlockChange was already submitted.");

            submitted = true;
            int index = 0, size = blocksCache.size();

            for (Map.Entry<ChunkPosition, List<BlockData>> entry : blocksCache.entrySet()) {
                int entryIndex = ++index;
                ChunksProvider.loadChunk(entry.getKey(), chunk -> {
                    plugin.getNMSBlocks().refreshLight(chunk);
                    ChunksTracker.markDirty(island, chunk, false);

                    for (BlockData blockData : entry.getValue())
                        plugin.getNMSBlocks().setBlock(chunk, blockData.location, blockData.combinedId,
                                blockData.statesTag, blockData.tileEntity);

                    plugin.getNMSBlocks().refreshChunk(chunk);

                    if(entryIndex == size && onFinish != null)
                        onFinish.run();
                });
            }
        }finally {
            blocksCache.clear();
        }
    }

    private static class BlockData {

        private final Location location;
        private final int combinedId;
        private final CompoundTag statesTag, tileEntity;

        BlockData(Location location, int combinedId, CompoundTag statesTag, CompoundTag tileEntity){
            this.location = location;
            this.combinedId = combinedId;
            this.statesTag = statesTag;
            this.tileEntity = tileEntity;
        }

    }

}

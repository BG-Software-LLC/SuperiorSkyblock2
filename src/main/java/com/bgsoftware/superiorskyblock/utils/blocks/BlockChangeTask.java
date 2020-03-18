package com.bgsoftware.superiorskyblock.utils.blocks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.schematics.data.BlockType;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class BlockChangeTask {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final Map<ChunkPosition, List<BlockData>> blocksCache = Maps.newConcurrentMap();
    private final List<Chunk> chunksToUpdate = new ArrayList<>();

    private boolean submitted = false;

    public void setBlock(Location location, int combinedId, BlockType blockType, Object... args){
        Preconditions.checkArgument(!submitted, "This MultiBlockChange was already submitted.");
        ChunkPosition chunkPosition = new ChunkPosition(location.getWorld().getName(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
        blocksCache.computeIfAbsent(chunkPosition, pairs -> new ArrayList<>()).add(new BlockData(location, combinedId, blockType, args));
    }

    public void submitUpdate(Runnable onFinish){
        Preconditions.checkArgument(!submitted, "This MultiBlockChange was already submitted.");

        submitted = true;

        for(Map.Entry<ChunkPosition, List<BlockData>> entry : blocksCache.entrySet()){
            Chunk chunk = Bukkit.getWorld(entry.getKey().world).getChunkAt(entry.getKey().x, entry.getKey().z);
            chunksToUpdate.add(chunk);
            plugin.getNMSBlocks().refreshLight(chunk);
            ChunksTracker.markDirty(null, chunk, false);

            for(BlockData blockData : entry.getValue()){
                plugin.getNMSBlocks().setBlock(chunk, blockData.location, blockData.combinedId, blockData.blockType, blockData.args);
            }
        }

        blocksCache.clear();

        chunksToUpdate.forEach(chunk -> plugin.getNMSBlocks().refreshChunk(chunk));

        chunksToUpdate.clear();

        if(onFinish != null)
            onFinish.run();
    }

    private static class ChunkPosition{

        private String world;
        private int x, z;

        ChunkPosition(String world, int x, int z){
            this.world = world;
            this.x = x;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChunkPosition that = (ChunkPosition) o;
            return x == that.x &&
                    z == that.z &&
                    Objects.equals(world, that.world);
        }

        @Override
        public int hashCode() {
            return Objects.hash(world, x, z);
        }
    }

    private static class BlockData{

        private final Location location;
        private final int combinedId;
        private final BlockType blockType;
        private final Object[] args;

        BlockData(Location location, int combinedId, BlockType blockType, Object... args){
            this.location = location;
            this.combinedId = combinedId;
            this.blockType = blockType;
            this.args = args;
        }

    }

}

package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.CalculatedChunk;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.Counter;
import com.bgsoftware.superiorskyblock.core.collections.Chunk2ObjectMap;
import com.bgsoftware.superiorskyblock.core.threads.Synchronized;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface NMSChunks {

    void setBiome(List<ChunkPosition> chunkPositions, Biome biome, Collection<Player> playersToUpdate);

    void deleteChunks(Island island, List<ChunkPosition> chunkPositions, @Nullable Runnable onFinish);

    CompletableFuture<List<CalculatedChunk.Blocks>> calculateChunks(List<ChunkPosition> chunkPositions,
                                                             Synchronized<Chunk2ObjectMap<CalculatedChunk.Blocks>> unloadedChunksCache);

    CompletableFuture<List<CalculatedChunk.Entities>> calculateChunkEntities(Collection<ChunkPosition> chunkPositions);

    void injectChunkSections(Chunk chunk);

    boolean isChunkEmpty(Chunk chunk);

    /**
     * Checks whether the chunk is empty without loading it.
     * Returns false if the chunk cannot be accessed without loading it.
     */
    default boolean isChunkEmptyIfLoaded(Chunk chunk) {
        // Versions that read the chunk through its own handle do not load it.
        return isChunkEmpty(chunk);
    }

    @Nullable
    Chunk getChunkIfLoaded(ChunkPosition chunkPosition);

    void startTickingChunk(Island island, Chunk chunk, boolean stop);

    void updateCropsTicker(List<ChunkPosition> chunkPositions, double newCropGrowthMultiplier);

    void shutdown();

    List<Location> getBlockEntities(Chunk chunk);

}

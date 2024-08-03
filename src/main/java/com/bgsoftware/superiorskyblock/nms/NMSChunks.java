package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.CalculatedChunk;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.Counter;
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

    CompletableFuture<List<CalculatedChunk>> calculateChunks(List<ChunkPosition> chunkPositions,
                                                             Map<ChunkPosition, CalculatedChunk> unloadedChunksCache);

    CompletableFuture<KeyMap<Counter>> calculateChunkEntities(Collection<ChunkPosition> chunkPositions);

    void injectChunkSections(Chunk chunk);

    boolean isChunkEmpty(Chunk chunk);

    @Nullable
    Chunk getChunkIfLoaded(ChunkPosition chunkPosition);

    void startTickingChunk(Island island, Chunk chunk, boolean stop);

    void updateCropsTicker(List<ChunkPosition> chunkPositions, double newCropGrowthMultiplier);

    void shutdown();

    List<Location> getBlockEntities(Chunk chunk);

}

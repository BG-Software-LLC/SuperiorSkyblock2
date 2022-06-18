package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.core.SchematicBlock;
import com.bgsoftware.superiorskyblock.core.CalculatedChunk;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import org.bukkit.Chunk;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface NMSChunks {

    void setBiome(List<ChunkPosition> chunkPositions, Biome biome, Collection<Player> playersToUpdate);

    void deleteChunks(Island island, List<ChunkPosition> chunkPositions, Runnable onFinish);

    CompletableFuture<List<CalculatedChunk>> calculateChunks(List<ChunkPosition> chunkPositions);

    void injectChunkSections(Chunk chunk);

    boolean isChunkEmpty(Chunk chunk);

    void refreshChunk(Chunk chunk);

    void refreshLights(Chunk chunk, List<SchematicBlock> blockData);

    Chunk getChunkIfLoaded(ChunkPosition chunkPosition);

    void startTickingChunk(Island island, Chunk chunk, boolean stop);

}

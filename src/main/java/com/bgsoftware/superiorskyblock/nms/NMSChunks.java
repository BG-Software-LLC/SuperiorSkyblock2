package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public interface NMSChunks {

    void setBiome(List<ChunkPosition> chunkPositions, Biome biome, Collection<Player> playersToUpdate);

    void deleteChunks(Island island, List<ChunkPosition> chunkPositions, Runnable onFinish);

}

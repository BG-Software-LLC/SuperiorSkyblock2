package com.bgsoftware.superiorskyblock.api.hooks;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import org.bukkit.World;

import java.util.Collection;

public interface StackedBlocksProvider {

    /**
     * Get all stacked blocks in a chunk.
     *
     * @param world  The world of the chunk.
     * @param chunkX The x-coords of the chunk.
     * @param chunkZ The z-coords of the chunk.
     * @return Collection of pairs representing the stacked blocks.
     * The key of the pair is a Key object of the block.
     * The value of the pair is the amount of the block.
     */
    Collection<Pair<Key, Integer>> getBlocks(World world, int chunkX, int chunkZ);

}

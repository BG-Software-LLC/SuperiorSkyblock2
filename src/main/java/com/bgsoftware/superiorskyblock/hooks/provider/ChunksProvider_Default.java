package com.bgsoftware.superiorskyblock.hooks.provider;

import com.bgsoftware.superiorskyblock.api.hooks.ChunksProvider;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.concurrent.CompletableFuture;

public final class ChunksProvider_Default implements ChunksProvider {

    @Override
    public CompletableFuture<Chunk> loadChunk(World world, int chunkX, int chunkZ) {
        return CompletableFuture.completedFuture(world.getChunkAt(chunkX, chunkZ));
    }

}

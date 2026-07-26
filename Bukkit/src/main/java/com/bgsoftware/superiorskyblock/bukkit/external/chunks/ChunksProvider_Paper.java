package com.bgsoftware.superiorskyblock.bukkit.external.chunks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.ChunksProvider;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

public class ChunksProvider_Paper implements ChunksProvider {

    public ChunksProvider_Paper(SuperiorSkyblockPlugin plugin, JavaPlugin javaPlugin) {

    }

    @Override
    public CompletableFuture<Chunk> loadChunk(World world, int chunkX, int chunkZ) {
        return world.getChunkAtAsync(chunkX, chunkZ);
    }

}

package com.bgsoftware.superiorskyblock.hooks.provider;

import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.function.Consumer;

public final class AsyncProvider_Default implements AsyncProvider {

    @Override
    public void loadChunk(ChunkPosition chunkPosition, Consumer<Chunk> chunkResult) {
        chunkResult.accept(chunkPosition.loadChunk());
    }

    @Override
    public void teleport(Entity entity, Location location, Consumer<Boolean> teleportResult) {
        boolean result = entity.teleport(location);
        if(teleportResult != null)
            teleportResult.accept(result);
    }

}

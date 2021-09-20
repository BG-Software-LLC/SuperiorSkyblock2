package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.hooks.provider.AsyncProvider;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public final class AsyncProvider_Paper implements AsyncProvider {

    @Override
    public void loadChunk(ChunkPosition chunkPosition, Consumer<Chunk> chunkResult) {
        chunkPosition.getWorld().getChunkAtAsync(chunkPosition.getX(), chunkPosition.getZ())
                .whenComplete((chunk, ex) -> chunkResult.accept(chunk));
    }

    @Override
    public void teleport(Entity entity, Location location, Consumer<Boolean> teleportResult) {
        entity.teleportAsync(location).whenComplete((result, ex) -> {
            if(teleportResult != null)
                teleportResult.accept(result);
        });
    }

}

package com.bgsoftware.superiorskyblock.hooks.provider;

import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.function.Consumer;

public interface AsyncProvider {

    void loadChunk(ChunkPosition chunkPosition, Consumer<Chunk> chunkResult);

    default void teleport(Entity entity, Location location){
        teleport(entity, location, r -> {});
    }

    void teleport(Entity entity, Location location, Consumer<Boolean> teleportResult);



}

package com.bgsoftware.superiorskyblock.utils.chunks;

import com.bgsoftware.superiorskyblock.utils.database.Query;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

public final class ChunksTracker {

    private static final Set<ChunkPosition> dirtyChunks = new HashSet<>();
    private static int changesCounter = 0;

    public static void markEmpty(Block block){
        markEmpty(ChunkPosition.of(block.getWorld(), block.getX() >> 4, block.getZ() >> 4));
    }

    public static void markEmpty(Location location){
        markEmpty(ChunkPosition.of(location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4));
    }

    public static void markEmpty(Chunk chunk){
        markEmpty(ChunkPosition.of(chunk.getWorld(), chunk.getX(), chunk.getZ()));
    }

    public static void markEmpty(ChunkSnapshot chunkSnapshot){
        markEmpty(ChunkPosition.of(Bukkit.getWorld(chunkSnapshot.getWorldName()), chunkSnapshot.getX(), chunkSnapshot.getZ()));
    }

    public static void markDirty(Location location){
        markDirty(ChunkPosition.of(location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4));
    }

    public static void markDirty(Block block){
        markDirty(ChunkPosition.of(block.getWorld(), block.getX() >> 4, block.getZ() >> 4));
    }

    public static void markDirty(Chunk chunk){
        markDirty(ChunkPosition.of(chunk.getWorld(), chunk.getX(), chunk.getZ()));
    }

    public static boolean isMarkedDirty(World world, int x, int z){
        return dirtyChunks.contains(ChunkPosition.of(world, x, z));
    }

    public static String serialize(){
        StringBuilder stringBuilder = new StringBuilder();

        for(ChunkPosition dirtyChunk : dirtyChunks){
            stringBuilder.append(";").append(dirtyChunk.getWorld().getName())
                    .append(",").append(dirtyChunk.getX()).append(",").append(dirtyChunk.getZ());
        }

        return stringBuilder.length() <= 0 ? "" : stringBuilder.substring(1);
    }

    public static void deserialize(String serialized){
        String[] dirtyChunkSections = serialized.split(";");
        for(String dirtyChunk : dirtyChunkSections) {
            String[] dirtyChunkSection = dirtyChunk.split(",");
            if(dirtyChunkSection.length == 3) {
                dirtyChunks.add(ChunkPosition.of(Bukkit.getWorld(dirtyChunkSection[0]),
                        Integer.parseInt(dirtyChunkSection[1]), Integer.parseInt(dirtyChunkSection[2])));
            }
        }
    }

    private static void markEmpty(ChunkPosition chunkPosition){
        if(dirtyChunks.remove(chunkPosition))
            increaseCounter();
    }

    private static void markDirty(ChunkPosition chunkPosition){
        if(dirtyChunks.add(chunkPosition))
            increaseCounter();
    }

    private static void increaseCounter(){
        if(++changesCounter >= 25){
            Query.GRID_UPDATE_DIRTY_CHUNKS.getStatementHolder()
                    .setString(serialize())
                    .execute(true);
            changesCounter = 0;
        }
    }

}

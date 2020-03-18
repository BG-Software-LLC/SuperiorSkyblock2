package com.bgsoftware.superiorskyblock.utils.chunks;

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

    private static void markEmpty(ChunkPosition chunkPosition){
        dirtyChunks.remove(chunkPosition);
    }

    private static void markDirty(ChunkPosition chunkPosition){
        dirtyChunks.add(chunkPosition);
    }

}

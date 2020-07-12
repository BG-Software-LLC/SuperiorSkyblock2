package com.bgsoftware.superiorskyblock.utils.chunks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.handlers.GridHandler;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.island.SpawnIsland;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ChunksTracker {

    private static final Registry<Island, Set<ChunkPosition>> dirtyChunks = Registry.createRegistry();
    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private ChunksTracker(){

    }

    public static void markEmpty(Island island, Block block, boolean save){
        markEmpty(island, ChunkPosition.of(block.getWorld(), block.getX() >> 4, block.getZ() >> 4), save);
    }

    public static void markEmpty(Island island, Location location, boolean save){
        markEmpty(island, ChunkPosition.of(location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4), save);
    }

    public static void markEmpty(Island island, Chunk chunk, boolean save){
        markEmpty(island, ChunkPosition.of(chunk.getWorld(), chunk.getX(), chunk.getZ()), save);
    }

    public static void markEmpty(Island island, ChunkSnapshot chunkSnapshot, boolean save){
        markEmpty(island, ChunkPosition.of(Bukkit.getWorld(chunkSnapshot.getWorldName()), chunkSnapshot.getX(), chunkSnapshot.getZ()), save);
    }

    public static void markDirty(Island island, Location location, boolean save){
        markDirty(island, ChunkPosition.of(location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4), save);
    }

    public static void markDirty(Island island, Block block, boolean save){
        markDirty(island, ChunkPosition.of(block.getWorld(), block.getX() >> 4, block.getZ() >> 4), save);
    }

    public static void markDirty(Island island, Chunk chunk, boolean save){
        markDirty(island, ChunkPosition.of(chunk.getWorld(), chunk.getX(), chunk.getZ()), save);
    }

    public static boolean isMarkedDirty(Island island, World world, int x, int z){
        return dirtyChunks.containsKey(island) && dirtyChunks.get(island).contains(ChunkPosition.of(world, x, z));
    }

    public static void removeIsland(Island island){
        if(dirtyChunks.containsKey(island)){
            dirtyChunks.get(island).clear();
            dirtyChunks.remove(island);
        }
    }

    public static String serialize(Island island){
        Set<ChunkPosition> dirtyChunks = ChunksTracker.dirtyChunks.get(island);

        if(dirtyChunks == null)
            return "";

        Map<String, StringBuilder> worlds = new HashMap<>();

        for(ChunkPosition dirtyChunk : dirtyChunks){
            worlds.computeIfAbsent(dirtyChunk.getWorld().getName(), sb -> new StringBuilder())
                    .append(";").append(dirtyChunk.getX()).append(",").append(dirtyChunk.getZ());
        }

        StringBuilder stringBuilder = new StringBuilder();

        for(Map.Entry<String, StringBuilder> entry : worlds.entrySet())
            stringBuilder.append(entry.getKey()).append("=").append(entry.getValue().substring(1)).append("|");

        return stringBuilder.toString();
    }

    public static void deserialize(GridHandler grid, Island island, String serialized){
        if(serialized != null && !serialized.isEmpty()) {
            if (serialized.contains("|")) {
                deserializeNew(island, serialized);
            } else {
                deserializeOld(grid, island, serialized);
            }
        }
    }

    private static void deserializeOld(GridHandler grid, Island island, String serialized){
        try {
            String[] dirtyChunkSections = serialized.split(";");
            for (String dirtyChunk : dirtyChunkSections) {
                String[] dirtyChunkSection = dirtyChunk.split(",");
                if (dirtyChunkSection.length == 3) {
                    ChunkPosition chunkPosition = ChunkPosition.of(dirtyChunkSection[0],
                            Integer.parseInt(dirtyChunkSection[1]), Integer.parseInt(dirtyChunkSection[2]));

                    if (island == null)
                        island = getIsland(grid, chunkPosition);

                    markDirty(island, chunkPosition, false);
                }
            }
        }catch(Exception ignored){}
    }

    private static void deserializeNew(Island island, String serialized){
        String[] serializedSections = serialized.split("\\|");

        for(String section : serializedSections) {
            String[] worldSections = section.split("=");
            if (worldSections.length == 2) {
                String[] dirtyChunkSections = worldSections[1].split(";");
                for (String dirtyChunk : dirtyChunkSections) {
                    String[] dirtyChunkSection = dirtyChunk.split(",");
                    if (dirtyChunkSection.length == 2) {
                        markDirty(island, ChunkPosition.of(worldSections[0],
                                Integer.parseInt(dirtyChunkSection[0]), Integer.parseInt(dirtyChunkSection[1])), false);
                    }
                }
            }
        }
    }

    private static Island getIsland(GridHandler grid, ChunkPosition chunkPosition){
        return grid.getIslandAt(new Location(chunkPosition.getWorld(), chunkPosition.getX() << 4, 100, chunkPosition.getZ() << 4));
    }

    public static void markEmpty(Island island, ChunkPosition chunkPosition, boolean save){
        if(island == null)
            island = getIsland(plugin.getGrid(), chunkPosition);

        if(dirtyChunks.containsKey(island) && dirtyChunks.get(island).remove(chunkPosition) && save)
            ((SIsland) island).saveDirtyChunks();
    }

    private static void markDirty(Island island, ChunkPosition chunkPosition, boolean save){
        if(island == null)
            island = getIsland(plugin.getGrid(), chunkPosition);

        if(dirtyChunks.computeIfAbsent(island, s -> new HashSet<>()).add(chunkPosition) && save && !(island instanceof SpawnIsland))
            ((SIsland) island).saveDirtyChunks();
    }

}

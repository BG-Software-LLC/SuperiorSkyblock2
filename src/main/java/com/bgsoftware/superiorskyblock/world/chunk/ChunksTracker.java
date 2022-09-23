package com.bgsoftware.superiorskyblock.world.chunk;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.database.bridge.IslandsDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.database.serialization.IslandsSerializer;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.island.GridManagerImpl;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChunksTracker {

    private static final Map<UUID, Set<ChunkPosition>> dirtyChunks = new ConcurrentHashMap<>();
    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final Gson gson = new GsonBuilder().create();

    private ChunksTracker() {

    }

    public static void markEmpty(Island island, Block block, boolean save) {
        markEmpty(island, ChunkPosition.of(block.getWorld(), block.getX() >> 4, block.getZ() >> 4), save);
    }

    public static void markEmpty(Island island, Location location, boolean save) {
        markEmpty(island, ChunkPosition.of(location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4), save);
    }

    public static void markEmpty(Island island, Chunk chunk, boolean save) {
        markEmpty(island, ChunkPosition.of(chunk.getWorld(), chunk.getX(), chunk.getZ()), save);
    }

    public static void markEmpty(Island island, ChunkSnapshot chunkSnapshot, boolean save) {
        markEmpty(island, ChunkPosition.of(Bukkit.getWorld(chunkSnapshot.getWorldName()), chunkSnapshot.getX(), chunkSnapshot.getZ()), save);
    }

    public static void markDirty(Island island, Location location, boolean save) {
        markDirty(island, ChunkPosition.of(location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4), save);
    }

    public static void markDirty(Island island, Block block, boolean save) {
        markDirty(island, ChunkPosition.of(block.getWorld(), block.getX() >> 4, block.getZ() >> 4), save);
    }

    public static void markDirty(Island island, Chunk chunk, boolean save) {
        markDirty(island, ChunkPosition.of(chunk.getWorld(), chunk.getX(), chunk.getZ()), save);
    }

    public static boolean isMarkedDirty(Island island, World world, int x, int z) {
        Set<ChunkPosition> islandDirtyChunks = dirtyChunks.get(island.getUniqueId());
        return islandDirtyChunks != null && islandDirtyChunks.contains(ChunkPosition.of(world, x, z));
    }

    public static void removeIsland(Island island) {
        dirtyChunks.remove(island.getUniqueId());
    }

    public static String serialize(Island island) {
        Set<ChunkPosition> islandDirtyChunks = dirtyChunks.getOrDefault(island.getUniqueId(), Collections.emptySet());
        return IslandsSerializer.serializeDirtyChunks(islandDirtyChunks);
    }

    public static void deserialize(GridManagerImpl grid, Island island, @Nullable String serialized) {
        try {
            if (Text.isBlank(serialized))
                throw new JsonSyntaxException("");

            JsonObject dirtyChunksObject = gson.fromJson(serialized, JsonObject.class);
            dirtyChunksObject.entrySet().forEach(dirtyChunkEntry -> {
                String worldName = dirtyChunkEntry.getKey();
                JsonArray dirtyChunksArray = dirtyChunkEntry.getValue().getAsJsonArray();

                dirtyChunksArray.forEach(dirtyChunkElement -> {
                    String[] chunkPositionSections = dirtyChunkElement.getAsString().split(",");
                    try {
                        markDirty(island, ChunkPosition.of(worldName, Integer.parseInt(chunkPositionSections[0]),
                                Integer.parseInt(chunkPositionSections[1])), false);
                    } catch (Exception error) {
                        PluginDebugger.debug(error);
                    }
                });
            });
        } catch (JsonSyntaxException ex) {
            if (serialized != null && !serialized.isEmpty()) {
                if (serialized.contains("|")) {
                    deserializeOldV1(island, serialized);
                } else if (grid != null) {
                    deserializeOldV2(grid, island, serialized);
                }
            }
        }
    }

    private static void deserializeOldV2(GridManagerImpl grid, Island island, String serialized) {
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
        } catch (Exception error) {
            PluginDebugger.debug(error);
        }
    }

    private static void deserializeOldV1(Island island, String serialized) {
        String[] serializedSections = serialized.split("\\|");

        for (String section : serializedSections) {
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

    private static Island getIsland(GridManagerImpl grid, ChunkPosition chunkPosition) {
        return grid.getIslandAt(new Location(chunkPosition.getWorld(), chunkPosition.getX() << 4, 100, chunkPosition.getZ() << 4));
    }

    public static void markEmpty(Island island, ChunkPosition chunkPosition, boolean save) {
        if (island == null)
            island = getIsland(plugin.getGrid(), chunkPosition);

        Set<ChunkPosition> islandDirtyChunks = dirtyChunks.get(island.getUniqueId());

        if (islandDirtyChunks != null && islandDirtyChunks.remove(chunkPosition) && save)
            IslandsDatabaseBridge.saveDirtyChunks(island, islandDirtyChunks);
    }

    public static void markDirty(Island island, ChunkPosition chunkPosition, boolean save) {
        if (island == null)
            island = getIsland(plugin.getGrid(), chunkPosition);

        Set<ChunkPosition> islandDirtyChunks = dirtyChunks.computeIfAbsent(island.getUniqueId(), s -> new HashSet<>());

        if (islandDirtyChunks.add(chunkPosition) && save && !island.isSpawn())
            IslandsDatabaseBridge.saveDirtyChunks(island, islandDirtyChunks);
    }

}

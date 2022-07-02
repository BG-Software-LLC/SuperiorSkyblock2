package com.bgsoftware.superiorskyblock.world.chunk;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandBase;
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

    private static final Map<UUID, Set<ChunkPosition>> dirtyChunks2 = new ConcurrentHashMap<>();
    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final Gson gson = new GsonBuilder().create();

    private ChunksTracker() {

    }

    public static void markEmpty(IslandBase island, Block block, boolean save) {
        markEmpty(island, ChunkPosition.of(block.getWorld(), block.getX() >> 4, block.getZ() >> 4), save);
    }

    public static void markEmpty(IslandBase island, Location location, boolean save) {
        markEmpty(island, ChunkPosition.of(location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4), save);
    }

    public static void markEmpty(IslandBase island, Chunk chunk, boolean save) {
        markEmpty(island, ChunkPosition.of(chunk.getWorld(), chunk.getX(), chunk.getZ()), save);
    }

    public static void markEmpty(IslandBase island, ChunkSnapshot chunkSnapshot, boolean save) {
        markEmpty(island, ChunkPosition.of(Bukkit.getWorld(chunkSnapshot.getWorldName()), chunkSnapshot.getX(), chunkSnapshot.getZ()), save);
    }

    public static void markDirty(IslandBase island, Location location, boolean save) {
        markDirty(island, ChunkPosition.of(location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4), save);
    }

    public static void markDirty(IslandBase island, Block block, boolean save) {
        markDirty(island, ChunkPosition.of(block.getWorld(), block.getX() >> 4, block.getZ() >> 4), save);
    }

    public static void markDirty(IslandBase island, Chunk chunk, boolean save) {
        markDirty(island, ChunkPosition.of(chunk.getWorld(), chunk.getX(), chunk.getZ()), save);
    }

    public static boolean isMarkedDirty(IslandBase island, World world, int x, int z) {
        return isMarkedDirty(island.getUniqueId(), world, x, z);
    }

    public static boolean isMarkedDirty(UUID islandUUID, World world, int x, int z) {
        Set<ChunkPosition> dirtyChunks = ChunksTracker.dirtyChunks2.get(islandUUID);
        return dirtyChunks != null && dirtyChunks.contains(ChunkPosition.of(world, x, z));
    }

    public static Set<ChunkPosition> getDirtyChunks(IslandBase island) {
        Set<ChunkPosition> dirtyChunks = ChunksTracker.dirtyChunks2.get(island.getUniqueId());
        return dirtyChunks == null ? Collections.emptySet() : dirtyChunks;
    }

    public static void removeIsland(IslandBase island) {
        Set<ChunkPosition> dirtyChunks = ChunksTracker.dirtyChunks2.remove(island.getUniqueId());
        if (dirtyChunks != null)
            dirtyChunks.clear();
    }

    public static String serialize(IslandBase island) {
        Set<ChunkPosition> dirtyChunks = ChunksTracker.dirtyChunks2.getOrDefault(island.getUniqueId(), new HashSet<>());
        return IslandsSerializer.serializeDirtyChunks(dirtyChunks);
    }

    public static Set<ChunkPosition> deserialize(@Nullable String serialized) {
        try {
            if (Text.isBlank(serialized))
                throw new JsonSyntaxException("");

            JsonObject dirtyChunksObject = gson.fromJson(serialized, JsonObject.class);

            if (dirtyChunksObject.entrySet().isEmpty())
                return Collections.emptySet();

            Set<ChunkPosition> dirtyChunks = new HashSet<>();

            dirtyChunksObject.entrySet().forEach(dirtyChunkEntry -> {
                String worldName = dirtyChunkEntry.getKey();
                JsonArray dirtyChunksArray = dirtyChunkEntry.getValue().getAsJsonArray();

                dirtyChunksArray.forEach(dirtyChunkElement -> {
                    String[] chunkPositionSections = dirtyChunkElement.getAsString().split(",");
                    dirtyChunks.add(ChunkPosition.of(worldName, Integer.parseInt(chunkPositionSections[0]),
                            Integer.parseInt(chunkPositionSections[1])));
                });
            });

            return dirtyChunks;
        } catch (JsonSyntaxException ex) {
            if (serialized != null && !serialized.isEmpty()) {
                if (serialized.contains("|")) {
                    return deserializeOldV1(serialized);
                } else {
                    return deserializeOldV2(serialized);
                }
            }
        }

        return Collections.emptySet();
    }

    private static Set<ChunkPosition> deserializeOldV2(String serialized) {
        try {
            String[] dirtyChunkSections = serialized.split(";");

            if (dirtyChunkSections.length == 0)
                return Collections.emptySet();

            Set<ChunkPosition> dirtyChunks = new HashSet<>();

            for (String dirtyChunk : dirtyChunkSections) {
                String[] dirtyChunkSection = dirtyChunk.split(",");
                if (dirtyChunkSection.length == 3) {
                    dirtyChunks.add(ChunkPosition.of(dirtyChunkSection[0], Integer.parseInt(dirtyChunkSection[1]),
                            Integer.parseInt(dirtyChunkSection[2])));
                }
            }

            return dirtyChunks;
        } catch (Exception error) {
            PluginDebugger.debug(error);
        }

        return Collections.emptySet();
    }

    private static Set<ChunkPosition> deserializeOldV1(String serialized) {
        String[] serializedSections = serialized.split("\\|");

        if (serializedSections.length == 0)
            return Collections.emptySet();

        Set<ChunkPosition> dirtyChunks = new HashSet<>();

        for (String section : serializedSections) {
            String[] worldSections = section.split("=");
            if (worldSections.length == 2) {
                String[] dirtyChunkSections = worldSections[1].split(";");
                for (String dirtyChunk : dirtyChunkSections) {
                    String[] dirtyChunkSection = dirtyChunk.split(",");
                    if (dirtyChunkSection.length == 2) {
                        dirtyChunks.add(ChunkPosition.of(worldSections[0],
                                Integer.parseInt(dirtyChunkSection[0]), Integer.parseInt(dirtyChunkSection[1])));
                    }
                }
            }
        }

        return dirtyChunks;
    }

    private static Island getIsland(GridManagerImpl grid, ChunkPosition chunkPosition) {
        return grid.getIslandAt(new Location(chunkPosition.getWorld(), chunkPosition.getX() << 4, 100, chunkPosition.getZ() << 4));
    }

    public static void markEmpty(IslandBase island, ChunkPosition chunkPosition, boolean save) {
        if (island == null)
            island = getIsland(plugin.getGrid(), chunkPosition);

        Set<ChunkPosition> dirtyChunks = ChunksTracker.dirtyChunks2.get(island.getUniqueId());

        if (dirtyChunks != null && dirtyChunks.remove(chunkPosition) && save)
            IslandsDatabaseBridge.saveDirtyChunks(island, dirtyChunks);
    }

    public static void markDirty(IslandBase island, ChunkPosition chunkPosition, boolean save) {
        if (island == null)
            island = getIsland(plugin.getGrid(), chunkPosition);

        Set<ChunkPosition> dirtyChunks = ChunksTracker.dirtyChunks2.computeIfAbsent(island.getUniqueId(), s -> new HashSet<>());

        if (dirtyChunks.add(chunkPosition) && save && !island.isSpawn())
            IslandsDatabaseBridge.saveDirtyChunks(island, dirtyChunks);
    }

}

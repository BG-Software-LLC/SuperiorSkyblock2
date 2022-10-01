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
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChunksTracker {

    private static final Map<UUID, BitSet[]> dirtyChunks = new ConcurrentHashMap<>();
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

    public static void removeIsland(Island island) {
        dirtyChunks.remove(island.getUniqueId());
    }

    public static boolean isMarkedDirty(Island island, World world, int x, int z) {
        BitSet[] dirtyChunksWorldBitsets = dirtyChunks.get(island.getUniqueId());

        if (dirtyChunksWorldBitsets == null || world.getEnvironment().ordinal() >= dirtyChunksWorldBitsets.length)
            return false;

        int chunkIndex = getChunkIndex(island, x, z);
        BitSet dirtyChunksBitset = dirtyChunksWorldBitsets[world.getEnvironment().ordinal()];

        return !dirtyChunksBitset.isEmpty() && dirtyChunksBitset.get(chunkIndex);
    }

    public static void markEmpty(@Nullable Island islandParam, ChunkPosition chunkPosition, boolean save) {
        Island island = islandParam == null ? getIsland(plugin.getGrid(), chunkPosition) : islandParam;

        BitSet[] dirtyChunksWorldBitsets = dirtyChunks.get(island.getUniqueId());

        if (dirtyChunksWorldBitsets == null)
            return;

        int chunkIndex = getChunkIndex(island, chunkPosition.getX(), chunkPosition.getZ());
        BitSet dirtyChunksBitset = dirtyChunksWorldBitsets[chunkPosition.getWorld().getEnvironment().ordinal()];

        boolean isMarkedDirty = !dirtyChunksBitset.isEmpty() && dirtyChunksBitset.get(chunkIndex);

        if (isMarkedDirty) {
            dirtyChunksBitset.clear(chunkIndex);
            if (save && !island.isSpawn())
                IslandsDatabaseBridge.saveDirtyChunks(island, convertIntoChunkPositions(island, dirtyChunksWorldBitsets));
        }
    }

    public static void markDirty(@Nullable Island islandParam, ChunkPosition chunkPosition, boolean save) {
        Island island = islandParam == null ? getIsland(plugin.getGrid(), chunkPosition) : islandParam;

        BitSet[] dirtyChunksWorldBitsets = dirtyChunks.computeIfAbsent(island.getUniqueId(), u -> createBitsets(island));

        int chunkIndex = getChunkIndex(island, chunkPosition.getX(), chunkPosition.getZ());
        BitSet dirtyChunksBitset = dirtyChunksWorldBitsets[chunkPosition.getWorld().getEnvironment().ordinal()];

        boolean isMarkedDirty = !dirtyChunksBitset.isEmpty() && dirtyChunksBitset.get(chunkIndex);

        if (!isMarkedDirty) {
            dirtyChunksBitset.set(chunkIndex);
            if (save && !island.isSpawn())
                IslandsDatabaseBridge.saveDirtyChunks(island, convertIntoChunkPositions(island, dirtyChunksWorldBitsets));
        }
    }

    public static String serialize(Island island) {
        BitSet[] dirtyChunksWorldBitsets = dirtyChunks.get(island.getUniqueId());
        return IslandsSerializer.serializeDirtyChunks(convertIntoChunkPositions(island, dirtyChunksWorldBitsets));
    }

    public static void deserialize(Island.Builder builder, @Nullable String serialized) {
        try {
            if (Text.isBlank(serialized))
                throw new JsonSyntaxException("");

            JsonObject dirtyChunksObject = gson.fromJson(serialized, JsonObject.class);
            dirtyChunksObject.entrySet().forEach(dirtyChunkEntry -> {
                String worldName = dirtyChunkEntry.getKey();
                JsonArray dirtyChunksArray = dirtyChunkEntry.getValue().getAsJsonArray();

                dirtyChunksArray.forEach(dirtyChunkElement -> {
                    String[] chunkPositionSections = dirtyChunkElement.getAsString().split(",");
                    builder.setDirtyChunk(worldName, Integer.parseInt(chunkPositionSections[0]),
                            Integer.parseInt(chunkPositionSections[1]));
                });
            });
        } catch (JsonSyntaxException ex) {
            if (serialized != null && !serialized.isEmpty()) {
                if (serialized.contains("|")) {
                    deserializeOldV1(builder, serialized);
                } else {
                    deserializeOldV2(builder, serialized);
                }
            }
        }
    }

    private static void deserializeOldV2(Island.Builder builder, String serialized) {
        try {
            String[] dirtyChunkSections = serialized.split(";");
            for (String dirtyChunk : dirtyChunkSections) {
                String[] dirtyChunkSection = dirtyChunk.split(",");
                if (dirtyChunkSection.length == 3) {
                    builder.setDirtyChunk(dirtyChunkSection[0],
                            Integer.parseInt(dirtyChunkSection[1]), Integer.parseInt(dirtyChunkSection[2]));
                }
            }
        } catch (Exception error) {
            PluginDebugger.debug(error);
        }
    }

    private static void deserializeOldV1(Island.Builder builder, String serialized) {
        String[] serializedSections = serialized.split("\\|");

        for (String section : serializedSections) {
            String[] worldSections = section.split("=");
            if (worldSections.length == 2) {
                String[] dirtyChunkSections = worldSections[1].split(";");
                for (String dirtyChunk : dirtyChunkSections) {
                    String[] dirtyChunkSection = dirtyChunk.split(",");
                    if (dirtyChunkSection.length == 2) {
                        builder.setDirtyChunk(worldSections[0],
                                Integer.parseInt(dirtyChunkSection[0]), Integer.parseInt(dirtyChunkSection[1]));
                    }
                }
            }
        }
    }

    private static Island getIsland(GridManagerImpl grid, ChunkPosition chunkPosition) {
        return grid.getIslandAt(new Location(chunkPosition.getWorld(), chunkPosition.getX() << 4, 100, chunkPosition.getZ() << 4));
    }

    private static int getChunkIndex(Island island, int x, int z) {
        Location minimum = island.getMinimum();
        Location maximum = island.getMaximum();
        int minChunkX = minimum.getBlockX() >> 4;
        int minChunkZ = minimum.getBlockZ() >> 4;
        int maxChunkX = maximum.getBlockX() >> 4;

        int deltaX = x - minChunkX;
        int deltaZ = z - minChunkZ;
        int chunksInXAxis = maxChunkX - minChunkX;

        return deltaX * chunksInXAxis + deltaZ;
    }

    private static BitSet[] createBitsets(Island island) {
        BitSet[] bitSets = new BitSet[World.Environment.values().length];

        Location minimum = island.getMinimum();
        Location maximum = island.getMaximum();
        int minChunkX = minimum.getBlockX() >> 4;
        int minChunkZ = minimum.getBlockZ() >> 4;
        int maxChunkX = maximum.getBlockX() >> 4;
        int maxChunkZ = maximum.getBlockZ() >> 4;

        int chunksInXAxis = maxChunkX - minChunkX;
        int chunksInZAxis = maxChunkZ - minChunkZ;
        int totalChunksCount = chunksInXAxis * chunksInZAxis;

        for (int i = 0; i < bitSets.length; ++i)
            bitSets[i] = new BitSet(totalChunksCount);

        return bitSets;
    }

    private static Set<ChunkPosition> convertIntoChunkPositions(Island island, BitSet[] bitSets) {
        if (bitSets == null)
            return Collections.emptySet();

        Location minimum = island.getMinimum();
        Location maximum = island.getMaximum();
        int minChunkX = minimum.getBlockX() >> 4;
        int minChunkZ = minimum.getBlockZ() >> 4;
        int maxChunkX = maximum.getBlockX() >> 4;

        int chunksInXAxis = maxChunkX - minChunkX;

        Set<ChunkPosition> chunkPositions = new HashSet<>();

        for (int i = 0; i < bitSets.length; ++i) {
            BitSet bitset = bitSets[i];
            if (!bitset.isEmpty() && i < World.Environment.values().length) {
                World world = plugin.getGrid().getIslandsWorld(island, World.Environment.values()[i]);
                if (world != null) {
                    for (int j = bitset.nextSetBit(0); j >= 0; j = bitset.nextSetBit(j + 1)) {
                        int deltaX = j / chunksInXAxis;
                        int deltaZ = j % chunksInXAxis;
                        chunkPositions.add(ChunkPosition.of(world, deltaX + minChunkX, deltaZ + minChunkZ));
                    }
                }
            }
        }

        return chunkPositions;
    }

}

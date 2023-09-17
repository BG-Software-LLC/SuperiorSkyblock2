package com.bgsoftware.superiorskyblock.island.algorithm;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChunkFlags;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandCalculationAlgorithm;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.CalculatedChunk;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.Counter;
import com.bgsoftware.superiorskyblock.core.collections.CompletableFutureList;
import com.bgsoftware.superiorskyblock.core.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.KeyMaps;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.key.types.SpawnerKey;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.profiler.ProfileType;
import com.bgsoftware.superiorskyblock.core.profiler.Profiler;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.world.chunk.ChunkLoadReason;
import org.bukkit.Location;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultIslandCalculationAlgorithm implements IslandCalculationAlgorithm {

    public static final Map<ChunkPosition, CalculatedChunk> CACHED_CALCULATED_CHUNKS = new ConcurrentHashMap<>();

    private static final List<Pair<Key, Key>> MINECART_BLOCK_TYPES = createMinecartBlockTypes();
    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final DefaultIslandCalculationAlgorithm INSTANCE = new DefaultIslandCalculationAlgorithm();

    private DefaultIslandCalculationAlgorithm() {

    }

    public static DefaultIslandCalculationAlgorithm getInstance() {
        return INSTANCE;
    }

    @Override
    public CompletableFuture<IslandCalculationResult> calculateIsland(Island island) {
        CompletableFutureList<List<CalculatedChunk>> chunksToLoad = new CompletableFutureList<>();

        long profiler = Profiler.start(ProfileType.CALCULATE_ISLAND);
        Log.debug(Debug.CHUNK_CALCULATION, island.getOwner().getName());

        if (!plugin.getProviders().hasSnapshotsSupport()) {
            IslandUtils.getChunkCoords(island, IslandChunkFlags.ONLY_PROTECTED | IslandChunkFlags.NO_EMPTY_CHUNKS).values()
                    .forEach(worldChunks -> chunksToLoad.add(plugin.getNMSChunks().calculateChunks(worldChunks, CACHED_CALCULATED_CHUNKS)));
        } else {
            IslandUtils.getAllChunksAsync(island, IslandChunkFlags.ONLY_PROTECTED | IslandChunkFlags.NO_EMPTY_CHUNKS,
                    ChunkLoadReason.BLOCKS_RECALCULATE, plugin.getProviders()::takeSnapshots).forEach(completableFuture -> {
                CompletableFuture<List<CalculatedChunk>> calculateCompletable = new CompletableFuture<>();
                completableFuture.whenComplete((chunk, ex) -> plugin.getNMSChunks()
                        .calculateChunks(Collections.singletonList(ChunkPosition.of(chunk)), CACHED_CALCULATED_CHUNKS).whenComplete(
                                (pair, ex2) -> calculateCompletable.complete(pair)));
                chunksToLoad.add(calculateCompletable);
            });
        }

        BlockCountsTracker blockCounts = new BlockCountsTracker();
        CompletableFuture<IslandCalculationResult> result = new CompletableFuture<>();

        Set<SpawnerInfo> spawnersToCheck = new HashSet<>();
        Set<ChunkPosition> chunksToCheck = new HashSet<>();

        BukkitExecutor.createTask().runAsync(v -> {
            chunksToLoad.forEachCompleted(worldCalculatedChunks -> worldCalculatedChunks.forEach(calculatedChunk -> {
                Log.debugResult(Debug.CHUNK_CALCULATION, "Chunk Finished", calculatedChunk.getPosition());

                // We want to remove spawners from the chunkInfo, as it will be used later
                calculatedChunk.getBlockCounts().removeIf(key -> key instanceof SpawnerKey);

                blockCounts.addCounts(calculatedChunk.getBlockCounts());

                // Load spawners
                for (Location location : calculatedChunk.getSpawners()) {
                    Pair<Integer, String> spawnerInfo = plugin.getProviders().getSpawnersProvider().getSpawner(location);

                    if (spawnerInfo.getValue() == null) {
                        spawnersToCheck.add(new SpawnerInfo(location, spawnerInfo.getKey()));
                    } else {
                        Key spawnerKey = Keys.ofSpawner(spawnerInfo.getValue(), location);
                        blockCounts.addCounts(spawnerKey, spawnerInfo.getKey());
                    }
                }

                ChunkPosition chunkPosition = calculatedChunk.getPosition();

                // Load stacked blocks
                Collection<Pair<Key, Integer>> stackedBlocks = plugin.getProviders().getStackedBlocksProvider()
                        .getBlocks(chunkPosition.getWorld(), chunkPosition.getX(), chunkPosition.getZ());

                if (stackedBlocks == null) {
                    chunksToCheck.add(calculatedChunk.getPosition());
                } else for (Pair<Key, Integer> pair : stackedBlocks) {
                    blockCounts.addCounts(pair.getKey(), pair.getValue() - 1);
                }

                // Load built-in stacked blocks
                plugin.getStackedBlocks().forEach(calculatedChunk.getPosition(), stackedBlock ->
                        blockCounts.addCounts(stackedBlock.getBlockKey(), stackedBlock.getAmount() - 1));

                plugin.getProviders().releaseSnapshots(calculatedChunk.getPosition());
            }), result::completeExceptionally);
        }).runSync(v -> {
            Key blockKey;
            int blockCount;

            // Calculate spawner counts
            for (SpawnerInfo spawnerInfo : spawnersToCheck) {
                try {
                    blockKey = Keys.of(spawnerInfo.location.getBlock());
                    blockCount = spawnerInfo.spawnerCount;

                    if (blockCount <= 0) {
                        Pair<Integer, String> spawnersProviderInfo = plugin.getProviders()
                                .getSpawnersProvider().getSpawner(spawnerInfo.location);

                        blockCount = spawnersProviderInfo.getKey();

                        String entityType = spawnersProviderInfo.getValue();
                        if (entityType != null) {
                            blockKey = Keys.ofSpawner(entityType, spawnerInfo.location);
                        }
                    }

                    blockCounts.addCounts(blockKey, blockCount);
                } catch (Throwable ignored) {
                }
            }
            spawnersToCheck.clear();

            // Calculate stacked block counts
            for (ChunkPosition chunkPosition : chunksToCheck) {
                for (Pair<Key, Integer> pair : plugin.getProviders().getStackedBlocksProvider()
                        .getBlocks(chunkPosition.getWorld(), chunkPosition.getX(), chunkPosition.getZ())) {
                    blockCounts.addCounts(pair.getKey(), pair.getValue() - 1);
                }
            }

            // Calculate minecart block counts
            MINECART_BLOCK_TYPES.forEach(minecartTypes -> {
                int count = island.getEntitiesTracker().getEntityCount(minecartTypes.getKey());
                if (count > 0)
                    blockCounts.addCounts(minecartTypes.getValue(), count);
            });

            chunksToCheck.clear();

            Profiler.end(profiler);

            result.complete(blockCounts);
        });

        return result;
    }

    private static List<Pair<Key, Key>> createMinecartBlockTypes() {
        List<Pair<Key, Key>> minecartBlockTypes = new LinkedList<>();

        minecartBlockTypes.add(new Pair<>(ConstantKeys.ENTITY_MINECART_COMMAND, ConstantKeys.COMMAND_BLOCK));
        minecartBlockTypes.add(new Pair<>(ConstantKeys.ENTITY_MINECART_CHEST, ConstantKeys.CHEST));
        minecartBlockTypes.add(new Pair<>(ConstantKeys.ENTITY_MINECART_FURNACE, ConstantKeys.FURNACE));
        minecartBlockTypes.add(new Pair<>(ConstantKeys.ENTITY_MINECART_TNT, ConstantKeys.TNT));
        minecartBlockTypes.add(new Pair<>(ConstantKeys.ENTITY_MINECART_HOPPER, ConstantKeys.HOPPER));
        minecartBlockTypes.add(new Pair<>(ConstantKeys.ENTITY_MINECART_MOB_SPAWNER, ConstantKeys.MOB_SPAWNER));

        return Collections.unmodifiableList(minecartBlockTypes);
    }

    private static class BlockCountsTracker implements IslandCalculationResult {

        private final KeyMap<BigInteger> blockCounts = KeyMaps.createConcurrentHashMap(KeyIndicator.MATERIAL);

        @Override
        public Map<Key, BigInteger> getBlockCounts() {
            return blockCounts;
        }

        public void addCounts(Key blockKey, int amount) {
            blockCounts.put(blockKey, blockCounts.getRaw(blockKey, BigInteger.ZERO).add(BigInteger.valueOf(amount)));
        }

        public void addCounts(KeyMap<Counter> other) {
            other.forEach((key, counter) -> addCounts(key, counter.get()));
        }
    }

    private static class SpawnerInfo {

        private final Location location;
        private final int spawnerCount;

        SpawnerInfo(Location location, int spawnerCount) {
            this.location = location;
            this.spawnerCount = spawnerCount;
        }

    }

}

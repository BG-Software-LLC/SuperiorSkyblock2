package com.bgsoftware.superiorskyblock.island.algorithms;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandCalculationAlgorithm;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.key.KeyImpl;
import com.bgsoftware.superiorskyblock.structure.CompletableFutureList;
import com.bgsoftware.superiorskyblock.threads.Executor;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.bgsoftware.superiorskyblock.world.blocks.stacked.StackedBlock;
import com.bgsoftware.superiorskyblock.world.chunks.CalculatedChunk;
import com.bgsoftware.superiorskyblock.world.chunks.ChunkPosition;
import org.bukkit.Location;
import org.bukkit.block.CreatureSpawner;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class DefaultIslandCalculationAlgorithm implements IslandCalculationAlgorithm {

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

        if (!plugin.getProviders().hasSnapshotsSupport()) {
            IslandUtils.getChunkCoords(island, true, true).values().forEach(worldChunks ->
                    chunksToLoad.add(plugin.getNMSChunks().calculateChunks(worldChunks)));
        } else {
            IslandUtils.getAllChunksAsync(island, true, true, plugin.getProviders()::takeSnapshots).forEach(completableFuture -> {
                CompletableFuture<List<CalculatedChunk>> calculateCompletable = new CompletableFuture<>();
                completableFuture.whenComplete((chunk, ex) -> plugin.getNMSChunks()
                        .calculateChunks(Collections.singletonList(ChunkPosition.of(chunk))).whenComplete(
                                (pair, ex2) -> calculateCompletable.complete(pair)));
                chunksToLoad.add(calculateCompletable);
            });
        }

        BlockCountsTracker blockCounts = new BlockCountsTracker();
        CompletableFuture<IslandCalculationResult> result = new CompletableFuture<>();

        Set<Pair<Location, Integer>> spawnersToCheck = new HashSet<>();
        Set<ChunkPosition> chunksToCheck = new HashSet<>();

        Executor.createTask().runAsync(v -> {
            chunksToLoad.forEachCompleted(worldCalculatedChunks -> worldCalculatedChunks.forEach(calculatedChunk -> {
                PluginDebugger.debug("Action: Chunk Calculation, Island: " + island.getOwner().getName() + ", Chunk: " + calculatedChunk.getPosition());

                // We want to remove spawners from the chunkInfo, as it will be used later
                calculatedChunk.getBlockCounts().removeIf(key ->
                        key.getGlobalKey().equals(ConstantKeys.MOB_SPAWNER.getGlobalKey()));

                blockCounts.addCounts(calculatedChunk.getBlockCounts());

                // Load spawners
                for (Location location : calculatedChunk.getSpawners()) {
                    Pair<Integer, String> spawnerInfo = plugin.getProviders().getSpawnersProvider().getSpawner(location);

                    if (spawnerInfo.getValue() == null) {
                        spawnersToCheck.add(new Pair<>(location, spawnerInfo.getKey()));
                    } else {
                        Key spawnerKey = KeyImpl.of(Materials.SPAWNER.toBukkitType().name() + "", spawnerInfo.getValue(), location);
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
                for (StackedBlock stackedBlock : plugin.getStackedBlocks().getRealStackedBlocks(calculatedChunk.getPosition()))
                    blockCounts.addCounts(stackedBlock.getBlockKey(), stackedBlock.getAmount() - 1);

                plugin.getProviders().releaseSnapshots(calculatedChunk.getPosition());
            }), result::completeExceptionally);
        }).runSync(v -> {
            Key blockKey;
            int blockCount;

            for (Pair<Location, Integer> pair : spawnersToCheck) {
                try {
                    CreatureSpawner creatureSpawner = (CreatureSpawner) pair.getKey().getBlock().getState();
                    blockKey = KeyImpl.of(Materials.SPAWNER.toBukkitType().name() + "", creatureSpawner.getSpawnedType() + "", pair.getKey());
                    blockCount = pair.getValue();

                    if (blockCount <= 0) {
                        Pair<Integer, String> spawnerInfo = plugin.getProviders().getSpawnersProvider().getSpawner(pair.getKey());

                        String entityType = spawnerInfo.getValue();
                        if (entityType == null)
                            entityType = creatureSpawner.getSpawnedType().name();

                        blockCount = spawnerInfo.getKey();
                        blockKey = KeyImpl.of(Materials.SPAWNER.toBukkitType().name() + "", entityType, pair.getKey());
                    }

                    blockCounts.addCounts(blockKey, blockCount);
                } catch (Throwable error) {
                    PluginDebugger.debug(error);
                }
            }
            spawnersToCheck.clear();

            for (ChunkPosition chunkPosition : chunksToCheck) {
                for (Pair<Key, Integer> pair : plugin.getProviders().getStackedBlocksProvider()
                        .getBlocks(chunkPosition.getWorld(), chunkPosition.getX(), chunkPosition.getZ())) {
                    blockCounts.addCounts(pair.getKey(), pair.getValue() - 1);
                }
            }

            chunksToCheck.clear();

            result.complete(blockCounts);
        });

        return result;
    }

    private static class BlockCountsTracker implements IslandCalculationResult {

        private final KeyMap<BigInteger> blockCounts = KeyMap.createConcurrentKeyMap();

        @Override
        public Map<Key, BigInteger> getBlockCounts() {
            return blockCounts;
        }

        public void addCounts(Key blockKey, int amount) {
            blockCounts.put(blockKey, blockCounts.getRaw(blockKey, BigInteger.ZERO).add(BigInteger.valueOf(amount)));
        }

        public void addCounts(KeyMap<Integer> other) {
            other.forEach(this::addCounts);
        }
    }

}

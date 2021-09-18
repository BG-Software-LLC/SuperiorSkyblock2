package com.bgsoftware.superiorskyblock.island.algorithms;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.algorithms.IslandCalculationAlgorithm;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.key.dataset.KeyMap;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.bgsoftware.superiorskyblock.structure.CompletableFutureList;
import com.bgsoftware.superiorskyblock.utils.chunks.CalculatedChunk;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.world.blocks.StackedBlock;
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

    private final Island island;

    public DefaultIslandCalculationAlgorithm(Island island) {
        this.island = island;
    }

    @Override
    public CompletableFuture<IslandCalculationResult> calculateIsland() {
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
                SuperiorSkyblockPlugin.debug("Action: Chunk Calculation, Island: " + island.getOwner().getName() + ", Chunk: " + calculatedChunk.getPosition());

                // We want to remove spawners from the chunkInfo, as it will be used later
                calculatedChunk.getBlockCounts().removeIf(key ->
                        key.getGlobalKey().equals(ConstantKeys.MOB_SPAWNER.getGlobalKey()));

                blockCounts.addCounts(calculatedChunk.getBlockCounts());

                // Load spawners
                for (Location location : calculatedChunk.getSpawners()) {
                    Pair<Integer, String> spawnerInfo = plugin.getProviders().getSpawner(location);

                    if (spawnerInfo.getValue() == null) {
                        spawnersToCheck.add(new Pair<>(location, spawnerInfo.getKey()));
                    } else {
                        Key spawnerKey = Key.of(Materials.SPAWNER.toBukkitType().name() + "", spawnerInfo.getValue(), location);
                        blockCounts.addCounts(spawnerKey, spawnerInfo.getKey());
                    }
                }

                // Load stacked blocks
                Collection<Pair<com.bgsoftware.superiorskyblock.api.key.Key, Integer>> stackedBlocks =
                        plugin.getProviders().getBlocks(calculatedChunk.getPosition());

                if (stackedBlocks == null) {
                    chunksToCheck.add(calculatedChunk.getPosition());
                } else for (Pair<com.bgsoftware.superiorskyblock.api.key.Key, Integer> pair : stackedBlocks) {
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
                    blockKey = Key.of(Materials.SPAWNER.toBukkitType().name() + "", creatureSpawner.getSpawnedType() + "", pair.getKey());
                    blockCount = pair.getValue();

                    if (blockCount <= 0) {
                        Pair<Integer, String> spawnerInfo = plugin.getProviders().getSpawner(pair.getKey());

                        String entityType = spawnerInfo.getValue();
                        if (entityType == null)
                            entityType = creatureSpawner.getSpawnedType().name();

                        blockCount = spawnerInfo.getKey();
                        blockKey = Key.of(Materials.SPAWNER.toBukkitType().name() + "", entityType, pair.getKey());
                    }

                    blockCounts.addCounts(blockKey, blockCount);
                } catch (Throwable ignored) {
                }
            }
            spawnersToCheck.clear();

            for (ChunkPosition chunkPosition : chunksToCheck) {
                for (Pair<com.bgsoftware.superiorskyblock.api.key.Key, Integer> pair : plugin.getProviders().getBlocks(chunkPosition)) {
                    blockCounts.addCounts(pair.getKey(), pair.getValue() - 1);
                }
            }

            chunksToCheck.clear();

            result.complete(blockCounts);
        });

        return result;
    }

    private static class BlockCountsTracker implements IslandCalculationResult {

        private final KeyMap<BigInteger> blockCounts = new KeyMap<>();

        @Override
        public Map<com.bgsoftware.superiorskyblock.api.key.Key, BigInteger> getBlockCounts() {
            return blockCounts;
        }

        public void addCounts(com.bgsoftware.superiorskyblock.api.key.Key blockKey, int amount) {
            blockCounts.put(blockKey, blockCounts.getRaw(blockKey, BigInteger.ZERO).add(BigInteger.valueOf(amount)));
        }

        public void addCounts(KeyMap<Integer> other) {
            other.forEach(this::addCounts);
        }
    }

}

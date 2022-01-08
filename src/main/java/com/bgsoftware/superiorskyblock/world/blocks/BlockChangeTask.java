package com.bgsoftware.superiorskyblock.world.blocks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeCropGrowth;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.threads.Executor;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.world.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.world.chunks.ChunksProvider;
import com.bgsoftware.superiorskyblock.world.chunks.ChunksTracker;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class BlockChangeTask {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final Map<ChunkPosition, List<BlockData>> blocksCache = Maps.newConcurrentMap();
    private final Set<ChunkPosition> interactedChunks = new HashSet<>();
    private final Island island;

    private boolean submitted = false;
    private boolean failed = false;

    public BlockChangeTask(Island island) {
        this.island = island;
    }

    public void setBlock(Location location, int combinedId, byte skyLightLevel, byte blockLightLevel, CompoundTag statesTag, CompoundTag tileEntity) {
        Preconditions.checkArgument(!submitted, "This MultiBlockChange was already submitted.");
        blocksCache.computeIfAbsent(ChunkPosition.of(location), pairs -> new ArrayList<>())
                .add(new BlockData(location, combinedId, skyLightLevel, blockLightLevel, statesTag, tileEntity));
    }

    public void submitUpdate(Runnable onFinish, Consumer<Throwable> onFailure) {
        try {
            Preconditions.checkArgument(!submitted, "This MultiBlockChange was already submitted.");

            submitted = true;

            List<CompletableFuture<Chunk>> chunkFutures = new ArrayList<>();

            for (Map.Entry<ChunkPosition, List<BlockData>> entry : blocksCache.entrySet()) {
                chunkFutures.add(ChunksProvider.loadChunk(entry.getKey(), chunk -> {
                    if (failed)
                        return;

                    try {
                        interactedChunks.add(entry.getKey());

                        IslandUtils.deleteChunks(island, Collections.singletonList(entry.getKey()), null);

                        boolean cropGrowthEnabled = BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeCropGrowth.class);
                        if (cropGrowthEnabled && island.isInsideRange(chunk))
                            plugin.getNMSChunks().startTickingChunk(island, chunk, false);

                        ChunksTracker.markDirty(island, chunk, false);

                        entry.getValue().forEach(blockData -> blockData.doPrePlace(island));

                        plugin.getNMSWorld().setBlocks(chunk, entry.getValue());

                        if (island.getOwner().isOnline())
                            entry.getValue().forEach(blockData -> blockData.doPostPlace(island));

                        plugin.getNMSChunks().refreshChunk(chunk);
                        Executor.sync(() -> plugin.getNMSChunks().refreshLights(chunk, entry.getValue()), 10L);
                    } catch (Throwable error) {
                        failed = true;
                        if (onFailure != null)
                            onFailure.accept(error);
                    }
                }));
            }

            if (onFinish != null) {
                CompletableFuture.allOf(chunkFutures.toArray(new CompletableFuture[0])).whenComplete((v, error) -> {
                    if (!failed)
                        onFinish.run();
                });
            }
        } catch (Throwable error) {
            failed = true;
            if (onFailure != null)
                onFailure.accept(error);
        } finally {
            blocksCache.clear();
        }
    }

    public Set<ChunkPosition> getLoadedChunks() {
        return Collections.unmodifiableSet(interactedChunks);
    }

}

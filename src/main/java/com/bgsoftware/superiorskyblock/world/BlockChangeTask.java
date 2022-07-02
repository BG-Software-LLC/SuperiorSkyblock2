package com.bgsoftware.superiorskyblock.world;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.SchematicBlock;
import com.bgsoftware.superiorskyblock.core.SchematicBlockData;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeCropGrowth;
import com.bgsoftware.superiorskyblock.world.chunk.ChunkLoadReason;
import com.bgsoftware.superiorskyblock.world.chunk.ChunksProvider;
import com.bgsoftware.superiorskyblock.world.chunk.ChunksTracker;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class BlockChangeTask {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final Map<ChunkPosition, List<SchematicBlock>> blocksCache = Maps.newConcurrentMap();
    private final Set<ChunkPosition> interactedChunks = new HashSet<>();
    private final Island island;

    private boolean submitted = false;
    private boolean failed = false;

    public BlockChangeTask(Island island) {
        this.island = island;
    }

    public void setBlock(Location location, SchematicBlockData schematicBlockData) {
        Preconditions.checkArgument(!submitted, "This MultiBlockChange was already submitted.");
        blocksCache.computeIfAbsent(ChunkPosition.of(location), pairs -> new LinkedList<>()).add(new SchematicBlock(location, schematicBlockData));
    }

    public void submitUpdate(Runnable onFinish, Consumer<Throwable> onFailure) {
        try {
            Preconditions.checkArgument(!submitted, "This MultiBlockChange was already submitted.");

            submitted = true;

            @SuppressWarnings("unchecked")
            CompletableFuture<Chunk>[] chunkFutures = new CompletableFuture[blocksCache.size()];
            int currentIndex = 0;

            for (Map.Entry<ChunkPosition, List<SchematicBlock>> entry : blocksCache.entrySet()) {
                chunkFutures[currentIndex++] = ChunksProvider.loadChunk(entry.getKey(), ChunkLoadReason.SCHEMATIC_PLACE, chunk -> {
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

                        if (plugin.getSettings().isLightsUpdate())
                            BukkitExecutor.sync(() -> plugin.getNMSChunks().refreshLights(chunk, entry.getValue()), 10L);
                    } catch (Throwable error) {
                        failed = true;
                        if (onFailure != null)
                            onFailure.accept(error);
                    }
                });
            }

            if (onFinish != null) {
                CompletableFuture.allOf(chunkFutures).whenComplete((v, error) -> {
                    try {
                        if (!failed)
                            onFinish.run();
                    } catch (Throwable error2) {
                        if (onFailure != null)
                            onFailure.accept(error2);
                    }
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

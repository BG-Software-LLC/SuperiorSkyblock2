package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.cache.IslandCache;
import com.bgsoftware.superiorskyblock.api.service.world.WorldRecordService;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.mutable.MutableBoolean;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.algorithm.DefaultIslandCalculationAlgorithm;
import com.bgsoftware.superiorskyblock.island.cache.IslandCacheKeys;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeCropGrowth;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeEntityLimits;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ChunksListener extends AbstractGameEventListener {

    private final LazyReference<WorldRecordService> worldRecordService = new LazyReference<WorldRecordService>() {
        @Override
        protected WorldRecordService create() {
            return plugin.getServices().getService(WorldRecordService.class);
        }
    };

    public ChunksListener(SuperiorSkyblockPlugin plugin) {
        super(plugin);

        registerCallback(GameEventType.CHUNK_UNLOAD_EVENT, GameEventPriority.MONITOR, this::onChunkUnload);
        registerCallback(GameEventType.WORLD_UNLOAD_EVENT, GameEventPriority.MONITOR, this::onWorldUnload);
        registerCallback(GameEventType.CHUNK_LOAD_EVENT, GameEventPriority.MONITOR, this::onChunkLoad);
    }

    private void onChunkUnload(GameEvent<GameEventArgs.ChunkUnloadEvent> e) {
        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(e.getArgs().chunk.getWorld()))
            return;

        handleChunkUnload(e.getArgs().chunk);
    }

    private void onWorldUnload(GameEvent<GameEventArgs.WorldUnloadEvent> e) {
        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(e.getArgs().world))
            return;

        for (Chunk loadedChunk : e.getArgs().world.getLoadedChunks())
            handleChunkUnload(loadedChunk);
    }

    private void onChunkLoad(GameEvent<GameEventArgs.ChunkLoadEvent> e) {
        handleChunkLoad(e.getArgs().chunk, e.getArgs().isNewChunk);
    }

    /* INTERNAL */

    private void handleChunkUnload(Chunk chunk) {
        plugin.getStackedBlocks().removeStackedBlockHolograms(chunk);

        List<Island> chunkIslands = plugin.getGrid().getIslandsAt(chunk);
        chunkIslands.forEach(island -> {
            if (!island.isSpawn())
                handleIslandChunkUnload(island, chunk);
        });
    }

    private void handleIslandChunkUnload(Island island, Chunk chunk) {
        if (BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeCropGrowth.class))
            plugin.getNMSChunks().startTickingChunk(island, chunk, true);

        if (!plugin.getNMSChunks().isChunkEmpty(chunk))
            island.markChunkDirty(chunk.getWorld(), chunk.getX(), chunk.getZ(), true);

        Arrays.stream(chunk.getEntities()).forEach(this.worldRecordService.get()::recordEntityDespawn);
    }

    private void handleChunkLoad(Chunk chunk, boolean isNewChunk) {
        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(chunk.getWorld()))
            return;

        List<Island> chunkIslands = plugin.getGrid().getIslandsAt(chunk);
        chunkIslands.forEach(island -> {
            if (!island.isSpawn()) {
                try (ChunkPosition chunkPosition = ChunkPosition.of(chunk)) {
                    handleIslandChunkLoad(island, chunk, chunkPosition, isNewChunk);
                }
            }
        });
    }

    private void handleIslandChunkLoad(Island island, Chunk chunk, ChunkPosition chunkPosition, boolean isNewChunk) {
        World world = chunk.getWorld();
        Dimension dimension = plugin.getGrid().getIslandsWorldDimension(world);

        if (isNewChunk && dimension == plugin.getSettings().getWorlds().getDefaultWorldDimension()) {
            Biome defaultWorldBiome = IslandUtils.getDefaultWorldBiome(dimension);
            // We want to update the biome for new island chunks.
            if (island.getBiome() != defaultWorldBiome) {
                List<Player> playersToUpdate = new SequentialListBuilder<Player>()
                        .filter(player -> player.getWorld().equals(world))
                        .build(island.getAllPlayersInside(), SuperiorPlayer::asPlayer);
                plugin.getNMSChunks().setBiome(Collections.singletonList(chunkPosition), island.getBiome(), playersToUpdate);
            }
        }

        plugin.getNMSChunks().injectChunkSections(chunk);

        IslandCache islandCache = island.getCache();

        Set<Chunk> pendingLoadedChunksForIsland = islandCache.computeIfAbsent(IslandCacheKeys.PENDING_LOADED_CHUNKS,
                k -> new LinkedHashSet<>());
        pendingLoadedChunksForIsland.add(chunk);

        boolean cropGrowthEnabled = BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeCropGrowth.class);
        if (cropGrowthEnabled && island.isInsideRange(chunk))
            plugin.getNMSChunks().startTickingChunk(island, chunk, false);

        if (!plugin.getNMSChunks().isChunkEmpty(chunk))
            island.markChunkDirty(world, chunk.getX(), chunk.getZ(), true);

        BlockPosition islandCenter = island.getCenterPosition();

        boolean entityLimitsEnabled = BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeEntityLimits.class);
        MutableBoolean recalculateEntities = new MutableBoolean(false);

        if (chunk.getX() == (islandCenter.getX() >> 4) && chunk.getZ() == (islandCenter.getZ() >> 4)) {
            if (dimension == plugin.getSettings().getWorlds().getDefaultWorldDimension()) {
                Block chunkBlock = chunk.getBlock(0, 100, 0);
                island.setBiome(world.getBiome(chunkBlock.getX(), chunkBlock.getZ()), false);
            }

            if (entityLimitsEnabled)
                recalculateEntities.set(true);
        }

        BukkitExecutor.sync(() -> {
            if (chunk.isLoaded())
                // Update holograms of stacked blocks in delay so the chunk is entirely loaded.
                plugin.getStackedBlocks().updateStackedBlockHolograms(chunk);
        }, 10L);

        BukkitExecutor.sync(() -> {
            if (!pendingLoadedChunksForIsland.remove(chunk) || !chunk.isLoaded())
                return;

            // If we cannot recalculate entities at this moment, we want to track entities normally.
            if (!island.getEntitiesTracker().canRecalculateEntityCounts())
                recalculateEntities.set(false);

            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                for (Entity entity : chunk.getEntities()) {
                    // We want to delete old holograms of stacked blocks + count entities for the chunk
                    if (entity instanceof ArmorStand && isOldHologram((ArmorStand) entity) &&
                            plugin.getStackedBlocks().getStackedBlockAmount(entity.getLocation(wrapper.getHandle()).subtract(0, 1, 0)) > 1) {
                        entity.remove();
                    }
                }
            }

            if (recalculateEntities.get()) {
                island.getEntitiesTracker().recalculateEntityCounts();
                pendingLoadedChunksForIsland.clear();
                islandCache.remove(IslandCacheKeys.PENDING_LOADED_CHUNKS);
            }
        }, 2L);

        DefaultIslandCalculationAlgorithm.CACHED_CALCULATED_CHUNKS.write(cache -> cache.remove(chunkPosition));
    }

    private static boolean isOldHologram(ArmorStand armorStand) {
        return !armorStand.hasGravity() && armorStand.isSmall() && !armorStand.isVisible() &&
                armorStand.isCustomNameVisible() && armorStand.isMarker() && armorStand.getCustomName() != null;
    }

}

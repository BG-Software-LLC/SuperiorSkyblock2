package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.service.world.WorldRecordService;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.collections.ArrayMap;
import com.bgsoftware.superiorskyblock.core.mutable.MutableBoolean;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.algorithm.DefaultIslandCalculationAlgorithm;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeCropGrowth;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeEntityLimits;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ChunksListener implements Listener {

    private final Map<UUID, Set<Chunk>> pendingLoadedChunks = new ArrayMap<>();

    private final SuperiorSkyblockPlugin plugin;
    private final LazyReference<WorldRecordService> worldRecordService = new LazyReference<WorldRecordService>() {
        @Override
        protected WorldRecordService create() {
            return plugin.getServices().getService(WorldRecordService.class);
        }
    };

    public ChunksListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onChunkUnloadMonitor(ChunkUnloadEvent e) {
        handleChunkUnload(e.getChunk());
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onWorldUnload(WorldUnloadEvent e) {
        for (Chunk loadedChunk : e.getWorld().getLoadedChunks())
            handleChunkUnload(loadedChunk);
    }

    private void handleChunkUnload(Chunk chunk) {
        if (!plugin.getGrid().isIslandsWorld(chunk.getWorld()))
            return;

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

    @EventHandler
    private void onChunkLoad(ChunkLoadEvent e) {
        handleChunkLoad(e.getChunk(), e.isNewChunk());
    }

    private void handleChunkLoad(Chunk chunk, boolean isNewChunk) {
        if (!plugin.getGrid().isIslandsWorld(chunk.getWorld()))
            return;

        List<Island> chunkIslands = plugin.getGrid().getIslandsAt(chunk);
        chunkIslands.forEach(island -> {
            if (!island.isSpawn())
                handleIslandChunkLoad(island, chunk, isNewChunk);
        });
    }

    private void handleIslandChunkLoad(Island island, Chunk chunk, boolean isNewChunk) {
        ChunkPosition chunkPosition = ChunkPosition.of(chunk);


        World world = chunk.getWorld();
        Dimension dimension = plugin.getGrid().getIslandsWorldDimension(world);

        if (isNewChunk && dimension == plugin.getSettings().getWorlds().getDefaultWorldDimension()) {
            Biome defaultWorldBiome = IslandUtils.getDefaultWorldBiome(dimension);
            // We want to update the biome for new island chunks.
            if (island.getBiome() != defaultWorldBiome) {
                List<Player> playersToUpdate = new SequentialListBuilder<Player>()
                        .filter(player -> player.getWorld().equals(world))
                        .build(island.getAllPlayersInside(), SuperiorPlayer::asPlayer);
                plugin.getNMSChunks().setBiome(Collections.singletonList(ChunkPosition.of(chunk)), island.getBiome(), playersToUpdate);
            }
        }

        plugin.getNMSChunks().injectChunkSections(chunk);

        Set<Chunk> pendingLoadedChunksForIsland = this.pendingLoadedChunks.computeIfAbsent(island.getUniqueId(), u -> new LinkedHashSet<>());
        pendingLoadedChunksForIsland.add(chunk);

        boolean cropGrowthEnabled = BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeCropGrowth.class);
        if (cropGrowthEnabled && island.isInsideRange(chunk))
            plugin.getNMSChunks().startTickingChunk(island, chunk, false);

        if (!plugin.getNMSChunks().isChunkEmpty(chunk))
            island.markChunkDirty(world, chunk.getX(), chunk.getZ(), true);

        Location islandCenter = island.getCenter(dimension);

        boolean entityLimitsEnabled = BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeEntityLimits.class);
        MutableBoolean recalculateEntities = new MutableBoolean(false);

        if (chunk.getX() == (islandCenter.getBlockX() >> 4) && chunk.getZ() == (islandCenter.getBlockZ() >> 4)) {
            if (dimension == plugin.getSettings().getWorlds().getDefaultWorldDimension()) {
                Block chunkBlock = chunk.getBlock(0, 100, 0);
                island.setBiome(world.getBiome(chunkBlock.getX(), chunkBlock.getZ()), false);
            }

            if (entityLimitsEnabled)
                recalculateEntities.set(true);
        }

        plugin.getStackedBlocks().updateStackedBlockHolograms(chunk);

        BukkitExecutor.sync(() -> {
            if (!pendingLoadedChunksForIsland.remove(chunk) || !chunk.isLoaded())
                return;

            // If we cannot recalculate entities at this moment, we want to track entities normally.
            if (!island.getEntitiesTracker().canRecalculateEntityCounts())
                recalculateEntities.set(false);

            for (Entity entity : chunk.getEntities()) {
                // We want to delete old holograms of stacked blocks + count entities for the chunk
                if (entity instanceof ArmorStand && isOldHologram((ArmorStand) entity) &&
                        plugin.getStackedBlocks().getStackedBlockAmount(entity.getLocation().subtract(0, 1, 0)) > 1) {
                    entity.remove();
                }
            }

            if (recalculateEntities.get()) {
                island.getEntitiesTracker().recalculateEntityCounts();
                pendingLoadedChunksForIsland.clear();
                this.pendingLoadedChunks.remove(island.getUniqueId());
            }
        }, 2L);

        DefaultIslandCalculationAlgorithm.CACHED_CALCULATED_CHUNKS.write(cache -> cache.remove(chunkPosition));
    }

    private static boolean isOldHologram(ArmorStand armorStand) {
        return !armorStand.hasGravity() && armorStand.isSmall() && !armorStand.isVisible() &&
                armorStand.isCustomNameVisible() && armorStand.isMarker() && armorStand.getCustomName() != null;
    }

}

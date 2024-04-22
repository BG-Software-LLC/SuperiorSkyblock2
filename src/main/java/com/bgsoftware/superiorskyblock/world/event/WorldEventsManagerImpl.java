package com.bgsoftware.superiorskyblock.world.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.service.world.WorldRecordService;
import com.bgsoftware.superiorskyblock.api.world.event.WorldEventsManager;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.Mutable;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.algorithm.DefaultIslandCalculationAlgorithm;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeCropGrowth;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeEntityLimits;
import com.google.common.base.Preconditions;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;

@Deprecated
public class WorldEventsManagerImpl implements WorldEventsManager {

    private final LazyReference<WorldRecordService> worldRecordService = new LazyReference<WorldRecordService>() {
        @Override
        protected WorldRecordService create() {
            return plugin.getServices().getService(WorldRecordService.class);
        }
    };
    private final SuperiorSkyblockPlugin plugin;
    private final Map<UUID, Set<Chunk>> pendingLoadedChunks = new HashMap<>();

    public WorldEventsManagerImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    private static boolean isHologram(ArmorStand armorStand) {
        return !armorStand.hasGravity() && armorStand.isSmall() && !armorStand.isVisible() &&
                armorStand.isCustomNameVisible() && armorStand.isMarker() && armorStand.getCustomName() != null;
    }

    @Override
    public void loadChunk(Chunk chunk) {
        Preconditions.checkNotNull(chunk, "chunk parameter cannot be null.");

        if (!plugin.getGrid().isIslandsWorld(chunk.getWorld()))
            return;

        List<Island> chunkIslands = plugin.getGrid().getIslandsAt(chunk);
        chunkIslands.forEach(island -> {
            if (!island.isSpawn())
                handleIslandChunkLoad(island, chunk);
        });
    }

    private void handleIslandChunkLoad(Island island, Chunk chunk) {
        ChunkPosition chunkPosition = ChunkPosition.of(chunk);

        if (!island.getBiome().name().equals(plugin.getSettings().getWorlds().getNormal().getBiome())) {
            List<Player> playersToUpdate = new SequentialListBuilder<Player>()
                    .build(island.getAllPlayersInside(), SuperiorPlayer::asPlayer);
            plugin.getNMSChunks().setBiome(Collections.singletonList(chunkPosition), island.getBiome(), playersToUpdate);
        }

        plugin.getNMSChunks().injectChunkSections(chunk);

        Set<Chunk> pendingLoadedChunksForIsland = this.pendingLoadedChunks.computeIfAbsent(island.getUniqueId(), u -> new LinkedHashSet<>());
        pendingLoadedChunksForIsland.add(chunk);

        boolean cropGrowthEnabled = BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeCropGrowth.class);
        if (cropGrowthEnabled && island.isInsideRange(chunk))
            plugin.getNMSChunks().startTickingChunk(island, chunk, false);

        if (!plugin.getNMSChunks().isChunkEmpty(chunk))
            island.markChunkDirty(chunk.getWorld(), chunk.getX(), chunk.getZ(), true);

        Location islandCenter = island.getCenter(chunk.getWorld().getEnvironment());

        boolean entityLimitsEnabled = BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeEntityLimits.class);
        Mutable<Boolean> recalculateEntities = new Mutable<>(false);

        if (chunk.getX() == (islandCenter.getBlockX() >> 4) && chunk.getZ() == (islandCenter.getBlockZ() >> 4)) {
            if (chunk.getWorld().getEnvironment() == plugin.getSettings().getWorlds().getDefaultWorld()) {
                Block chunkBlock = chunk.getBlock(0, 100, 0);
                island.setBiome(chunk.getWorld().getBiome(chunkBlock.getX(), chunkBlock.getZ()), false);
            }

            if (entityLimitsEnabled)
                recalculateEntities.setValue(true);
        }

        BukkitExecutor.sync(() -> {
            if (!pendingLoadedChunksForIsland.remove(chunk) || !chunk.isLoaded())
                return;

            // If we cannot recalculate entities at this moment, we want to track entities normally.
            if (!island.getEntitiesTracker().canRecalculateEntityCounts())
                recalculateEntities.setValue(false);

            for (Entity entity : chunk.getEntities()) {
                // We want to delete old holograms of stacked blocks + count entities for the chunk
                if (entity instanceof ArmorStand && isHologram((ArmorStand) entity) &&
                        plugin.getStackedBlocks().getStackedBlockAmount(entity.getLocation().subtract(0, 1, 0)) > 1) {
                    entity.remove();
                }
            }

            if (recalculateEntities.getValue()) {
                island.getEntitiesTracker().recalculateEntityCounts();
                pendingLoadedChunksForIsland.clear();
                this.pendingLoadedChunks.remove(island.getUniqueId());
            }
        }, 2L);

        DefaultIslandCalculationAlgorithm.CACHED_CALCULATED_CHUNKS.remove(chunkPosition);

        plugin.getStackedBlocks().updateStackedBlockHolograms(chunk);
    }

    @Override
    public void unloadChunk(Chunk chunk) {
        Preconditions.checkNotNull(chunk, "chunk parameter cannot be null.");

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

}

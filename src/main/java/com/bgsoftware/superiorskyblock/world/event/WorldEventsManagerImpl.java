package com.bgsoftware.superiorskyblock.world.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.event.WorldEventsManager;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.Mutable;
import com.bgsoftware.superiorskyblock.core.Singleton;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.algorithm.DefaultIslandCalculationAlgorithm;
import com.bgsoftware.superiorskyblock.listener.EntityTrackingListener;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeCropGrowth;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeEntityLimits;
import com.google.common.base.Preconditions;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

import java.util.Arrays;

@Deprecated
public class WorldEventsManagerImpl implements WorldEventsManager {

    private final SuperiorSkyblockPlugin plugin;
    private final Singleton<EntityTrackingListener> entityTrackingListener;

    public WorldEventsManagerImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        this.entityTrackingListener = plugin.getListener(EntityTrackingListener.class);
    }

    private static boolean isHologram(ArmorStand armorStand) {
        return !armorStand.hasGravity() && armorStand.isSmall() && !armorStand.isVisible() &&
                armorStand.isCustomNameVisible() && armorStand.isMarker() && armorStand.getCustomName() != null;
    }

    @Override
    public void loadChunk(Chunk chunk) {
        Preconditions.checkNotNull(chunk, "chunk parameter cannot be null.");

        Location firstBlock = chunk.getBlock(0, 100, 0).getLocation();
        Island island = plugin.getGrid().getIslandAt(firstBlock);

        if (island == null || island.isSpawn())
            return;

        plugin.getNMSChunks().injectChunkSections(chunk);

        boolean cropGrowthEnabled = BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeCropGrowth.class);
        if (cropGrowthEnabled && island.isInsideRange(chunk))
            plugin.getNMSChunks().startTickingChunk(island, chunk, false);

        if (!plugin.getNMSChunks().isChunkEmpty(chunk))
            island.markChunkDirty(chunk.getWorld(), chunk.getX(), chunk.getZ(), true);

        Location islandCenter = island.getCenter(chunk.getWorld().getEnvironment());

        Mutable<Boolean> recalculateEntities = new Mutable<>(false);

        if (chunk.getX() == (islandCenter.getBlockX() >> 4) && chunk.getZ() == (islandCenter.getBlockZ() >> 4)) {
            if (chunk.getWorld().getEnvironment() == plugin.getSettings().getWorlds().getDefaultWorld()) {
                island.setBiome(firstBlock.getWorld().getBiome(firstBlock.getBlockX(), firstBlock.getBlockZ()), false);
            }

            recalculateEntities.setValue(true);
        }

        BukkitExecutor.sync((bukkitRunnable) -> {
            if (!chunk.isLoaded())
                return;

            // We want to delete old holograms of stacked blocks + count entities for the chunk
            for (Entity entity : chunk.getEntities()) {
                if (entity instanceof ArmorStand && isHologram((ArmorStand) entity) &&
                        plugin.getStackedBlocks().getStackedBlockAmount(entity.getLocation().subtract(0, 1, 0)) > 1)
                    entity.remove();
            }

            // We want to recalculate entities
            if (BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeEntityLimits.class)) {
                Arrays.stream(chunk.getEntities()).forEach(entityTrackingListener.get()::onEntitySpawn);
                if (recalculateEntities.getValue())
                    island.getEntitiesTracker().recalculateEntityCounts();
            }
        }, 2L);

        ChunkPosition chunkPosition = ChunkPosition.of(chunk);
        DefaultIslandCalculationAlgorithm.CACHED_CALCULATED_CHUNKS.remove(chunkPosition);

        plugin.getStackedBlocks().updateStackedBlockHolograms(chunk);
    }

    @Override
    public void unloadChunk(Chunk chunk) {
        Preconditions.checkNotNull(chunk, "chunk parameter cannot be null.");

        if (!plugin.getGrid().isIslandsWorld(chunk.getWorld()))
            return;

        plugin.getStackedBlocks().removeStackedBlockHolograms(chunk);

        Island island = plugin.getGrid().getIslandAt(chunk);

        if (island == null)
            return;

        if (BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeCropGrowth.class))
            plugin.getNMSChunks().startTickingChunk(island, chunk, true);

        if (!island.isSpawn() && !plugin.getNMSChunks().isChunkEmpty(chunk))
            island.markChunkDirty(chunk.getWorld(), chunk.getX(), chunk.getZ(), true);

        Arrays.stream(chunk.getEntities()).forEach(entityTrackingListener.get()::onEntityDespawn);
    }

}

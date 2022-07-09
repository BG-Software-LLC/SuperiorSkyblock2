package com.bgsoftware.superiorskyblock.world.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.event.WorldEventsManager;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.Singleton;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.algorithm.DefaultIslandCalculationAlgorithm;
import com.bgsoftware.superiorskyblock.listener.EntityTrackingListener;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeCropGrowth;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeEntityLimits;
import com.bgsoftware.superiorskyblock.world.chunk.ChunksTracker;
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

        ChunkPosition chunkPosition = ChunkPosition.of(chunk.getWorld(), chunk.getX(), chunk.getZ());

        if (!plugin.getNMSChunks().isChunkEmpty(chunk))
            ChunksTracker.markDirty(island, chunkPosition, true);

        BukkitExecutor.sync(() -> {
            // We want to delete old holograms of stacked blocks + count entities for the chunk
            for (Entity entity : chunk.getEntities()) {
                if (entity instanceof ArmorStand && isHologram((ArmorStand) entity) &&
                        plugin.getStackedBlocks().getStackedBlockAmount(entity.getLocation().subtract(0, 1, 0)) > 1)
                    entity.remove();

                entityTrackingListener.get().onEntityDespawn(entity);
            }
        }, 2L);

        Location islandCenter = island.getCenter(chunk.getWorld().getEnvironment());

        if (chunk.getX() == (islandCenter.getBlockX() >> 4) && chunk.getZ() == (islandCenter.getBlockZ() >> 4)) {
            if (chunk.getWorld().getEnvironment() == plugin.getSettings().getWorlds().getDefaultWorld()) {
                island.setBiome(firstBlock.getWorld().getBiome(firstBlock.getBlockX(), firstBlock.getBlockZ()), false);
            }

            if (BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeEntityLimits.class)) {
                BukkitExecutor.sync(() -> {
                    if (chunk.isLoaded())
                        island.getEntitiesTracker().recalculateEntityCounts();
                }, 20L);
            }
        }

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
            ChunksTracker.markDirty(island, chunk, true);

        Arrays.stream(chunk.getEntities()).forEach(entityTrackingListener.get()::onEntitySpawn);

    }

}

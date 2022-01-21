package com.bgsoftware.superiorskyblock.world.event;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.event.WorldEventsManager;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeCropGrowth;
import com.bgsoftware.superiorskyblock.world.chunks.ChunksTracker;
import com.google.common.base.Preconditions;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

public final class WorldEventsManagerImpl implements WorldEventsManager {

    private final SuperiorSkyblockPlugin plugin;

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

        Location firstBlock = chunk.getBlock(0, 100, 0).getLocation();
        Island island = plugin.getGrid().getIslandAt(firstBlock);

        if (island == null || island.isSpawn())
            return;

        if (chunk.getWorld().getEnvironment() == plugin.getSettings().getWorlds().getDefaultWorld()) {
            island.setBiome(firstBlock.getWorld().getBiome(firstBlock.getBlockX(), firstBlock.getBlockZ()), false);
        }

        plugin.getNMSChunks().injectChunkSections(chunk);

        boolean cropGrowthEnabled = BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeCropGrowth.class);
        if (cropGrowthEnabled && island.isInsideRange(chunk))
            plugin.getNMSChunks().startTickingChunk(island, chunk, false);

        if (!plugin.getNMSChunks().isChunkEmpty(chunk))
            ChunksTracker.markDirty(island, chunk, true);

        // We want to delete old holograms of stacked blocks
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof ArmorStand && isHologram((ArmorStand) entity) &&
                    plugin.getStackedBlocks().getStackedBlockAmount(entity.getLocation().subtract(0, 1, 0)) > 1)
                entity.remove();
        }

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
    }

}

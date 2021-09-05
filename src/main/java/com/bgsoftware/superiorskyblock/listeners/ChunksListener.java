package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

@SuppressWarnings("unused")
public final class ChunksListener implements Listener {

    private static final ReflectMethod<Void> SET_SAVE_CHUNK = new ReflectMethod<>(ChunkUnloadEvent.class, "setSaveChunk", boolean.class);

    private final SuperiorSkyblockPlugin plugin;

    public ChunksListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkUnloadMonitor(ChunkUnloadEvent e) {
        if (!plugin.getGrid().isIslandsWorld(e.getWorld()))
            return;

        plugin.getStackedBlocks().removeStackedBlockHolograms(e.getChunk());

        Island island = plugin.getGrid().getIslandAt(e.getChunk());

        if (island == null)
            return;

        plugin.getNMSChunks().startTickingChunk(island, e.getChunk(), true);

        if (!island.isSpawn() && !plugin.getNMSChunks().isChunkEmpty(e.getChunk()))
            ChunksTracker.markDirty(island, e.getChunk(), true);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        Location firstBlock = e.getChunk().getBlock(0, 100, 0).getLocation();
        Island island = plugin.getGrid().getIslandAt(firstBlock);

        if (island == null || island.isSpawn())
            return;

        if (e.getWorld().getEnvironment() == plugin.getSettings().getWorlds().getDefaultWorld()) {
            island.setBiome(firstBlock.getWorld().getBiome(firstBlock.getBlockX(), firstBlock.getBlockZ()), false);
        }

        plugin.getNMSChunks().injectChunkSections(e.getChunk());

        if (island.isInsideRange(e.getChunk()))
            plugin.getNMSChunks().startTickingChunk(island, e.getChunk(), false);

        if (!plugin.getNMSChunks().isChunkEmpty(e.getChunk()))
            ChunksTracker.markDirty(island, e.getChunk(), true);

        // We want to delete old holograms of stacked blocks
        for (Entity entity : e.getChunk().getEntities()) {
            if (entity instanceof ArmorStand && isHologram((ArmorStand) entity) &&
                    plugin.getStackedBlocks().getStackedBlockAmount(entity.getLocation().subtract(0, 1, 0)) > 1)
                entity.remove();
        }

        plugin.getStackedBlocks().updateStackedBlockHolograms(e.getChunk());
    }

    // Should potentially fix crop growth tile entities "disappearing"
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockGrow(BlockGrowEvent e) {
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
        if (island != null && island.isInsideRange(e.getBlock().getLocation()))
            plugin.getNMSChunks().startTickingChunk(island, e.getBlock().getChunk(), false);
    }

    private static boolean isHologram(ArmorStand armorStand) {
        return !armorStand.hasGravity() && armorStand.isSmall() && !armorStand.isVisible() &&
                armorStand.isCustomNameVisible() && armorStand.isMarker() && armorStand.getCustomName() != null;
    }

}

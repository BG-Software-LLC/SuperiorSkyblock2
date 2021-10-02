package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

@SuppressWarnings("unused")
public final class ChunksListener implements Listener {

    private final SuperiorSkyblockPlugin plugin;

    public ChunksListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkUnloadMonitor(ChunkUnloadEvent e) {
        plugin.getWorldEventsManager().unloadChunk(e.getChunk());
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        plugin.getWorldEventsManager().loadChunk(e.getChunk());
    }

    // Should potentially fix crop growth tile entities "disappearing"
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockGrow(BlockGrowEvent e) {
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
        if (island != null && island.isInsideRange(e.getBlock().getLocation()))
            plugin.getNMSChunks().startTickingChunk(island, e.getBlock().getChunk(), false);
    }

}

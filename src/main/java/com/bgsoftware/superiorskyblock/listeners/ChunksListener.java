package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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

}

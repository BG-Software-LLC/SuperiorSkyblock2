package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Collections;
import java.util.List;

public class ChunksListener implements Listener {

    private final SuperiorSkyblockPlugin plugin;

    public ChunksListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onChunkUnloadMonitor(ChunkUnloadEvent e) {
        // noinspection deprecation
        plugin.getWorldEventsManager().unloadChunk(e.getChunk());
    }

    @EventHandler
    private void onChunkLoad(ChunkLoadEvent e) {
        // noinspection deprecation
        plugin.getWorldEventsManager().loadChunk(e.getChunk());
    }

}

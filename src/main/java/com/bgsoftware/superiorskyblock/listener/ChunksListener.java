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

@SuppressWarnings("unused")
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
        if (e.isNewChunk() && plugin.getGrid().isIslandsWorld(e.getWorld()) && e.getWorld().getEnvironment() == World.Environment.NORMAL) {
            // We want to update the biome for new island chunks.
            Island island = plugin.getGrid().getIslandAt(e.getChunk());
            if (island != null && !island.getBiome().name().equals(plugin.getSettings().getWorlds().getNormal().getBiome())) {
                List<Player> playersToUpdate = new SequentialListBuilder<Player>()
                        .build(island.getAllPlayersInside(), SuperiorPlayer::asPlayer);
                plugin.getNMSChunks().setBiome(Collections.singletonList(ChunkPosition.of(e.getChunk())), island.getBiome(), playersToUpdate);
            }
        }

        // noinspection deprecation
        plugin.getWorldEventsManager().loadChunk(e.getChunk());
    }

}

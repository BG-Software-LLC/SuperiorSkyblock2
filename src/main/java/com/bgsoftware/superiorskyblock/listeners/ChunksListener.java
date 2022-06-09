package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.world.chunks.ChunkPosition;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public final class ChunksListener implements Listener {

    private final SuperiorSkyblockPlugin plugin;

    public ChunksListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkUnloadMonitor(ChunkUnloadEvent e) {
        // noinspection deprecation
        plugin.getWorldEventsManager().unloadChunk(e.getChunk());
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        if (e.isNewChunk() && plugin.getGrid().isIslandsWorld(e.getWorld()) && e.getWorld().getEnvironment() == World.Environment.NORMAL) {
            // We want to update the biome for new island chunks.
            Island island = plugin.getGrid().getIslandAt(e.getChunk());
            if (island != null && !island.getBiome().name().equals(plugin.getSettings().getWorlds().getNormal().getBiome())) {
                List<Player> playersToUpdate = island.getAllPlayersInside().stream().map(SuperiorPlayer::asPlayer).collect(Collectors.toList());
                plugin.getNMSChunks().setBiome(Collections.singletonList(ChunkPosition.of(e.getChunk())), island.getBiome(), playersToUpdate);
            }
        }

        // noinspection deprecation
        plugin.getWorldEventsManager().loadChunk(e.getChunk());
    }

}

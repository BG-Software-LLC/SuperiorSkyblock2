package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

@SuppressWarnings("unused")
public final class ChunksListener implements Listener {

    private final SuperiorSkyblockPlugin plugin;

    public ChunksListener(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e){
        if(plugin.getGrid() == null)
            return;

        Location firstBlock = e.getChunk().getBlock(0, 100, 0).getLocation();
        Island island = plugin.getGrid().getIslandAt(firstBlock);
        if(island != null && island.getBiome() != null && !island.getBiome().equals(firstBlock.getWorld().getBiome(firstBlock.getBlockX(), firstBlock.getBlockZ())))
            plugin.getNMSAdapter().setBiome(e.getChunk(), island.getBiome());
    }

}

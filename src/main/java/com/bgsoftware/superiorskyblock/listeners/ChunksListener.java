package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("unused")
public final class ChunksListener implements Listener {

    private final Set<Integer> alreadyUnloadedChunks = new HashSet<>();
    private final SuperiorSkyblockPlugin plugin;

    public ChunksListener(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent e){
        if(plugin.getGrid() == null || !plugin.getGrid().isIslandsWorld(e.getWorld()))
            return;

        Chunk chunk = e.getChunk();

        int hashedChunk = Objects.hash(e.getWorld().getName(), chunk.getX(), chunk.getZ());

        if(alreadyUnloadedChunks.contains(hashedChunk)) {
            alreadyUnloadedChunks.remove(hashedChunk);
            return;
        }

        Island island = plugin.getGrid().getIslandAt(chunk);

        if(plugin.getSettings().optimizeWorlds) {
            if (island == null || !island.isInsideRange(chunk)) {
                if (ServerVersion.isLessThan(ServerVersion.v1_14)) {
                    e.setCancelled(true);
                    alreadyUnloadedChunks.add(hashedChunk);
                    chunk.unload(false);
                } else try {
                    //noinspection JavaReflectionMemberAccess
                    Method setSaveChunkMethod = ChunkUnloadEvent.class.getMethod("setSaveChunk", boolean.class);
                    setSaveChunkMethod.invoke(e, false);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        plugin.getNMSBlocks().startTickingChunk(island, e.getChunk(), true);

        if(island != null && !island.isSpawn() && !plugin.getNMSAdapter().isChunkEmpty(e.getChunk()))
            ChunksTracker.markDirty(island, e.getChunk(), true);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e){
        if(plugin.getGrid() == null || e.getWorld().getEnvironment() != World.Environment.NORMAL)
            return;

        Location firstBlock = e.getChunk().getBlock(0, 100, 0).getLocation();
        Island island = plugin.getGrid().getIslandAt(firstBlock);

        if(island == null || island.isSpawn())
            return;

        ((SIsland) island).setBiomeRaw(firstBlock.getWorld().getBiome(firstBlock.getBlockX(), firstBlock.getBlockZ()));

        plugin.getNMSAdapter().injectChunkSections(e.getChunk());
        plugin.getNMSBlocks().startTickingChunk(island, e.getChunk(), false);

        if(!plugin.getNMSAdapter().isChunkEmpty(e.getChunk()))
            ChunksTracker.markDirty(island, e.getChunk(), true);
    }

}

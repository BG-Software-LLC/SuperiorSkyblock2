package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.handlers.StackedBlocksHandler;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings("unused")
public final class ChunksListener implements Listener {

    private static final ReflectMethod<Void> SET_SAVE_CHUNK = new ReflectMethod<>(ChunkUnloadEvent.class, "setSaveChunk", boolean.class);

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
                } else {
                    SET_SAVE_CHUNK.invoke(e, false);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkUnloadMonitor(ChunkUnloadEvent e){
        if(plugin.getGrid() == null || !plugin.getGrid().isIslandsWorld(e.getWorld()))
            return;

        plugin.getGrid().getStackedBlocks(ChunkPosition.of(e.getChunk()))
                .forEach(StackedBlocksHandler.StackedBlock::removeHologram);

        Island island = plugin.getGrid().getIslandAt(e.getChunk());

        if(island == null)
            return;

        plugin.getNMSBlocks().startTickingChunk(island, e.getChunk(), true);

        if(!island.isSpawn() && !plugin.getNMSAdapter().isChunkEmpty(e.getChunk()))
            ChunksTracker.markDirty(island, e.getChunk(), true);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e){
        if(plugin.getGrid() == null)
            return;

        Location firstBlock = e.getChunk().getBlock(0, 100, 0).getLocation();
        Island island = plugin.getGrid().getIslandAt(firstBlock);

        if(island == null || island.isSpawn())
            return;

        if(e.getWorld().getEnvironment() == World.Environment.NORMAL) {
            island.setBiome(firstBlock.getWorld().getBiome(firstBlock.getBlockX(), firstBlock.getBlockZ()), false);
        }

        plugin.getNMSAdapter().injectChunkSections(e.getChunk());

        if(island.isInsideRange(e.getChunk()))
            plugin.getNMSBlocks().startTickingChunk(island, e.getChunk(), false);

        if(!plugin.getNMSAdapter().isChunkEmpty(e.getChunk()))
            ChunksTracker.markDirty(island, e.getChunk(), true);

        // We want to delete old holograms of stacked blocks
        for(Entity entity : e.getChunk().getEntities()){
            if(entity instanceof ArmorStand && isHologram((ArmorStand) entity) &&
                    plugin.getGrid().getBlockAmount(entity.getLocation().subtract(0, 1, 0)) > 1)
                entity.remove();
        }

        plugin.getGrid().getStackedBlocks(ChunkPosition.of(e.getChunk())).forEach(StackedBlocksHandler.StackedBlock::updateName);
    }

    // Should potentially fix crop growth tile entities "disappearing"
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockGrow(BlockGrowEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
        if(island != null && island.isInsideRange(e.getBlock().getLocation()))
            plugin.getNMSBlocks().startTickingChunk(island, e.getBlock().getChunk(), false);
    }

    private static boolean isHologram(ArmorStand armorStand){
        return !armorStand.hasGravity() && armorStand.isSmall() && !armorStand.isVisible() &&
                armorStand.isCustomNameVisible() && armorStand.isMarker() && armorStand.getCustomName() != null;
    }

}

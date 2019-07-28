package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.utils.Pair;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.events.BarrelPlaceEvent;
import com.bgsoftware.wildstacker.api.events.BarrelStackEvent;
import com.bgsoftware.wildstacker.api.events.BarrelUnstackEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerPlaceEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerStackEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerUnstackEvent;
import com.bgsoftware.wildstacker.api.objects.StackedSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public final class BlocksProvider_WildStacker implements BlocksProvider {

    private static final Map<ChunkWrapper, StackedSnapshot> chunkSnapshots = new HashMap<>();

    public BlocksProvider_WildStacker(){
        Bukkit.getPluginManager().registerEvents(new StackerListener(), SuperiorSkyblockPlugin.getPlugin());
    }

    public static void cacheChunk(Chunk chunk){
        try {
            chunkSnapshots.put(new ChunkWrapper(chunk), WildStackerAPI.getWildStacker().getSystemManager().getStackedSnapshot(chunk, false));
        }catch(Throwable ignored){}
    }

    public static void uncacheChunk(Chunk chunk){
        chunkSnapshots.remove(new ChunkWrapper(chunk));
    }

    @Override
    public Pair<Integer, EntityType> getSpawner(Location location) {
        ChunkWrapper chunkWrapper = new ChunkWrapper(location);
        if(chunkSnapshots.containsKey(chunkWrapper))
            return new Pair<>(chunkSnapshots.get(chunkWrapper).getStackedSpawner(location));

        cacheChunk(location.getChunk());
        return getSpawner(location);
    }

    @Override
    public Pair<Integer, Material> getBlock(Location location) {
        ChunkWrapper chunkWrapper = new ChunkWrapper(location);
        if(chunkSnapshots.containsKey(chunkWrapper)) {
            Map.Entry<Integer, Material> entry = chunkSnapshots.get(chunkWrapper).getStackedBarrel(location);
            return entry.getValue().name().contains("AIR") ? null : new Pair<>(entry);
        }

        cacheChunk(location.getChunk());
        return getBlock(location);
    }

    @SuppressWarnings("unused")
    private static class StackerListener implements Listener{

        private final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBarrelPlace(BarrelPlaceEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getBarrel().getLocation());
            if(island != null)
                island.handleBlockPlace(Key.of(e.getBarrel().getBarrelItem(1)), e.getBarrel().getStackAmount());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBarrelStack(BarrelStackEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getBarrel().getLocation());
            if(island != null)
                island.handleBlockPlace(Key.of(e.getBarrel().getBarrelItem(1)), e.getTarget().getStackAmount());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBarrelUnstack(BarrelUnstackEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getBarrel().getLocation());
            if(island != null)
                island.handleBlockBreak(Key.of(e.getBarrel().getBarrelItem(1)), e.getAmount());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerPlace(SpawnerPlaceEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());
            if(island != null)
                island.handleBlockPlace(e.getSpawner().getLocation().getBlock(), e.getSpawner().getStackAmount() - 1);
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerStack(SpawnerStackEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());
            if(island != null)
                island.handleBlockPlace(e.getSpawner().getLocation().getBlock(), e.getTarget().getStackAmount());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerUnstack(SpawnerUnstackEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());
            if(island != null)
                island.handleBlockBreak(e.getSpawner().getLocation().getBlock(), e.getAmount());
        }

    }

    private static class ChunkWrapper{

        private int x, z;

        ChunkWrapper(Chunk chunk){
            this(chunk.getX(), chunk.getZ());
        }

        ChunkWrapper(Location location){
            this(location.getBlockX() >> 4, location.getBlockZ() >> 4);
        }

        ChunkWrapper(int x, int z){
            this.x = x;
            this.z = z;
        }

        @Override
        public int hashCode() {
            int hash = 19 * 3;
            hash = 19 * hash + (int)(Double.doubleToLongBits(this.x) ^ Double.doubleToLongBits(this.x) >>> 32);
            hash = 19 * hash + (int)(Double.doubleToLongBits(this.z) ^ Double.doubleToLongBits(this.z) >>> 32);
            return hash;
        }
    }

}

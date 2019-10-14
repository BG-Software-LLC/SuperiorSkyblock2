package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.utils.Pair;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.events.BarrelPlaceEvent;
import com.bgsoftware.wildstacker.api.events.BarrelPlaceInventoryEvent;
import com.bgsoftware.wildstacker.api.events.BarrelStackEvent;
import com.bgsoftware.wildstacker.api.events.BarrelUnstackEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerPlaceEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerPlaceInventoryEvent;
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

    private static final Map<String, StackedSnapshot> chunkSnapshots = new HashMap<>();

    public BlocksProvider_WildStacker(){
        Bukkit.getPluginManager().registerEvents(new StackerListener(), SuperiorSkyblockPlugin.getPlugin());
    }

    public static void cacheChunk(Chunk chunk){
        try {
            StackedSnapshot stackedSnapshot;
            try{
                stackedSnapshot = WildStackerAPI.getWildStacker().getSystemManager().getStackedSnapshot(chunk);
            }catch(Throwable ex){
                //noinspection deprecation
                stackedSnapshot = WildStackerAPI.getWildStacker().getSystemManager().getStackedSnapshot(chunk, false);
            }
            if(stackedSnapshot != null)
                chunkSnapshots.put(getId(chunk), stackedSnapshot);
        }catch(Throwable ex){
            ex.printStackTrace();
        }
    }

    public static void uncacheChunk(Chunk chunk){
        chunkSnapshots.remove(getId(chunk));
    }

    @Override
    public Pair<Integer, EntityType> getSpawner(Location location) {
        String id = getId(location);
        if(chunkSnapshots.containsKey(id))
            return new Pair<>(chunkSnapshots.get(id).getStackedSpawner(location));

        throw new RuntimeException("Chunk " + id + " is not cached.");
    }

    @Override
    public Pair<Integer, Material> getBlock(Location location) {
        String id = getId(location);
        if(chunkSnapshots.containsKey(id)) {
            Map.Entry<Integer, Material> entry = chunkSnapshots.get(id).getStackedBarrel(location);
            return entry.getValue().name().contains("AIR") ? null : new Pair<>(entry);
        }

        throw new RuntimeException("Chunk " + id + " is not cached. Location: " + location);
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
        public void onBarrelUnstack(BarrelPlaceInventoryEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getBarrel().getLocation());
            if(island != null)
                island.handleBlockPlace(Key.of(e.getBarrel().getBarrelItem(1)), e.getIncreaseAmount());
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

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerPlaceInventory(SpawnerPlaceInventoryEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());
            if(island != null)
                island.handleBlockPlace(e.getSpawner().getLocation().getBlock(), e.getIncreaseAmount());
        }

    }

    private static String getId(Location location){
        return getId(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    private static String getId(Chunk chunk){
        return getId(chunk.getX(), chunk.getZ());
    }

    private static String getId(int x, int z){
        return x + "," + z;
    }

}

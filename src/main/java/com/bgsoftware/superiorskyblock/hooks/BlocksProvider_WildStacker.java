package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.utils.Pair;
import com.bgsoftware.superiorskyblock.utils.key.SKey;
import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.events.BarrelPlaceEvent;
import com.bgsoftware.wildstacker.api.events.BarrelStackEvent;
import com.bgsoftware.wildstacker.api.events.BarrelUnstackEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerPlaceEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerStackEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerUnstackEvent;
import com.bgsoftware.wildstacker.api.objects.StackedBarrel;
import com.bgsoftware.wildstacker.api.objects.StackedSnapshot;
import com.bgsoftware.wildstacker.api.objects.StackedSpawner;
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

    private static final Map<Chunk, StackedSnapshot> chunkSnapshots = new HashMap<>();

    public BlocksProvider_WildStacker(){
        Bukkit.getPluginManager().registerEvents(new StackerListener(), SuperiorSkyblockPlugin.getPlugin());
    }

    public static void cacheChunk(Chunk chunk){
        try {
            chunkSnapshots.put(chunk, WildStackerAPI.getWildStacker().getSystemManager().getStackedSnapshot(chunk, false));
        }catch(Throwable ignored){}
    }

    public static void uncacheChunk(Chunk chunk){
        chunkSnapshots.remove(chunk);
    }

    @Override
    public Pair<Integer, EntityType> getSpawner(Location location) {
        if(chunkSnapshots.containsKey(location.getChunk()))
            return new Pair<>(chunkSnapshots.get(location.getChunk()).getStackedSpawner(location));

        StackedSpawner stackedSpawner = WildStackerAPI.getWildStacker().getSystemManager().getStackedSpawner(location);
        return new Pair<>(stackedSpawner.getStackAmount(), stackedSpawner.getSpawnedType());
    }

    @Override
    public Pair<Integer, Material> getBlock(Location location) {
        if(chunkSnapshots.containsKey(location.getChunk()))
            return new Pair<>(chunkSnapshots.get(location.getChunk()).getStackedBarrel(location));

        StackedBarrel stackedBarrel = WildStackerAPI.getWildStacker().getSystemManager().getStackedBarrel(location);
        return new Pair<>(stackedBarrel.getStackAmount(), stackedBarrel.getType());
    }

    @SuppressWarnings("unused")
    private static class StackerListener implements Listener{

        private final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBarrelPlace(BarrelPlaceEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getBarrel().getLocation());
            if(island != null)
                island.handleBlockPlace(SKey.of(e.getBarrel().getBarrelItem(1)), e.getBarrel().getStackAmount());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBarrelStack(BarrelStackEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getBarrel().getLocation());
            if(island != null)
                island.handleBlockPlace(SKey.of(e.getBarrel().getBarrelItem(1)), e.getTarget().getStackAmount());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBarrelUnstack(BarrelUnstackEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getBarrel().getLocation());
            if(island != null)
                island.handleBlockBreak(SKey.of(e.getBarrel().getBarrelItem(1)), e.getAmount());
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

}

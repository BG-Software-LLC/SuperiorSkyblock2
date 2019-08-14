package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.utils.Pair;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.events.BarrelAmountChangeEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerAmountChangeEvent;
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
            chunkSnapshots.put(getId(chunk), WildStackerAPI.getWildStacker().getSystemManager().getStackedSnapshot(chunk, false));
        }catch(Throwable ignored){}
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
        public void onBarrelAmountChange(BarrelAmountChangeEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getStackedObject().getLocation());
            if(island != null) {
                if(e.getStackAmount() > e.getOriginalAmount()){
                    island.handleBlockPlace(Key.of(e.getStackedObject().getBarrelItem(1)), e.getStackAmount() - e.getOriginalAmount());
                }
                else if(e.getStackAmount() < e.getOriginalAmount()){
                    island.handleBlockBreak(Key.of(e.getStackedObject().getBarrelItem(1)), e.getOriginalAmount() - e.getStackAmount());
                }
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerAmountChange(SpawnerAmountChangeEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getStackedObject().getLocation());
            if(island != null) {
                if(e.getStackAmount() > e.getOriginalAmount()){
                    island.handleBlockPlace(Key.of(e.getStackedObject().getSpawner().getBlock()), e.getStackAmount() - e.getOriginalAmount());
                }
                else if(e.getStackAmount() < e.getOriginalAmount()){
                    island.handleBlockBreak(Key.of(e.getStackedObject().getSpawner().getBlock()), e.getOriginalAmount() - e.getStackAmount());
                }
            }
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

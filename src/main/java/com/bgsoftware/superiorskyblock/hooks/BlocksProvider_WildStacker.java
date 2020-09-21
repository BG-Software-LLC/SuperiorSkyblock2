package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.CustomKeyParser;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.events.BarrelPlaceEvent;
import com.bgsoftware.wildstacker.api.events.BarrelPlaceInventoryEvent;
import com.bgsoftware.wildstacker.api.events.BarrelStackEvent;
import com.bgsoftware.wildstacker.api.events.BarrelUnstackEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerPlaceEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerPlaceInventoryEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerStackEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerUnstackEvent;
import com.bgsoftware.wildstacker.api.handlers.SystemManager;
import com.bgsoftware.wildstacker.api.objects.StackedSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class BlocksProvider_WildStacker implements BlocksProvider {

    private static boolean registered = false;

    public BlocksProvider_WildStacker(){
        if(!registered) {
            Bukkit.getPluginManager().registerEvents(new StackerListener(), SuperiorSkyblockPlugin.getPlugin());
            registered = true;

            SuperiorSkyblockAPI.getBlockValues().registerKeyParser(new CustomKeyParser() {

                @Override
                public com.bgsoftware.superiorskyblock.api.key.Key getCustomKey(Location location) {
                    return getSystemManager().isStackedBarrel(location) ?
                            com.bgsoftware.superiorskyblock.api.key.Key.of(getSystemManager().getStackedBarrel(location).getBarrelItem(1)) :
                            com.bgsoftware.superiorskyblock.api.key.Key.of("CAULDRON");
                }

                @Override
                public boolean isCustomKey(com.bgsoftware.superiorskyblock.api.key.Key key) {
                    return false;
                }

            }, ConstantKeys.CAULDRON);
        }
    }

    @Override
    public Pair<Integer, String> getSpawner(Location location) {
        throw new UnsupportedOperationException("Unsupported Operation");
    }

    private SystemManager getSystemManager(){
        return WildStackerAPI.getWildStacker().getSystemManager();
    }

    public static class WildStackerSnapshot {

        private final Registry<String, StackedSnapshot> chunkSnapshots = Registry.createRegistry();

        public void cacheChunk(Chunk chunk){
            try {
                StackedSnapshot stackedSnapshot;
                try {
                    stackedSnapshot = WildStackerAPI.getWildStacker().getSystemManager().getStackedSnapshot(chunk);
                } catch (Throwable ex) {
                    //noinspection deprecation
                    stackedSnapshot = WildStackerAPI.getWildStacker().getSystemManager().getStackedSnapshot(chunk, false);
                }
                if (stackedSnapshot != null) {
                    chunkSnapshots.add(getId(chunk), stackedSnapshot);
                }
            }catch(Throwable ignored){}
        }

        public void delete(){
            chunkSnapshots.delete();
        }

        public Pair<Integer, String> getSpawner(Location location) {
            String id = getId(location);
            if(chunkSnapshots.containsKey(id)) {
                Map.Entry<Integer, EntityType> entry = chunkSnapshots.get(id).getStackedSpawner(location);
                return new Pair<>(entry.getKey(), entry.getValue() + "");
            }

            throw new RuntimeException("Chunk " + id + " is not cached.");
        }

        public Set<Pair<Integer, ItemStack>> getBlocks(ChunkPosition chunkPosition) {
            String id = getId(chunkPosition);
            StackedSnapshot stackedSnapshot = chunkSnapshots.get(id);

            if(stackedSnapshot == null)
                throw new RuntimeException("Chunk " + id + " is not cached.");

            try {
                return stackedSnapshot.getAllBarrelsItems().values().stream()
                        .filter(entry -> entry.getValue() != null).map(Pair::new).collect(Collectors.toSet());
            }catch (Throwable ex){
                return stackedSnapshot.getAllBarrels().values().stream().map(
                        entry -> new Pair<>(entry.getKey(), new ItemStack(entry.getValue())))
                        .collect(Collectors.toSet());
            }
        }
    }

    @SuppressWarnings("unused")
    private static class StackerListener implements Listener{

        private final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBarrelPlace(BarrelPlaceEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getBarrel().getLocation());

            Key blockKey = Key.of(e.getBarrel().getBarrelItem(1));
            int increaseAmount = e.getBarrel().getStackAmount();

            if(island.hasReachedBlockLimit(blockKey, increaseAmount)){
                e.setCancelled(true);
                Locale.REACHED_BLOCK_LIMIT.send(e.getPlayer(), StringUtils.format(blockKey.toString()));
            }

            else{
                island.handleBlockPlace(blockKey, increaseAmount);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBarrelStack(BarrelStackEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getBarrel().getLocation());

            if(island == null)
                return;

            Key blockKey = Key.of(e.getTarget().getBarrelItem(1));
            int increaseAmount = e.getTarget().getStackAmount();

            if(island.hasReachedBlockLimit(blockKey, increaseAmount)){
                e.setCancelled(true);
            }

            else{
                island.handleBlockPlace(blockKey, increaseAmount);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBarrelUnstack(BarrelUnstackEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getBarrel().getLocation());
            if(island != null)
                island.handleBlockBreak(Key.of(e.getBarrel().getBarrelItem(1)), e.getAmount());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBarrelPlace(BarrelPlaceInventoryEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getBarrel().getLocation());

            if(island == null)
                return;

            Key blockKey = Key.of(e.getBarrel().getBarrelItem(1));
            int increaseAmount = e.getIncreaseAmount();

            if(island.hasReachedBlockLimit(blockKey, increaseAmount)){
                e.setCancelled(true);
                Locale.REACHED_BLOCK_LIMIT.send(e.getPlayer(), StringUtils.format(blockKey.toString()));
            }

            else{
                island.handleBlockPlace(blockKey, increaseAmount);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerPlace(SpawnerPlaceEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());

            if(island == null)
                return;

            Key blockKey = Key.of(Materials.SPAWNER.toBukkitType() + ":" + e.getSpawner().getSpawnedType());
            int increaseAmount = e.getSpawner().getStackAmount();

            if(island.hasReachedBlockLimit(blockKey, increaseAmount)){
                e.setCancelled(true);
                Locale.REACHED_BLOCK_LIMIT.send(e.getPlayer(), StringUtils.format(blockKey.toString()));
            }

            else{
                island.handleBlockPlace(blockKey, increaseAmount - 1);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerStack(SpawnerStackEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());

            if(island == null)
                return;

            Key blockKey = Key.of(Materials.SPAWNER.toBukkitType() + ":" + e.getSpawner().getSpawnedType());
            int increaseAmount = e.getTarget().getStackAmount();

            if(increaseAmount < 0){
                island.handleBlockBreak(blockKey, -increaseAmount);
            }

            else if(island.hasReachedBlockLimit(blockKey, increaseAmount)){
                e.setCancelled(true);
            }

            else{
                island.handleBlockPlace(blockKey, increaseAmount);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerUnstack(SpawnerUnstackEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());
            if(island != null)
                island.handleBlockBreak(Key.of(Materials.SPAWNER.toBukkitType() + ":" + e.getSpawner().getSpawnedType()), e.getAmount());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerPlaceInventory(SpawnerPlaceInventoryEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());

            if(island == null)
                return;

            Key blockKey = Key.of(Materials.SPAWNER.toBukkitType() + ":" + e.getSpawner().getSpawnedType());
            int increaseAmount = e.getIncreaseAmount();

            if(island.hasReachedBlockLimit(blockKey, increaseAmount)){
                e.setCancelled(true);
                Locale.REACHED_BLOCK_LIMIT.send(e.getPlayer(), StringUtils.format(blockKey.toString()));
            }

            else{
                island.handleBlockPlace(blockKey, increaseAmount);
            }
        }

    }

    private static String getId(Location location){
        return getId(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    private static String getId(Chunk chunk){
        return getId(chunk.getX(), chunk.getZ());
    }

    private static String getId(ChunkPosition chunkPosition){
        return getId(chunkPosition.getX(), chunkPosition.getZ());
    }

    private static String getId(int x, int z){
        return x + "," + z;
    }

}

package com.bgsoftware.superiorskyblock.hooks.provider;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.SpawnersSnapshotProvider;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.hooks.support.WildStackerSnapshotsContainer;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.bgsoftware.wildstacker.api.events.SpawnerPlaceEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerPlaceInventoryEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerStackEvent;
import com.bgsoftware.wildstacker.api.events.SpawnerUnstackEvent;
import com.bgsoftware.wildstacker.api.objects.StackedSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Map;

public final class SpawnersProvider_WildStacker implements SpawnersProviderItemMetaSpawnerType, SpawnersSnapshotProvider {

    private static boolean registered = false;

    public SpawnersProvider_WildStacker(){
        if(!registered) {
            Bukkit.getPluginManager().registerEvents(new StackerListener(), SuperiorSkyblockPlugin.getPlugin());
            registered = true;
            SuperiorSkyblockPlugin.log("Using WildStacker as a spawners provider.");
        }
    }

    @Override
    public Pair<Integer, String> getSpawner(Location location) {
        StackedSnapshot cachedSnapshot = WildStackerSnapshotsContainer.getSnapshot(ChunkPosition.of(location));
        Map.Entry<Integer, EntityType> entry = cachedSnapshot.getStackedSpawner(location);
        return new Pair<>(entry.getKey(), entry.getValue() + "");
    }

    @Override
    public void takeSnapshot(Chunk chunk) {
        WildStackerSnapshotsContainer.takeSnapshot(chunk);
    }

    @Override
    public void releaseSnapshot(World world, int chunkX, int chunkZ) {
        WildStackerSnapshotsContainer.releaseSnapshot(ChunkPosition.of(world, chunkX, chunkZ));
    }

    @SuppressWarnings("unused")
    private static class StackerListener implements Listener{

        private final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerPlace(SpawnerPlaceEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());

            if(island == null)
                return;

            Key blockKey = Key.of(Materials.SPAWNER.toBukkitType() + "", e.getSpawner().getSpawnedType() + "");
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

            Key blockKey = Key.of(Materials.SPAWNER.toBukkitType() + "", e.getSpawner().getSpawnedType() + "");
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
                island.handleBlockBreak(Key.of(Materials.SPAWNER.toBukkitType() + "", e.getSpawner().getSpawnedType() + ""), e.getAmount());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerPlaceInventory(SpawnerPlaceInventoryEvent e){
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());

            if(island == null)
                return;

            Key blockKey = Key.of(Materials.SPAWNER.toBukkitType() + "", e.getSpawner().getSpawnedType() + "");
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

}

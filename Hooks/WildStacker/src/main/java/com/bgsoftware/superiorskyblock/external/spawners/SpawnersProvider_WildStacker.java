package com.bgsoftware.superiorskyblock.external.spawners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.SpawnersSnapshotProvider;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.external.WildStackerSnapshotsContainer;
import com.bgsoftware.superiorskyblock.module.upgrades.listeners.WildStackerListener;
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

public class SpawnersProvider_WildStacker implements SpawnersProviderItemMetaSpawnerType, SpawnersSnapshotProvider {

    private static boolean registered = false;

    private final SuperiorSkyblockPlugin plugin;

    public SpawnersProvider_WildStacker(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        if (!registered) {
            Bukkit.getPluginManager().registerEvents(new StackerListener(), plugin);
            Bukkit.getPluginManager().registerEvents(new WildStackerListener(), plugin);
            registered = true;
            Log.info("Using WildStacker as a spawners provider.");
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
    private class StackerListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerPlace(SpawnerPlaceEvent e) {
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());

            if (island == null)
                return;

            Key blockKey = Keys.ofSpawner(e.getSpawner().getSpawnedType());
            int increaseAmount = e.getSpawner().getStackAmount();

            if (island.hasReachedBlockLimit(blockKey, increaseAmount)) {
                e.setCancelled(true);
                Message.REACHED_BLOCK_LIMIT.send(e.getPlayer(), Formatters.CAPITALIZED_FORMATTER.format(blockKey.toString()));
            } else if (increaseAmount > 1) {
                island.handleBlockPlace(blockKey, increaseAmount - 1);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerStack(SpawnerStackEvent e) {
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());

            if (island == null)
                return;

            Key blockKey = Keys.ofSpawner(e.getSpawner().getSpawnedType());
            int increaseAmount = e.getTarget().getStackAmount();

            if (increaseAmount < 0) {
                island.handleBlockBreak(blockKey, -increaseAmount);
            } else if (island.hasReachedBlockLimit(blockKey, increaseAmount)) {
                e.setCancelled(true);
            } else {
                island.handleBlockPlace(blockKey, increaseAmount);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerUnstack(SpawnerUnstackEvent e) {
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());
            if (island != null)
                island.handleBlockBreak(Keys.ofSpawner(e.getSpawner().getSpawnedType()), e.getAmount());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerPlaceInventory(SpawnerPlaceInventoryEvent e) {
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());

            if (island == null)
                return;

            Key blockKey = Keys.ofSpawner(e.getSpawner().getSpawnedType());
            int increaseAmount = e.getIncreaseAmount();

            if (island.hasReachedBlockLimit(blockKey, increaseAmount)) {
                e.setCancelled(true);
                Message.REACHED_BLOCK_LIMIT.send(e.getPlayer(), Formatters.CAPITALIZED_FORMATTER.format(blockKey.toString()));
            } else {
                island.handleBlockPlace(blockKey, increaseAmount);
            }
        }

    }

}

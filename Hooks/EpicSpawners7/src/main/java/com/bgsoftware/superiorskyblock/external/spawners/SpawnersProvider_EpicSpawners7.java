package com.bgsoftware.superiorskyblock.external.spawners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.SpawnersProvider;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.google.common.base.Preconditions;
import com.songoda.epicspawners.EpicSpawners;
import com.songoda.epicspawners.api.events.SpawnerBreakEvent;
import com.songoda.epicspawners.api.events.SpawnerChangeEvent;
import com.songoda.epicspawners.api.events.SpawnerPlaceEvent;
import com.songoda.epicspawners.spawners.spawner.SpawnerData;
import com.songoda.epicspawners.spawners.spawner.SpawnerStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("unused")
public class SpawnersProvider_EpicSpawners7 implements SpawnersProvider {

    private static boolean registered = false;

    private final EpicSpawners instance = EpicSpawners.getInstance();
    private final SuperiorSkyblockPlugin plugin;

    public SpawnersProvider_EpicSpawners7(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        if (!registered) {
            Bukkit.getPluginManager().registerEvents(new SpawnersProvider_EpicSpawners7.StackerListener(), plugin);
            registered = true;
            Log.info("Using EpicSpawners as a spawners provider.");
        }
    }

    @Override
    public Pair<Integer, String> getSpawner(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");

        int blockCount = -1;
        String entityType = null;

        if (Bukkit.isPrimaryThread()) {
            SpawnerStack spawnerStack = instance.getSpawnerManager().getSpawnerFromWorld(location).getFirstStack();
            if (spawnerStack != null) {
                blockCount = spawnerStack.getStackSize();
                entityType = spawnerStack.getCurrentTier().getIdentifyingName();
            }
        }

        return new Pair<>(blockCount, entityType);
    }

    @Override
    public String getSpawnerType(ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "itemStack parameter cannot be null.");
        return instance.getSpawnerManager().getSpawnerTier(itemStack).getSpawnerData().getIdentifyingName();
    }

    private class StackerListener implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onSpawnerStack(SpawnerPlaceEvent e) {
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());

            if (island == null)
                return;

            SpawnerData spawnerData = e.getSpawner().getFirstStack().getSpawnerData();

            Key spawnerKey = Key.ofSpawner(e.getSpawner().getIdentifyingName());
            int increaseAmount = e.getSpawner().getFirstStack().getStackSize();

            if (spawnerData.isCustom()) {
                // Custom spawners are egg spawners. Therefore, we want to remove one egg spawner from the counts and
                // replace it with the custom spawner. We subtract the spawner 1 tick later, so it will be registered
                // before removing it.
                BukkitExecutor.sync(() -> island.handleBlockBreak(ConstantKeys.EGG_MOB_SPAWNER, 1), 1L);
            } else {
                // Vanilla spawners are listened in the vanilla listeners as well, and therefore 1 spawner is already
                // being counted by the other listeners. We need to subtract 1 so the counts will be adjusted correctly.
                increaseAmount--;
            }

            if (increaseAmount <= 0)
                return;

            if (island.hasReachedBlockLimit(spawnerKey, increaseAmount)) {
                e.setCancelled(true);
                Message.REACHED_BLOCK_LIMIT.send(e.getPlayer(), Formatters.CAPITALIZED_FORMATTER.format(spawnerKey.toString()));
            } else {
                island.handleBlockPlace(spawnerKey, increaseAmount);
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onSpawnerStack(SpawnerChangeEvent e) {
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());

            if (island == null)
                return;

            Key blockKey = Key.ofSpawner(e.getSpawner().getIdentifyingName());

            int increaseAmount = e.getStackSize() - e.getOldStackSize();

            if (increaseAmount < 0) {
                island.handleBlockBreak(blockKey, -increaseAmount);
            } else if (island.hasReachedBlockLimit(blockKey, increaseAmount)) {
                e.setCancelled(true);
                Message.REACHED_BLOCK_LIMIT.send(e.getPlayer(), Formatters.CAPITALIZED_FORMATTER.format(blockKey.toString()));
            } else {
                island.handleBlockPlace(blockKey, increaseAmount);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerUnstack(SpawnerBreakEvent e) {
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());

            if (island == null)
                return;

            Key blockKey = Key.ofSpawner(e.getSpawner().getIdentifyingName());

            island.handleBlockBreak(blockKey, e.getSpawner().getFirstStack().getStackSize());
        }

    }

}

package com.bgsoftware.superiorskyblock.external.spawners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.google.common.base.Preconditions;
import com.songoda.ultimatestacker.UltimateStacker;
import com.songoda.ultimatestacker.events.SpawnerBreakEvent;
import com.songoda.ultimatestacker.events.SpawnerPlaceEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class SpawnersProvider_UltimateStacker implements SpawnersProviderItemMetaSpawnerType {

    private static boolean registered = false;

    private final UltimateStacker instance = UltimateStacker.getInstance();

    private final SuperiorSkyblockPlugin plugin;

    public SpawnersProvider_UltimateStacker(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        if (!registered) {
            Bukkit.getPluginManager().registerEvents(new StackerListener(), plugin);
            registered = true;
            Log.info("Using UltimateStacker as a spawners provider.");
        }
    }

    @Override
    public Pair<Integer, String> getSpawner(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");

        int blockCount = -1;
        if (Bukkit.isPrimaryThread()) {
            blockCount = instance.getSpawnerStackManager().getSpawner(location).getAmount();
        }

        return new Pair<>(blockCount, null);
    }

    @SuppressWarnings("unused")
    private class StackerListener implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onSpawnerStack(SpawnerPlaceEvent e) {
            Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

            if (island == null)
                return;

            Key blockKey = Key.ofSpawner(e.getSpawnerType());
            int increaseAmount = e.getAmount();

            if (island.hasReachedBlockLimit(blockKey, increaseAmount)) {
                e.setCancelled(true);
                Message.REACHED_BLOCK_LIMIT.send(e.getPlayer(), Formatters.CAPITALIZED_FORMATTER.format(blockKey.toString()));
            } else {
                island.handleBlockPlace(blockKey, increaseAmount);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerUnstack(SpawnerBreakEvent e) {
            Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
            if (island != null)
                island.handleBlockBreak(e.getBlock(), e.getAmount());
        }

    }

}

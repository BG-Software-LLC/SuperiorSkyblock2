package com.bgsoftware.superiorskyblock.external.spawners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.google.common.base.Preconditions;
import com.vk2gpz.mergedspawner.api.MergedSpawnerAPI;
import com.vk2gpz.mergedspawner.event.MergedSpawnerBreakEvent;
import com.vk2gpz.mergedspawner.event.MergedSpawnerPlaceEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class SpawnersProvider_MergedSpawner implements SpawnersProvider_AutoDetect {

    private static boolean registered = false;

    private final SuperiorSkyblockPlugin plugin;

    public SpawnersProvider_MergedSpawner(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        if (!registered) {
            Bukkit.getPluginManager().registerEvents(new SpawnersProvider_MergedSpawner.StackerListener(), plugin);
            registered = true;
            Log.info("Using MergedSpawner as a spawners provider.");
        }
    }

    @Override
    public Pair<Integer, String> getSpawner(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        int blockCount = -1;

        if (Bukkit.isPrimaryThread()) {
            MergedSpawnerAPI spawnerAPI = MergedSpawnerAPI.getInstance();
            blockCount = spawnerAPI.getCountFor(location.getBlock());
        }

        return new Pair<>(blockCount, null);
    }

    @Override
    public String getSpawnerType(ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "itemStack parameter cannot be null.");
        return MergedSpawnerAPI.getInstance().getEntityType(itemStack).name();
    }

    @SuppressWarnings("unused")
    private class StackerListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerStack(MergedSpawnerPlaceEvent e) {
            Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
            int increaseAmount = e.getNewCount() - e.getOldCount();
            if (island != null)
                island.handleBlockPlace(e.getBlock(), increaseAmount);
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerUnstack(MergedSpawnerBreakEvent e) {
            Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
            int decreaseAmount = e.getOldCount() - e.getNewCount();
            if (island != null)
                island.handleBlockBreak(Keys.ofSpawner(e.getSpawnerType()), decreaseAmount);
        }

    }

}

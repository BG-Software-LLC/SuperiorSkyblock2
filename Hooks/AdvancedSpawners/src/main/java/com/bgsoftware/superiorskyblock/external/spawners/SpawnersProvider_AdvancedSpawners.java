package com.bgsoftware.superiorskyblock.external.spawners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.google.common.base.Preconditions;
import gcspawners.ASAPI;
import gcspawners.AdvancedSpawnerPlaceEvent;
import gcspawners.AdvancedSpawnersBreakEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

public class SpawnersProvider_AdvancedSpawners implements SpawnersProvider_AutoDetect {

    private static boolean registered = false;

    private final SuperiorSkyblockPlugin plugin;

    public SpawnersProvider_AdvancedSpawners(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        if (!registered) {
            Bukkit.getPluginManager().registerEvents(new SpawnersProvider_AdvancedSpawners.StackerListener(), plugin);
            registered = true;
            Log.info("Using AdvancedSpawners as a spawners provider.");
        }
    }

    @Override
    public Pair<Integer, String> getSpawner(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");
        return !Bukkit.isPrimaryThread() ? new Pair<>(-1, null) :
                new Pair<>(ASAPI.getSpawnerAmount(location), ASAPI.getSpawnerType(location).toUpperCase(Locale.ENGLISH));
    }

    @Override
    public String getSpawnerType(ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "itemStack parameter cannot be null.");
        return ASAPI.getSpawnerType(itemStack).toUpperCase(Locale.ENGLISH);
    }

    @SuppressWarnings("unused")
    private class StackerListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerStack(AdvancedSpawnerPlaceEvent e) {
            Location location = e.getSpawner().getLocation();

            Island island = plugin.getGrid().getIslandAt(location);

            if (island != null)
                island.handleBlockPlace(
                        Keys.ofSpawner(e.getEntityType()),
                        e.getCountPlaced());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerUnstack(AdvancedSpawnersBreakEvent e) {
            Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());
            if (island != null)
                island.handleBlockBreak(
                        Keys.ofSpawner(e.getEntityType()),
                        e.getCountBroken());
        }

    }

}

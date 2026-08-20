package com.bgsoftware.superiorskyblock.external.spawners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.google.common.base.Preconditions;
import github.nighter.smartspawner.api.SmartSpawnerAPI;
import github.nighter.smartspawner.api.SmartSpawnerProvider;
import github.nighter.smartspawner.api.data.SpawnerDataDTO;
import github.nighter.smartspawner.api.events.SpawnerBreakEvent;
import github.nighter.smartspawner.api.events.SpawnerEggChangeEvent;
import github.nighter.smartspawner.api.events.SpawnerPlaceEvent;
import github.nighter.smartspawner.api.events.SpawnerPlayerBreakEvent;
import github.nighter.smartspawner.api.events.SpawnerRemoveEvent;
import github.nighter.smartspawner.api.events.SpawnerStackEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class SpawnersProvider_SmartSpawner implements SpawnersProvider_AutoDetect {

    private final SuperiorSkyblockPlugin plugin;
    private final SmartSpawnerAPI api;

    public SpawnersProvider_SmartSpawner(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        this.api = SmartSpawnerProvider.getAPI();
        Bukkit.getPluginManager().registerEvents(new StackerListener(), plugin);
        Log.info("Using SmartSpawner as a spawners provider.");
    }

    @Override
    public Pair<Integer, String> getSpawner(Location location) {
        Preconditions.checkNotNull(location, "location parameter cannot be null.");

        int blockCount = -1;
        String entityType = null;

        if (Bukkit.isPrimaryThread()) {
            SpawnerDataDTO spawnerData = api.getSpawnerByLocation(location);
            blockCount = spawnerData == null ? 1 : spawnerData.getStackSize();
            entityType = spawnerData == null ? null : spawnerData.getEntityType().name();
        }

        return new Pair<>(blockCount, entityType);
    }

    @Override
    public String getSpawnerType(ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "itemStack parameter cannot be null.");

        EntityType entityType = api.getSpawnerEntityType(itemStack);

        return entityType == null ? null : entityType.name();
    }

    @SuppressWarnings("unused")
    private class StackerListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerBreak(SpawnerBreakEvent e) {
            Location location = e.getLocation();
            Island island = plugin.getGrid().getIslandAt(location);

            if (island != null) {
                island.handleBlockBreak(location.getBlock(), e.getQuantity());
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onSpawnerEggChangeEvent(SpawnerEggChangeEvent e) {
            Location location = e.getLocation();
            Island island = plugin.getGrid().getIslandAt(location);

            if (island == null) {
                return;
            }

            Key oldEntity = Keys.ofSpawner(e.getOldEntityType());
            Key newEntity = Keys.ofSpawner(e.getNewEntityType());
            SpawnerDataDTO spawnerData = api.getSpawnerByLocation(location);

            if (spawnerData == null) {
                return;
            }

            if (island.hasReachedBlockLimit(newEntity, spawnerData.getStackSize())) {
                e.setCancelled(true);
                Message.REACHED_BLOCK_LIMIT.send(e.getPlayer(), Formatters.CAPITALIZED_FORMATTER.format(newEntity.toString()));
                return;
            }

            island.handleBlockBreak(oldEntity, spawnerData.getStackSize());
            island.handleBlockPlace(location.getBlock(), spawnerData.getStackSize());
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onSpawnerPlace(SpawnerPlaceEvent e) {
            Location location = e.getLocation();
            Island island = plugin.getGrid().getIslandAt(location);

            if (island == null) {
                return;
            }

            Key key = Key.ofSpawner(e.getEntityType());

            if (island.hasReachedBlockLimit(key, e.getQuantity())) {
                e.setCancelled(true);
                Message.REACHED_BLOCK_LIMIT.send(e.getPlayer(), Formatters.CAPITALIZED_FORMATTER.format(key.toString()));
                return;
            }

            // SmartSpawner calls SpawnerPlaceEvent before completing the spawner setup.
            // It initializes the CreatureSpawner and creates SpawnerData 2 ticks later.
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                island.handleBlockPlace(location.getBlock(), e.getQuantity());
            }, 3L);
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerPlayerBreak(SpawnerPlayerBreakEvent e) {
            Location location = e.getLocation();
            Island island = plugin.getGrid().getIslandAt(location);

            if (island != null) {
                island.handleBlockBreak(location.getBlock(), e.getQuantity());
            }
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpawnerRemove(SpawnerRemoveEvent e) {
            Location location = e.getLocation();
            Island island = plugin.getGrid().getIslandAt(location);

            if (island != null) {
                island.handleBlockBreak(location.getBlock(), e.getChangeAmount());
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onSpawnerStack(SpawnerStackEvent e) {
            Location location = e.getLocation();
            Island island = plugin.getGrid().getIslandAt(location);

            if (island == null) {
                return;
            }

            int amount = e.getNewStackSize() - e.getOldStackSize();
            SpawnerDataDTO spawnerData = api.getSpawnerByLocation(location);

            if (spawnerData == null) {
                return;
            }

            Key key = Key.ofSpawner(spawnerData.getEntityType());

            if (island.hasReachedBlockLimit(key, amount)) {
                e.setCancelled(true);
                Message.REACHED_BLOCK_LIMIT.send(e.getPlayer(), Formatters.CAPITALIZED_FORMATTER.format(key.toString()));
                return;
            }

            island.handleBlockPlace(location.getBlock(), amount);
        }

    }

}

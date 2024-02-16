package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.world.WorldRecordService;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeEntityLimits;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

public class EntityTrackingListener implements Listener {

    private final LazyReference<WorldRecordService> worldRecordService = new LazyReference<WorldRecordService>() {
        @Override
        protected WorldRecordService create() {
            return plugin.getServices().getService(WorldRecordService.class);
        }
    };
    private final SuperiorSkyblockPlugin plugin;

    public EntityTrackingListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        this.registerDeathListener();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onEntitySpawn(CreatureSpawnEvent e) {
        this.worldRecordService.get().recordEntitySpawn(e.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onHangingPlace(HangingPlaceEvent e) {
        this.worldRecordService.get().recordEntitySpawn(e.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onVehicleSpawn(VehicleCreateEvent e) {
        this.worldRecordService.get().recordEntitySpawn(e.getVehicle());
    }

    /* INTERNAL */

    private void registerDeathListener() {
        Listener deathListener;

        try {
            Class.forName("com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent");
            deathListener = new PaperDeathListener();
        } catch (ClassNotFoundException error) {
            deathListener = new SpigotDeathListener();
        }

        plugin.getServer().getPluginManager().registerEvents(deathListener, plugin);
    }

    private class PaperDeathListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        private void onEntityRemove(com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent e) {
            if (!BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeEntityLimits.class) ||
                    !BukkitEntities.canHaveLimit(e.getEntityType()) ||
                    BukkitEntities.canBypassEntityLimit(e.getEntity()))
                return;

            Location entityLocation = e.getEntity().getLocation();

            BukkitExecutor.sync(() -> {
                if (e.getEntity().isValid() && !e.getEntity().isDead())
                    return;

                World world = entityLocation.getWorld();
                int chunkX = entityLocation.getBlockX() >> 4;
                int chunkZ = entityLocation.getBlockZ() >> 4;
                // We don't want to track entities that are removed due to chunk being unloaded.
                if (world.isChunkLoaded(chunkX, chunkZ)) {
                    worldRecordService.get().recordEntityDespawn(e.getEntity());
                }
            }, 1L);
        }

    }

    private class SpigotDeathListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        private void onEntityDeath(EntityDeathEvent e) {
            worldRecordService.get().recordEntityDespawn(e.getEntity());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        private void onVehicleDestroy(VehicleDestroyEvent e) {
            worldRecordService.get().recordEntityDespawn(e.getVehicle());
        }

    }

}

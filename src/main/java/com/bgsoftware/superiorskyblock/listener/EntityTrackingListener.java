package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.world.WorldRecordService;
import com.bgsoftware.superiorskyblock.core.LazyReference;
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
            worldRecordService.get().recordEntityDespawn(e.getEntity());
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

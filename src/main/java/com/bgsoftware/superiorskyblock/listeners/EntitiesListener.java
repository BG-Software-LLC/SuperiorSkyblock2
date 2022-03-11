package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.logic.EntitiesLogic;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;

public final class EntitiesListener implements Listener {

    public EntitiesListener(SuperiorSkyblockPlugin plugin) {
        Listener deathListener;

        try {
            Class.forName("com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent");
            deathListener = new PaperDeathListener();
        } catch (ClassNotFoundException error) {
            deathListener = new SpigotDeathListener();
        }

        plugin.getServer().getPluginManager().registerEvents(deathListener, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntitySpawn(CreatureSpawnEvent e) {
        EntitiesLogic.handleSpawn(e.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent e) {
        EntitiesLogic.handleSpawn(e.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleSpawn(VehicleCreateEvent e) {
        EntitiesLogic.handleSpawn(e.getVehicle());
    }

    private static final class PaperDeathListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onEntityRemove(com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent e) {
            EntitiesLogic.handleDespawn(e.getEntity());
        }

    }

    private static final class SpigotDeathListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onEntityDeath(EntityDeathEvent e) {
            EntitiesLogic.handleDespawn(e.getEntity());
        }

    }

}

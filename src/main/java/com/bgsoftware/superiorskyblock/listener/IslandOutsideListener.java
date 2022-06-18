package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.world.EntityTeleports;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.jetbrains.annotations.Nullable;

public class IslandOutsideListener implements Listener {

    private final SuperiorSkyblockPlugin plugin;

    public IslandOutsideListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onMinecartRightClick(PlayerInteractAtEntityEvent e) {
        if (!plugin.getSettings().isStopLeaving())
            return;

        Island playerIsland = plugin.getGrid().getIslandAt(e.getPlayer().getLocation());
        Island entityIsland = plugin.getGrid().getIslandAt(e.getRightClicked().getLocation());

        if (plugin.getPlayers().getSuperiorPlayer(e.getPlayer()).hasBypassModeEnabled())
            return;

        if (playerIsland != null && (entityIsland == null || entityIsland.equals(playerIsland)) &&
                !playerIsland.isInsideRange(e.getRightClicked().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onMinecartRightClick(VehicleEnterEvent e) {
        if (!plugin.getSettings().isStopLeaving())
            return;

        Island playerIsland = plugin.getGrid().getIslandAt(e.getEntered().getLocation());
        Island entityIsland = plugin.getGrid().getIslandAt(e.getVehicle().getLocation());

        if (e.getEntered() instanceof Player && plugin.getPlayers().getSuperiorPlayer(e.getEntered()).hasBypassModeEnabled())
            return;

        if (playerIsland != null && (entityIsland == null || entityIsland.equals(playerIsland)) &&
                !playerIsland.isInsideRange(e.getVehicle().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onVehicleRide(VehicleMoveEvent e) {
        if (!plugin.getSettings().isStopLeaving())
            return;

        Location from = e.getFrom();
        Location to = e.getTo();

        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ())
            return;

        Island toIsland = plugin.getGrid().getIslandAt(e.getTo());
        Island fromIsland = plugin.getGrid().getIslandAt(e.getFrom());

        if (fromIsland != null && e.getVehicle().getWorld().equals(e.getTo().getWorld()) &&
                (toIsland == null || toIsland.equals(fromIsland)) && !fromIsland.isInsideRange(e.getTo())) {
            Entity passenger = e.getVehicle().getPassenger();
            SuperiorPlayer superiorPlayer = passenger instanceof Player ? plugin.getPlayers().getSuperiorPlayer(passenger) : null;
            if (passenger != null && (superiorPlayer == null || !superiorPlayer.hasBypassModeEnabled())) {
                e.getVehicle().setPassenger(null);
                EntityTeleports.teleport(passenger, e.getFrom());
            }
        }
    }

    public void onPlayerMove(SuperiorPlayer superiorPlayer, Location to,
                             @Nullable Island fromIsland, @Nullable Island toIsland) {
        if (!plugin.getSettings().isStopLeaving())
            return;

        World playerWorld = superiorPlayer.getWorld();

        if (playerWorld != null && !superiorPlayer.hasBypassModeEnabled() &&
                plugin.getGrid().isIslandsWorld(playerWorld) && playerWorld.equals(to.getWorld()) &&
                (fromIsland == null || toIsland == null || toIsland.equals(fromIsland)) &&
                (fromIsland == null || !fromIsland.isInsideRange(to, 1))) {
            superiorPlayer.teleport(fromIsland == null ? plugin.getGrid().getSpawnIsland() : fromIsland);
        }
    }

}

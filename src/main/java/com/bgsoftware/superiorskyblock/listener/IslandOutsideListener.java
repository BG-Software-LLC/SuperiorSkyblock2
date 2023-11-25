package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

public class IslandOutsideListener implements Listener {

    private final SuperiorSkyblockPlugin plugin;

    public IslandOutsideListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onMinecartRightClick(PlayerInteractAtEntityEvent e) {
        if (!plugin.getSettings().isStopLeaving())
            return;

        if (!plugin.getGrid().isIslandsWorld(e.getRightClicked().getWorld()))
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        if (superiorPlayer.hasBypassModeEnabled())
            return;

        Location entityLocation = e.getRightClicked().getLocation();
        Island entityIsland = plugin.getGrid().getIslandAt(entityLocation);

        if (entityIsland != null && entityIsland.isInsideRange(entityLocation))
            return;

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onMinecartRightClick(VehicleEnterEvent e) {
        if (!plugin.getSettings().isStopLeaving())
            return;

        if (!plugin.getGrid().isIslandsWorld(e.getVehicle().getWorld()))
            return;

        if (e.getEntered() instanceof Player && plugin.getPlayers().getSuperiorPlayer(e.getEntered()).hasBypassModeEnabled())
            return;

        Location vehicleLocation = e.getVehicle().getLocation();
        Island entityIsland = plugin.getGrid().getIslandAt(vehicleLocation);

        if (entityIsland != null && entityIsland.isInsideRange(vehicleLocation))
            return;

        e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onVehicleRide(VehicleMoveEvent e) {
        if (!plugin.getSettings().isStopLeaving())
            return;

        World world = e.getTo().getWorld();

        if (!plugin.getGrid().isIslandsWorld(world))
            return;

        if (!e.getVehicle().getWorld().equals(world))
            return;

        Location from = e.getFrom();
        Location to = e.getTo();

        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ())
            return;

        Entity passenger = e.getVehicle().getPassenger();
        SuperiorPlayer superiorPlayer = passenger instanceof Player ? plugin.getPlayers().getSuperiorPlayer(passenger) : null;

        if (superiorPlayer == null)
            return;

        handlePlayerMove(superiorPlayer, from, to, false, true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onPlayerMove(PlayerMoveEvent e) {
        if (!plugin.getSettings().isStopLeaving())
            return;

        World world = e.getTo().getWorld();

        if (!plugin.getGrid().isIslandsWorld(world))
            return;

        if (!e.getPlayer().getWorld().equals(world))
            return;

        Location from = e.getFrom();
        Location to = e.getTo();

        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ())
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        if (handlePlayerMove(superiorPlayer, from, to, true, false))
            e.setCancelled(true);
    }

    private boolean handlePlayerMove(SuperiorPlayer superiorPlayer, Location from, Location to,
                                     boolean delayTeleport, boolean forceTeleport) {
        if (superiorPlayer.hasBypassModeEnabled())
            return false;

        Island toIsland = plugin.getGrid().getIslandAt(to);
        if (toIsland != null && toIsland.isInsideRange(to))
            return false;

        if (delayTeleport) {
            // If we don't delay the teleport, it will not occur due to the cancellation of PlayerMoveEvent
            BukkitExecutor.sync(() -> handlePlayerMoveOutsideIslandTeleport(superiorPlayer, from, forceTeleport), 1L);
        } else {
            handlePlayerMoveOutsideIslandTeleport(superiorPlayer, from, forceTeleport);
        }

        return true;
    }

    private void handlePlayerMoveOutsideIslandTeleport(SuperiorPlayer superiorPlayer, Location from, boolean forceTeleport) {
        Island fromIsland = plugin.getGrid().getIslandAt(from);

        // We don't teleport in case we're inside the island, we just cancel the event.
        if (!forceTeleport && fromIsland != null && fromIsland.isInsideRange(from))
            return;

        if (fromIsland != null) {
            superiorPlayer.teleport(fromIsland, result -> {
                if (!result) {
                    superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
                }
            });
        } else {
            superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
        }
    }

}

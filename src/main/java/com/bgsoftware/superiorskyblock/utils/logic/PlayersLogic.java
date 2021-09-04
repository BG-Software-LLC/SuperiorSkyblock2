package com.bgsoftware.superiorskyblock.utils.logic;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandEnterEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandLeaveEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandRestrictMoveEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandFlags;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import java.util.Collections;

public final class PlayersLogic {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private PlayersLogic() {
    }

    public static void handleJoin(SuperiorPlayer superiorPlayer) {
        superiorPlayer.updateLastTimeStatus();

        Island island = superiorPlayer.getIsland();

        if (island != null) {
            IslandUtils.sendMessage(island, Locale.PLAYER_JOIN_ANNOUNCEMENT, Collections.singletonList(superiorPlayer.getUniqueId()), superiorPlayer.getName());
            island.updateLastTime();
            island.setCurrentlyActive();
        }
    }

    public static void handleQuit(SuperiorPlayer superiorPlayer) {
        superiorPlayer.updateLastTimeStatus();

        Island island = superiorPlayer.getIsland();

        if (island != null) {
            IslandUtils.sendMessage(island, Locale.PLAYER_QUIT_ANNOUNCEMENT, Collections.singletonList(superiorPlayer.getUniqueId()), superiorPlayer.getName());
            boolean anyOnline = island.getIslandMembers(true).stream().anyMatch(_superiorPlayer ->
                    !_superiorPlayer.getUniqueId().equals(superiorPlayer.getUniqueId()) && _superiorPlayer.isOnline());
            if (!anyOnline)
                island.setLastTimeUpdate(System.currentTimeMillis() / 1000);
        }
    }

    public static boolean handlePlayerLeaveIsland(SuperiorPlayer superiorPlayer, Location fromLocation, Location toLocation,
                                                  Island fromIsland, Island toIsland, IslandLeaveEvent.LeaveCause leaveCause,
                                                  Event event) {
        if (fromIsland == null)
            return true;

        boolean equalWorlds = toLocation != null && fromLocation.getWorld().equals(toLocation.getWorld());
        boolean equalIslands = fromIsland.equals(toIsland);
        boolean fromInsideRange = fromIsland.isInsideRange(fromLocation);
        boolean toInsideRange = toLocation != null && toIsland != null && toIsland.isInsideRange(toLocation);

        //Checking for the stop leaving feature.
        if (plugin.getSettings().isStopLeaving() && fromInsideRange && !toInsideRange && !superiorPlayer.hasBypassModeEnabled() &&
                !fromIsland.isSpawn() && equalWorlds) {
            EventsCaller.callIslandRestrictMoveEvent(superiorPlayer, IslandRestrictMoveEvent.RestrictReason.LEAVE_ISLAND_TO_OUTSIDE);
            superiorPlayer.setLeavingFlag(true);
            if (event instanceof Cancellable)
                ((Cancellable) event).setCancelled(true);
            return false;
        }

        // Handling the leave protected event
        if (fromInsideRange && (!equalIslands || !toInsideRange)) {
            if (!EventsCaller.callIslandLeaveProtectedEvent(superiorPlayer, fromIsland, leaveCause, toLocation)) {
                EventsCaller.callIslandRestrictMoveEvent(superiorPlayer, IslandRestrictMoveEvent.RestrictReason.LEAVE_PROTECTED_EVENT_CANCELLED);
                if (event instanceof Cancellable)
                    ((Cancellable) event).setCancelled(true);
                return false;
            }
        }

        if (equalIslands)
            return true;

        if (!EventsCaller.callIslandLeaveEvent(superiorPlayer, fromIsland, leaveCause, toLocation)) {
            EventsCaller.callIslandRestrictMoveEvent(superiorPlayer, IslandRestrictMoveEvent.RestrictReason.LEAVE_EVENT_CANCELLED);
            if (event instanceof Cancellable)
                ((Cancellable) event).setCancelled(true);
            return false;
        }

        fromIsland.setPlayerInside(superiorPlayer, false);

        Player player = superiorPlayer.asPlayer();
        assert player != null;

        player.resetPlayerTime();
        player.resetPlayerWeather();
        fromIsland.removeEffects(superiorPlayer);

        if (superiorPlayer.hasIslandFlyEnabled() && (toIsland == null || toIsland.isSpawn()) && !superiorPlayer.hasFlyGamemode()) {
            player.setAllowFlight(false);
            player.setFlying(false);
            Locale.ISLAND_FLY_DISABLED.send(player);
        }

        if (toIsland == null)
            plugin.getNMSWorld().setWorldBorder(superiorPlayer, null);

        return true;
    }

    public static void handlePlayerEnterIsland(SuperiorPlayer superiorPlayer, Location fromLocation, Location toLocation,
                                               Island fromIsland, Island toIsland, IslandEnterEvent.EnterCause enterCause,
                                               Event event) {
        if (toIsland == null)
            return;

        // This can happen after the leave event is cancelled.
        if (superiorPlayer.isLeavingFlag()) {
            superiorPlayer.setLeavingFlag(false);
            return;
        }

        // Checking if the player is banned from the island.
        if (toIsland.isBanned(superiorPlayer) && !superiorPlayer.hasBypassModeEnabled() &&
                !superiorPlayer.hasPermissionWithoutOP("superior.admin.ban.bypass")) {
            EventsCaller.callIslandRestrictMoveEvent(superiorPlayer, IslandRestrictMoveEvent.RestrictReason.BANNED_FROM_ISLAND);
            if (event instanceof Cancellable)
                ((Cancellable) event).setCancelled(true);
            Locale.BANNED_FROM_ISLAND.send(superiorPlayer);
            superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
            return;
        }

        // Checking if the player is locked to visitors.
        if (toIsland.isLocked() && !toIsland.hasPermission(superiorPlayer, IslandPrivileges.CLOSE_BYPASS)) {
            EventsCaller.callIslandRestrictMoveEvent(superiorPlayer, IslandRestrictMoveEvent.RestrictReason.LOCKED_ISLAND);
            if (event instanceof Cancellable)
                ((Cancellable) event).setCancelled(true);
            Locale.NO_CLOSE_BYPASS.send(superiorPlayer);
            superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
            return;
        }

        boolean equalIslands = toIsland.equals(fromIsland);
        boolean toInsideRange = toIsland.isInsideRange(toLocation);
        boolean fromInsideRange = fromLocation != null && fromIsland != null && fromIsland.isInsideRange(fromLocation);
        boolean equalWorlds = fromLocation != null && toLocation.getWorld().equals(fromLocation.getWorld());
        Player player = superiorPlayer.asPlayer();
        assert player != null;

        if (toInsideRange && (!equalIslands || !fromInsideRange)) {
            if (!EventsCaller.callIslandEnterProtectedEvent(superiorPlayer, toIsland, enterCause)) {
                EventsCaller.callIslandRestrictMoveEvent(superiorPlayer, IslandRestrictMoveEvent.RestrictReason.ENTER_PROTECTED_EVENT_CANCELLED);
                if (event instanceof Cancellable)
                    ((Cancellable) event).setCancelled(true);
                return;
            }
        }

        if (equalIslands) {
            if (!equalWorlds) {
                Executor.sync(() -> plugin.getNMSWorld().setWorldBorder(superiorPlayer, toIsland), 1L);
                superiorPlayer.setImmunedToPortals(true);
                Executor.sync(() -> superiorPlayer.setImmunedToPortals(false), 100L);
            }
            return;
        }

        if (!EventsCaller.callIslandEnterEvent(superiorPlayer, toIsland, enterCause)) {
            EventsCaller.callIslandRestrictMoveEvent(superiorPlayer, IslandRestrictMoveEvent.RestrictReason.ENTER_EVENT_CANCELLED);
            if (event instanceof Cancellable)
                ((Cancellable) event).setCancelled(true);
            return;
        }

        toIsland.setPlayerInside(superiorPlayer, true);

        if (!toIsland.isMember(superiorPlayer) && toIsland.hasSettingsEnabled(IslandFlags.PVP)) {
            Locale.ENTER_PVP_ISLAND.send(superiorPlayer);
            if (plugin.getSettings().isImmuneToPvPWhenTeleport()) {
                superiorPlayer.setImmunedToPvP(true);
                Executor.sync(() -> superiorPlayer.setImmunedToPvP(false), 200L);
            }
        }

        superiorPlayer.setImmunedToPortals(true);
        Executor.sync(() -> superiorPlayer.setImmunedToPortals(false), 100L);

        if (plugin.getSettings().getSpawn().isProtected() || !toIsland.isSpawn()) {
            if (toIsland.hasSettingsEnabled(IslandFlags.ALWAYS_DAY)) {
                player.setPlayerTime(0, false);
            } else if (toIsland.hasSettingsEnabled(IslandFlags.ALWAYS_MIDDLE_DAY)) {
                player.setPlayerTime(6000, false);
            } else if (toIsland.hasSettingsEnabled(IslandFlags.ALWAYS_NIGHT)) {
                player.setPlayerTime(14000, false);
            } else if (toIsland.hasSettingsEnabled(IslandFlags.ALWAYS_MIDDLE_NIGHT)) {
                player.setPlayerTime(18000, false);
            }

            if (toIsland.hasSettingsEnabled(IslandFlags.ALWAYS_SHINY)) {
                player.setPlayerWeather(WeatherType.CLEAR);
            } else if (toIsland.hasSettingsEnabled(IslandFlags.ALWAYS_RAIN)) {
                player.setPlayerWeather(WeatherType.DOWNFALL);
            }
        }

        toIsland.applyEffects(superiorPlayer);

        if (superiorPlayer.hasIslandFlyEnabled() && !superiorPlayer.hasFlyGamemode()) {
            Executor.sync(() -> {
                if (player.isOnline())
                    toIsland.updateIslandFly(superiorPlayer);
            }, 5L);
        }

        Executor.sync(() -> plugin.getNMSWorld().setWorldBorder(superiorPlayer, toIsland), 1L);
    }

}

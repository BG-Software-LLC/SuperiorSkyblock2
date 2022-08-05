package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.HitActionResult;
import com.bgsoftware.superiorskyblock.api.events.IslandEnterEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandLeaveEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandRestrictMoveEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandUncoopPlayerEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.Singleton;
import com.bgsoftware.superiorskyblock.core.collections.AutoRemovalCollection;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.SIslandChest;
import com.bgsoftware.superiorskyblock.island.flag.IslandFlags;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.island.top.SortingTypes;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import com.bgsoftware.superiorskyblock.player.SuperiorNPCPlayer;
import com.bgsoftware.superiorskyblock.player.chat.PlayerChat;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayersListener implements Listener {

    private final Collection<UUID> noFallDamage = AutoRemovalCollection.newHashSet(1, TimeUnit.SECONDS);

    private final SuperiorSkyblockPlugin plugin;
    private final Singleton<IslandPreviewListener> islandPreviewListener;
    private final Singleton<IslandOutsideListener> islandOutsideListener;

    public PlayersListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        this.islandPreviewListener = plugin.getListener(IslandPreviewListener.class);
        this.islandOutsideListener = plugin.getListener(IslandOutsideListener.class);
    }

    /* PLAYER NOTIFIERS */

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPlayerLogin(PlayerLoginEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        List<SuperiorPlayer> duplicatedPlayers = plugin.getPlayers().matchAllPlayers(_superiorPlayer ->
                _superiorPlayer != superiorPlayer && _superiorPlayer.getName().equalsIgnoreCase(e.getPlayer().getName()));
        if (!duplicatedPlayers.isEmpty()) {
            SuperiorSkyblockPlugin.log("Changing UUID of " + superiorPlayer.getName() + " to " + superiorPlayer.getUniqueId());
            for (SuperiorPlayer duplicatePlayer : duplicatedPlayers) {
                plugin.getPlayers().replacePlayers(duplicatePlayer, superiorPlayer);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPlayerJoin(PlayerJoinEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if (superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        // Updating the name of the player.
        if (!superiorPlayer.getName().equals(e.getPlayer().getName())) {
            plugin.getEventsBus().callPlayerChangeNameEvent(superiorPlayer, e.getPlayer().getName());
            superiorPlayer.updateName();
        }

        superiorPlayer.updateLastTimeStatus();

        // Handling player join
        if (superiorPlayer.isShownAsOnline())
            notifyPlayerJoin(superiorPlayer);

        Island island = plugin.getGrid().getIslandAt(e.getPlayer().getLocation());
        if (island != null) {
            onPlayerEnterIsland(superiorPlayer, null, null, e.getPlayer().getLocation(), island,
                    IslandEnterEvent.EnterCause.PLAYER_JOIN);
        }

        BukkitExecutor.sync(() -> {
            if (!e.getPlayer().isOnline())
                return;

            // Updating skin of the player
            if (!plugin.getProviders().notifySkinsListeners(superiorPlayer))
                plugin.getNMSPlayers().setSkinTexture(superiorPlayer);

            // Checking if the player is in the islands world, not inside an island.
            if (plugin.getGrid().isIslandsWorld(superiorPlayer.getWorld()) && island == null) {
                superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
                Message.ISLAND_GOT_DELETED_WHILE_INSIDE.send(superiorPlayer);
            }

            // Checking auto language detection
            if (plugin.getSettings().isAutoLanguageDetection() && !e.getPlayer().hasPlayedBefore()) {
                Locale playerLocale = plugin.getNMSPlayers().getPlayerLocale(e.getPlayer());
                if (playerLocale != null && PlayerLocales.isValidLocale(playerLocale) &&
                        !superiorPlayer.getUserLocale().equals(playerLocale)) {
                    if (plugin.getEventsBus().callPlayerChangeLanguageEvent(superiorPlayer, playerLocale))
                        superiorPlayer.setUserLocale(playerLocale);
                }
            }
        }, 5L);
    }

    public void notifyPlayerJoin(SuperiorPlayer superiorPlayer) {
        Island island = superiorPlayer.getIsland();
        if (island != null) {
            IslandUtils.sendMessage(island, Message.PLAYER_JOIN_ANNOUNCEMENT, Collections.singletonList(superiorPlayer.getUniqueId()), superiorPlayer.getName());
            island.updateLastTime();
            island.setCurrentlyActive();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPlayerQuit(PlayerQuitEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if (superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        // Removing coop status from other islands.
        for (Island _island : plugin.getGrid().getIslands()) {
            if (_island.isCoop(superiorPlayer)) {
                if (plugin.getEventsBus().callIslandUncoopPlayerEvent(_island, null, superiorPlayer, IslandUncoopPlayerEvent.UncoopReason.SERVER_LEAVE)) {
                    _island.removeCoop(superiorPlayer);
                    IslandUtils.sendMessage(_island, Message.UNCOOP_LEFT_ANNOUNCEMENT, Collections.emptyList(), superiorPlayer.getName());
                }
            }
        }

        superiorPlayer.updateLastTimeStatus();

        // Handling player quit
        if (superiorPlayer.isShownAsOnline())
            notifyPlayerQuit(superiorPlayer);

        // Remove coop players
        Island island = superiorPlayer.getIsland();
        if (island != null && plugin.getSettings().isAutoUncoopWhenAlone() && !island.getCoopPlayers().isEmpty()) {
            boolean shouldRemoveCoops = island.getIslandMembers(true).stream().noneMatch(islandMember ->
                    islandMember != superiorPlayer && island.hasPermission(islandMember, IslandPrivileges.UNCOOP_MEMBER) && islandMember.isOnline());

            if (shouldRemoveCoops) {
                for (SuperiorPlayer coopPlayer : island.getCoopPlayers()) {
                    island.removeCoop(coopPlayer);
                    Message.UNCOOP_AUTO_ANNOUNCEMENT.send(coopPlayer);
                }
            }
        }

        Location playerLocation = e.getPlayer().getLocation();
        Island islandAtLocation = plugin.getGrid().getIslandAt(playerLocation);
        if (islandAtLocation != null) {
            islandAtLocation.setPlayerInside(superiorPlayer, false);
            onPlayerLeaveIsland(superiorPlayer, playerLocation, islandAtLocation, null, null,
                    IslandLeaveEvent.LeaveCause.PLAYER_QUIT);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPlayerGameModeChange(PlayerGameModeChangeEvent e) {
        if (e.getNewGameMode() == GameMode.SPECTATOR) {
            notifyPlayerQuit(plugin.getPlayers().getSuperiorPlayer(e.getPlayer()));
        } else if (e.getPlayer().getGameMode() == GameMode.SPECTATOR) {
            notifyPlayerJoin(plugin.getPlayers().getSuperiorPlayer(e.getPlayer()));
        }
    }

    public void notifyPlayerQuit(SuperiorPlayer superiorPlayer) {
        Island island = superiorPlayer.getIsland();

        if (island == null)
            return;

        IslandUtils.sendMessage(island, Message.PLAYER_QUIT_ANNOUNCEMENT, Collections.singletonList(superiorPlayer.getUniqueId()), superiorPlayer.getName());

        boolean anyOnline = island.getIslandMembers(true).stream().anyMatch(islandMember ->
                islandMember != superiorPlayer && islandMember.isOnline());

        if (!anyOnline)
            island.setLastTimeUpdate(System.currentTimeMillis() / 1000);
    }

    /* PLAYER MOVES */

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onPlayerMove(PlayerMoveEvent e) {
        Location from = e.getFrom();
        Location to = e.getTo();

        // Check if player moved a block.
        if (from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ()) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

            if (superiorPlayer instanceof SuperiorNPCPlayer)
                return;

            Island fromIsland = plugin.getGrid().getIslandAt(from);
            Island toIsland = plugin.getGrid().getIslandAt(to);

            // Handle moving while in teleport warmup.
            BukkitTask teleportTask = superiorPlayer.getTeleportTask();
            if (teleportTask != null) {
                teleportTask.cancel();
                superiorPlayer.setTeleportTask(null);
                Message.TELEPORT_WARMUP_CANCEL.send(superiorPlayer);
            }

            // Handle moving while in island preview mode
            islandPreviewListener.get().onPlayerMove(superiorPlayer, to);

            if (toIsland != null && preventPlayerEnterIsland(superiorPlayer, from, fromIsland, to, toIsland,
                    IslandEnterEvent.EnterCause.PLAYER_MOVE)) {
                e.setCancelled(true);
                return;
            } else if (fromIsland != null && preventPlayerLeaveIsland(superiorPlayer, from, fromIsland, to, toIsland,
                    IslandLeaveEvent.LeaveCause.PLAYER_MOVE)) {
                e.setCancelled(true);
                return;
            } else {
                // Handle moving outside of islands
                islandOutsideListener.get().onPlayerMove(superiorPlayer, to, fromIsland, toIsland);
            }
        }

        // Check for falling out of the void
        if (from.getBlockY() != to.getBlockY() && to.getBlockY() <= plugin.getNMSWorld().getMinHeight(to.getWorld()) - 5) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

            Island island = plugin.getGrid().getIslandAt(e.getPlayer().getLocation());

            if (island == null || (island.isVisitor(superiorPlayer, false) ?
                    !plugin.getSettings().getVoidTeleport().isVisitors() : !plugin.getSettings().getVoidTeleport().isMembers()))
                return;

            PluginDebugger.debug("Action: Void Teleport, Player: " + superiorPlayer.getName());

            noFallDamage.add(e.getPlayer().getUniqueId());
            superiorPlayer.teleport(island, result -> {
                if (!result) {
                    Message.TELEPORTED_FAILED.send(superiorPlayer);
                    superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onPlayerTeleport(PlayerTeleportEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if (superiorPlayer == null || superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        Island fromIsland = plugin.getGrid().getIslandAt(e.getFrom());
        Island toIsland = plugin.getGrid().getIslandAt(e.getTo());

        if (toIsland != null && preventPlayerEnterIsland(superiorPlayer, e.getFrom(), fromIsland, e.getTo(), toIsland,
                IslandEnterEvent.EnterCause.PLAYER_TELEPORT)) {
            e.setCancelled(true);
        } else if (fromIsland != null && preventPlayerLeaveIsland(superiorPlayer, e.getFrom(), fromIsland, e.getTo(), toIsland,
                IslandLeaveEvent.LeaveCause.PLAYER_TELEPORT)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPlayerChangeWorld(PlayerChangedWorldEvent e) {
        Island island = plugin.getGrid().getIslandAt(e.getPlayer().getLocation());
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if (island != null && superiorPlayer.hasIslandFlyEnabled() && !e.getPlayer().getAllowFlight() &&
                island.hasPermission(superiorPlayer, IslandPrivileges.FLY))
            BukkitExecutor.sync(() -> {
                e.getPlayer().setAllowFlight(true);
                e.getPlayer().setFlying(true);
            }, 1L);
    }

    public void onPlayerLeaveIsland(SuperiorPlayer superiorPlayer,
                                    @NotNull Location from,
                                    @NotNull Island fromIsland,
                                    @Nullable Location to,
                                    @Nullable Island toIsland,
                                    IslandLeaveEvent.LeaveCause leaveCause) {
        /* Alias for preventPlayerLeaveIsland */
        this.preventPlayerLeaveIsland(superiorPlayer, from, fromIsland, to, toIsland, leaveCause);
    }

    public boolean preventPlayerLeaveIsland(SuperiorPlayer superiorPlayer,
                                            @NotNull Location from,
                                            @NotNull Island fromIsland,
                                            @Nullable Location to,
                                            @Nullable Island toIsland,
                                            IslandLeaveEvent.LeaveCause leaveCause) {
        boolean equalWorlds = to != null && from.getWorld().equals(to.getWorld());
        boolean equalIslands = fromIsland.equals(toIsland);
        boolean fromInsideRange = fromIsland.isInsideRange(from);
        boolean toInsideRange = to != null && toIsland != null && toIsland.isInsideRange(to);

        //Checking for the stop leaving feature.
        if (plugin.getSettings().isStopLeaving() && fromInsideRange && !toInsideRange &&
                !superiorPlayer.hasBypassModeEnabled() && !fromIsland.isSpawn() && equalWorlds) {
            plugin.getEventsBus().callIslandRestrictMoveEvent(superiorPlayer, IslandRestrictMoveEvent.RestrictReason.LEAVE_ISLAND_TO_OUTSIDE);
            superiorPlayer.setLeavingFlag(true);
            return true;
        }

        // Handling the leave protected event
        if (fromInsideRange && (!equalIslands || !toInsideRange)) {
            if (!plugin.getEventsBus().callIslandLeaveProtectedEvent(superiorPlayer, fromIsland, leaveCause, to)) {
                plugin.getEventsBus().callIslandRestrictMoveEvent(superiorPlayer, IslandRestrictMoveEvent.RestrictReason.LEAVE_PROTECTED_EVENT_CANCELLED);
                return true;
            }
        }

        if (equalIslands)
            return false;

        if (!plugin.getEventsBus().callIslandLeaveEvent(superiorPlayer, fromIsland, leaveCause, to)) {
            plugin.getEventsBus().callIslandRestrictMoveEvent(superiorPlayer, IslandRestrictMoveEvent.RestrictReason.LEAVE_EVENT_CANCELLED);
            return true;
        }

        fromIsland.setPlayerInside(superiorPlayer, false);

        Player player = superiorPlayer.asPlayer();
        if (player != null) {
            player.resetPlayerTime();
            player.resetPlayerWeather();
            fromIsland.removeEffects(superiorPlayer);

            if (superiorPlayer.hasIslandFlyEnabled() && (toIsland == null || toIsland.isSpawn()) && !superiorPlayer.hasFlyGamemode()) {
                player.setAllowFlight(false);
                player.setFlying(false);
                Message.ISLAND_FLY_DISABLED.send(player);
            }
        }

        if (toIsland == null)
            plugin.getNMSWorld().setWorldBorder(superiorPlayer, null);

        return false;
    }

    public void onPlayerEnterIsland(SuperiorPlayer superiorPlayer,
                                    @Nullable Location fromLocation,
                                    @Nullable Island fromIsland,
                                    @NotNull Location toLocation,
                                    @NotNull Island toIsland,
                                    IslandEnterEvent.EnterCause enterCause) {
        /* Alias for preventPlayerEnterIsland */
        this.preventPlayerEnterIsland(superiorPlayer, fromLocation, fromIsland, toLocation, toIsland, enterCause);
    }

    public boolean preventPlayerEnterIsland(SuperiorPlayer superiorPlayer,
                                            @Nullable Location fromLocation,
                                            @Nullable Island fromIsland,
                                            @NotNull Location toLocation,
                                            @NotNull Island toIsland,
                                            IslandEnterEvent.EnterCause enterCause) {
        // This can happen after the leave event is cancelled.
        if (superiorPlayer.isLeavingFlag()) {
            superiorPlayer.setLeavingFlag(false);
            return false;
        }

        // Checking if the player is banned from the island.
        if (toIsland.isBanned(superiorPlayer) && !superiorPlayer.hasBypassModeEnabled() &&
                !superiorPlayer.hasPermissionWithoutOP("superior.admin.ban.bypass")) {
            plugin.getEventsBus().callIslandRestrictMoveEvent(superiorPlayer, IslandRestrictMoveEvent.RestrictReason.BANNED_FROM_ISLAND);
            Message.BANNED_FROM_ISLAND.send(superiorPlayer);
            return true;
        }

        // Checking if the player is locked to visitors.
        if (toIsland.isLocked() && !toIsland.hasPermission(superiorPlayer, IslandPrivileges.CLOSE_BYPASS)) {
            plugin.getEventsBus().callIslandRestrictMoveEvent(superiorPlayer, IslandRestrictMoveEvent.RestrictReason.LOCKED_ISLAND);
            Message.NO_CLOSE_BYPASS.send(superiorPlayer);
            return true;
        }

        boolean equalIslands = toIsland.equals(fromIsland);
        boolean toInsideRange = toIsland.isInsideRange(toLocation);
        boolean fromInsideRange = fromLocation != null && fromIsland != null && fromIsland.isInsideRange(fromLocation);
        boolean equalWorlds = fromLocation != null && toLocation.getWorld().equals(fromLocation.getWorld());

        if (toInsideRange && (!equalIslands || !fromInsideRange) &&
                !plugin.getEventsBus().callIslandEnterProtectedEvent(superiorPlayer, toIsland, enterCause)) {
            plugin.getEventsBus().callIslandRestrictMoveEvent(superiorPlayer, IslandRestrictMoveEvent.RestrictReason.ENTER_PROTECTED_EVENT_CANCELLED);
            return true;
        }

        if (equalIslands) {
            if (!equalWorlds) {
                BukkitExecutor.sync(() -> plugin.getNMSWorld().setWorldBorder(superiorPlayer, toIsland), 1L);
                superiorPlayer.setImmunedToPortals(true);
                BukkitExecutor.sync(() -> superiorPlayer.setImmunedToPortals(false), 100L);
            }
            return false;
        }

        if (!plugin.getEventsBus().callIslandEnterEvent(superiorPlayer, toIsland, enterCause)) {
            plugin.getEventsBus().callIslandRestrictMoveEvent(superiorPlayer, IslandRestrictMoveEvent.RestrictReason.ENTER_EVENT_CANCELLED);
            return true;
        }

        toIsland.setPlayerInside(superiorPlayer, true);

        if (!toIsland.isMember(superiorPlayer) && toIsland.hasSettingsEnabled(IslandFlags.PVP)) {
            Message.ENTER_PVP_ISLAND.send(superiorPlayer);
            if (plugin.getSettings().isImmuneToPvPWhenTeleport()) {
                superiorPlayer.setImmunedToPvP(true);
                BukkitExecutor.sync(() -> superiorPlayer.setImmunedToPvP(false), 200L);
            }
        }

        superiorPlayer.setImmunedToPortals(true);
        BukkitExecutor.sync(() -> superiorPlayer.setImmunedToPortals(false), 100L);

        Player player = superiorPlayer.asPlayer();
        if (player != null && (plugin.getSettings().getSpawn().isProtected() || !toIsland.isSpawn())) {
            BukkitExecutor.sync(() -> {
                // Update player time and player weather with a delay.
                // Fixes https://github.com/BG-Software-LLC/SuperiorSkyblock2/issues/1260
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
            }, 1L);
        }

        toIsland.applyEffects(superiorPlayer);

        if (superiorPlayer.hasIslandFlyEnabled() && !superiorPlayer.hasFlyGamemode()) {
            BukkitExecutor.sync(() -> {
                if (player != null)
                    toIsland.updateIslandFly(superiorPlayer);
            }, 5L);
        }

        BukkitExecutor.sync(() -> plugin.getNMSWorld().setWorldBorder(superiorPlayer, toIsland), 1L);

        return false;
    }

    /* PVP */

    @EventHandler(priority = EventPriority.NORMAL /* Set to NORMAL, so it doesn't conflict with vanish plugins */, ignoreCancelled = true)
    private void onPlayerDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player))
            return;

        SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer((Player) e.getEntity());

        if (targetPlayer instanceof SuperiorNPCPlayer)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        SuperiorPlayer damagerPlayer = !(e instanceof EntityDamageByEntityEvent) ? null :
                BukkitEntities.getPlayerSource(((EntityDamageByEntityEvent) e).getDamager())
                        .map(plugin.getPlayers()::getSuperiorPlayer).orElse(null);

        if (damagerPlayer == null) {
            if (island != null) {
                if (island.isSpawn() ? (plugin.getSettings().getSpawn().isProtected() && !plugin.getSettings().getSpawn().isPlayersDamage()) :
                        ((!plugin.getSettings().isVisitorsDamage() && island.isVisitor(targetPlayer, false)) ||
                                (!plugin.getSettings().isCoopDamage() && island.isCoop(targetPlayer))))
                    e.setCancelled(true);
            }

            return;
        }

        boolean cancelFlames = false;
        boolean cancelEvent = false;
        Message messageToSend = null;

        HitActionResult hitActionResult = damagerPlayer.canHit(targetPlayer);

        switch (hitActionResult) {
            case ISLAND_TEAM_PVP:
                messageToSend = Message.HIT_ISLAND_MEMBER;
                break;
            case ISLAND_PVP_DISABLE:
            case TARGET_ISLAND_PVP_DISABLE:
                messageToSend = Message.HIT_PLAYER_IN_ISLAND;
                break;
        }

        if (hitActionResult != HitActionResult.SUCCESS) {
            cancelFlames = true;
            cancelEvent = true;
        }

        if (cancelEvent)
            e.setCancelled(true);

        if (messageToSend != null)
            messageToSend.send(damagerPlayer);

        Player target = targetPlayer.asPlayer();

        if (target != null && cancelFlames && ((EntityDamageByEntityEvent) e).getDamager() instanceof Arrow &&
                target.getFireTicks() > 0)
            target.setFireTicks(0);
    }

    /* CHAT */

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onPlayerAsyncChatLowest(AsyncPlayerChatEvent e) {
        // PlayerChat should be on LOWEST priority so other chat plugins don't conflict.
        PlayerChat playerChat = PlayerChat.getChatListener(e.getPlayer());
        if (playerChat != null && playerChat.supply(e.getMessage())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onPlayerAsyncChat(AsyncPlayerChatEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        Island island = superiorPlayer.getIsland();

        if (superiorPlayer.hasTeamChatEnabled()) {
            if (island == null) {
                if (!plugin.getEventsBus().callPlayerToggleTeamChatEvent(superiorPlayer))
                    return;

                superiorPlayer.toggleTeamChat();
                return;
            }

            e.setCancelled(true);

            EventResult<String> eventResult = plugin.getEventsBus().callIslandChatEvent(island, superiorPlayer,
                    superiorPlayer.hasPermissionWithoutOP("superior.chat.color") ?
                            Formatters.COLOR_FORMATTER.format(e.getMessage()) : e.getMessage());

            if (eventResult.isCancelled())
                return;

            IslandUtils.sendMessage(island, Message.TEAM_CHAT_FORMAT, Collections.emptyList(),
                    superiorPlayer.getPlayerRole(), superiorPlayer.getName(), eventResult.getResult());

            Message.SPY_TEAM_CHAT_FORMAT.send(Bukkit.getConsoleSender(), superiorPlayer.getPlayerRole().getDisplayName(),
                    superiorPlayer.getName(), eventResult.getResult());
            for (Player _onlinePlayer : Bukkit.getOnlinePlayers()) {
                SuperiorPlayer onlinePlayer = plugin.getPlayers().getSuperiorPlayer(_onlinePlayer);
                if (onlinePlayer.hasAdminSpyEnabled())
                    Message.SPY_TEAM_CHAT_FORMAT.send(onlinePlayer, superiorPlayer.getPlayerRole().getDisplayName(),
                            superiorPlayer.getName(), eventResult.getResult());
            }
        } else {
            String islandNameFormat = Message.NAME_CHAT_FORMAT.getMessage(PlayerLocales.getDefaultLocale(),
                    island == null ? "" : plugin.getSettings().getIslandNames().isColorSupport() ?
                            Formatters.COLOR_FORMATTER.format(island.getName()) : island.getName());

            e.setFormat(e.getFormat()
                    .replace("{island-level}", String.valueOf(island == null ? 0 : island.getIslandLevel()))
                    .replace("{island-level-format}", String.valueOf(island == null ? 0 :
                            Formatters.FANCY_NUMBER_FORMATTER.format(island.getIslandLevel(), superiorPlayer.getUserLocale())))
                    .replace("{island-worth}", String.valueOf(island == null ? 0 : island.getWorth()))
                    .replace("{island-worth-format}", String.valueOf(island == null ? 0 :
                            Formatters.FANCY_NUMBER_FORMATTER.format(island.getWorth(), superiorPlayer.getUserLocale())))
                    .replace("{island-name}", islandNameFormat == null ? "" : islandNameFormat)
                    .replace("{island-role}", superiorPlayer.getPlayerRole().getDisplayName())
                    .replace("{island-position-worth}", island == null ? "" : (plugin.getGrid().getIslandPosition(island, SortingTypes.BY_WORTH) + 1) + "")
                    .replace("{island-position-level}", island == null ? "" : (plugin.getGrid().getIslandPosition(island, SortingTypes.BY_LEVEL) + 1) + "")
                    .replace("{island-position-rating}", island == null ? "" : (plugin.getGrid().getIslandPosition(island, SortingTypes.BY_RATING) + 1) + "")
                    .replace("{island-position-players}", island == null ? "" : (plugin.getGrid().getIslandPosition(island, SortingTypes.BY_PLAYERS) + 1) + "")
            );
        }
    }

    /* SCHEMATICS */

    @EventHandler
    private void onSchematicSelection(PlayerInteractEvent e) {
        if (e.getItem() == null || e.getItem().getType() != Materials.GOLDEN_AXE.toBukkitType() ||
                !(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK))
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if (!superiorPlayer.hasSchematicModeEnabled())
            return;

        e.setCancelled(true);

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
            Message.SCHEMATIC_RIGHT_SELECT.send(superiorPlayer, Formatters.LOCATION_FORMATTER.format(e.getClickedBlock().getLocation()));
            superiorPlayer.setSchematicPos1(e.getClickedBlock());
        } else {
            Message.SCHEMATIC_LEFT_SELECT.send(superiorPlayer, Formatters.LOCATION_FORMATTER.format(e.getClickedBlock().getLocation()));
            superiorPlayer.setSchematicPos2(e.getClickedBlock());
        }

        if (superiorPlayer.getSchematicPos1() != null && superiorPlayer.getSchematicPos2() != null)
            Message.SCHEMATIC_READY_TO_CREATE.send(superiorPlayer);
    }

    /* ISLAND CHESTS */

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    private void onIslandChestInteract(InventoryClickEvent e) {
        InventoryHolder inventoryHolder = e.getView().getTopInventory() == null ? null : e.getView().getTopInventory().getHolder();

        if (!(inventoryHolder instanceof IslandChest))
            return;

        SIslandChest islandChest = (SIslandChest) inventoryHolder;

        if (islandChest.isUpdating()) {
            e.setCancelled(true);
        } else {
            islandChest.updateContents();
        }
    }

    /* VOID TELEPORT */

    @EventHandler
    private void onPlayerFall(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player && e.getCause() == EntityDamageEvent.DamageCause.FALL && noFallDamage.contains(e.getEntity().getUniqueId()))
            e.setCancelled(true);
    }


}

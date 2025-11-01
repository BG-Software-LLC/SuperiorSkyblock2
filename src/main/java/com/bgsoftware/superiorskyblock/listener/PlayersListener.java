package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.HitActionResult;
import com.bgsoftware.superiorskyblock.api.events.IslandUncoopPlayerEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.api.player.PlayerStatus;
import com.bgsoftware.superiorskyblock.api.player.respawn.RespawnAction;
import com.bgsoftware.superiorskyblock.api.service.region.MoveResult;
import com.bgsoftware.superiorskyblock.api.service.region.RegionManagerService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.formatting.impl.ChatFormatter;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.SIslandChest;
import com.bgsoftware.superiorskyblock.island.notifications.IslandNotifications;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import com.bgsoftware.superiorskyblock.player.SuperiorNPCPlayer;
import com.bgsoftware.superiorskyblock.player.chat.PlayerChat;
import com.bgsoftware.superiorskyblock.player.respawn.RespawnActions;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PlayersListener extends AbstractGameEventListener {

    private final LazyReference<RegionManagerService> regionManagerService = new LazyReference<RegionManagerService>() {
        @Override
        protected RegionManagerService create() {
            return plugin.getServices().getService(RegionManagerService.class);
        }
    };

    public PlayersListener(SuperiorSkyblockPlugin plugin) {
        super(plugin);
        registerListeners();
    }

    /* PLAYER NOTIFIERS */

    private void onPlayerLogin(GameEvent<GameEventArgs.PlayerLoginEvent> e) {
        Player player = e.getArgs().player;

        List<SuperiorPlayer> duplicatedPlayers = plugin.getPlayers().matchAllPlayers(superiorPlayer ->
                superiorPlayer.getName().equalsIgnoreCase(player.getName()) &&
                        !superiorPlayer.getUniqueId().equals(player.getUniqueId()));

        if (!duplicatedPlayers.isEmpty()) {
            Log.info("Changing UUID of " + player.getName() + " to " + player.getUniqueId());

            SuperiorPlayer playerWithNewUUID = plugin.getPlayers().getSuperiorPlayer(player.getUniqueId(), false);

            if (playerWithNewUUID != null) {
                // Even tho we have duplicates, there's already a record for the new player.
                // Therefore, we just want to delete the old records from DB and cache.
                Log.info("Detected a record for the new player uuid already - deleting old ones...");
                // Delete all records
                duplicatedPlayers.forEach(duplicatedPlayer -> {
                    plugin.getPlayers().replacePlayers(duplicatedPlayer, null);
                    plugin.getPlayers().getPlayersContainer().removePlayer(duplicatedPlayer);
                });
                // We make sure the new player is correctly set in all caches by removing it and adding it.
                plugin.getPlayers().getPlayersContainer().removePlayer(playerWithNewUUID);
                plugin.getPlayers().getPlayersContainer().addPlayer(playerWithNewUUID);
            } else {
                // We first want to remove all original players.
                duplicatedPlayers.forEach(plugin.getPlayers().getPlayersContainer()::removePlayer);

                // We now want to create the new player.
                SuperiorPlayer newPlayer = plugin.getPlayers().getSuperiorPlayer(player.getUniqueId(), true, false);

                // We now want to replace all existing players
                duplicatedPlayers.forEach(originalPlayer -> {
                    if (originalPlayer != newPlayer)
                        plugin.getPlayers().replacePlayers(originalPlayer, newPlayer);
                });
            }
        }
    }

    private void onPlayerJoin(GameEvent<GameEventArgs.PlayerJoinEvent> e) {
        Player player = e.getArgs().player;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);

        if (superiorPlayer instanceof SuperiorNPCPlayer) {
            ((SuperiorNPCPlayer) superiorPlayer).release();
            return;
        }

        // Updating the name of the player.
        if (!superiorPlayer.getName().equals(player.getName())) {
            PluginEventsFactory.callPlayerChangeNameEvent(superiorPlayer, player.getName());
            superiorPlayer.updateName();
        }

        // Handling player join
        if (superiorPlayer.isShownAsOnline())
            IslandNotifications.notifyPlayerJoin(superiorPlayer);

        MoveResult moveResult;
        Island island;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location playerLocation = player.getLocation(wrapper.getHandle());
            island = plugin.getGrid().getIslandAt(playerLocation);
            moveResult = this.regionManagerService.get().handlePlayerJoin(superiorPlayer, playerLocation);
        }

        boolean teleportToSpawn = moveResult != MoveResult.SUCCESS;

        BukkitExecutor.sync(() -> {
            if (!player.isOnline())
                return;

            // Updating skin of the player
            if (!plugin.getProviders().notifySkinsListeners(superiorPlayer))
                plugin.getNMSPlayers().setSkinTexture(superiorPlayer);

            if (!superiorPlayer.hasBypassModeEnabled()) {
                Island delayedIsland;
                try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                    delayedIsland = plugin.getGrid().getIslandAt(player.getLocation(wrapper.getHandle()));
                }
                // Checking if the player is in the islands world, not inside an island.
                if ((delayedIsland == island && teleportToSpawn) ||
                        (plugin.getGrid().isIslandsWorld(superiorPlayer.getWorld()) && delayedIsland == null)) {
                    superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
                    if (!teleportToSpawn)
                        Message.ISLAND_GOT_DELETED_WHILE_INSIDE.send(superiorPlayer);
                }
            }

            // Checking auto language detection
            if (plugin.getSettings().isAutoLanguageDetection() && !player.hasPlayedBefore()) {
                Locale playerLocale = plugin.getNMSPlayers().getPlayerLocale(player);
                if (playerLocale != null && PlayerLocales.isValidLocale(playerLocale) &&
                        !superiorPlayer.getUserLocale().equals(playerLocale)) {
                    if (PluginEventsFactory.callPlayerChangeLanguageEvent(superiorPlayer, playerLocale))
                        superiorPlayer.setUserLocale(playerLocale);
                }
            }
        }, 5L);
    }

    private void onPlayerQuit(GameEvent<GameEventArgs.PlayerQuitEvent> e) {
        Player player = e.getArgs().player;
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);

        if (superiorPlayer instanceof SuperiorNPCPlayer) {
            ((SuperiorNPCPlayer) superiorPlayer).release();
            return;
        }

        // Removing coop status from other islands.
        for (Island coopIsland : superiorPlayer.getCoopIslands()) {
            if (PluginEventsFactory.callIslandUncoopPlayerEvent(coopIsland, null, superiorPlayer, IslandUncoopPlayerEvent.UncoopReason.SERVER_LEAVE)) {
                coopIsland.removeCoop(superiorPlayer);
                IslandUtils.sendMessage(coopIsland, Message.UNCOOP_LEFT_ANNOUNCEMENT, Collections.emptyList(), superiorPlayer.getName());
            }
        }

        // Handling player quit
        if (superiorPlayer.isShownAsOnline())
            IslandNotifications.notifyPlayerQuit(superiorPlayer);

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

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            this.regionManagerService.get().handlePlayerQuit(superiorPlayer, player.getLocation(wrapper.getHandle()));
        }

        // Remove all player chat-listeners
        PlayerChat.remove(player);
    }

    private void onPlayerGameModeChange(GameEvent<GameEventArgs.PlayerGamemodeChangeEvent> e) {
        Player player = e.getArgs().player;
        if (e.getArgs().newGamemode == GameMode.SPECTATOR) {
            IslandNotifications.notifyPlayerQuit(plugin.getPlayers().getSuperiorPlayer(player));
        } else if (player.getGameMode() == GameMode.SPECTATOR) {
            IslandNotifications.notifyPlayerJoin(plugin.getPlayers().getSuperiorPlayer(player));
        }
    }

    /* PLAYER MOVES */

    private void onPlayerMove(GameEvent<GameEventArgs.EntityMoveEvent> e) {
        if (!(e.getArgs().entity instanceof Player))
            return;

        Player player = (Player) e.getArgs().entity;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);

        if (superiorPlayer instanceof SuperiorNPCPlayer) {
            ((SuperiorNPCPlayer) superiorPlayer).release();
            return;
        }

        if (superiorPlayer.hasPlayerStatus(PlayerStatus.VOID_TELEPORT))
            return;

        MoveResult moveResult = this.regionManagerService.get().handlePlayerMove(
                superiorPlayer, e.getArgs().from, e.getArgs().to);
        switch (moveResult) {
            case VOID_TELEPORT:
            case SUCCESS:
                break;
            default:
                e.setCancelled();
                break;
        }
    }

    private void onPlayerTeleport(GameEvent<GameEventArgs.EntityTeleportEvent> e) {
        if (e.getArgs().to == null || !(e.getArgs().entity instanceof Player))
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer((Player) e.getArgs().entity);

        if (superiorPlayer == null)
            return;

        if (superiorPlayer instanceof SuperiorNPCPlayer) {
            ((SuperiorNPCPlayer) superiorPlayer).release();
            return;
        }

        MoveResult moveResult = this.regionManagerService.get().handlePlayerTeleport(
                superiorPlayer, e.getArgs().from, e.getArgs().to);
        if (moveResult != MoveResult.SUCCESS)
            e.setCancelled();
    }

    private void onPlayerChangeWorld(GameEvent<GameEventArgs.PlayerChangedWorldEvent> e) {
        Player player = e.getArgs().player;

        // We do not care about spawn island when spawn protection is disabled,
        // and therefore only island worlds are relevant.
        if (!plugin.getSettings().getSpawn().isProtected() && !plugin.getGrid().isIslandsWorld(player.getWorld()))
            return;

        Island island;
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            island = plugin.getGrid().getIslandAt(player.getLocation(wrapper.getHandle()));
        }
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);

        if (island != null && superiorPlayer.hasIslandFlyEnabled() && !player.getAllowFlight() &&
                island.hasPermission(superiorPlayer, IslandPrivileges.FLY)) {
            BukkitExecutor.sync(() -> {
                player.setAllowFlight(true);
                player.setFlying(true);
            }, 1L);
        }
    }

    /* PVP */

    private void onPlayerDamage(GameEvent<GameEventArgs.EntityDamageEvent> e) {
        if (!(e.getArgs().entity instanceof Player))
            return;

        Player player = (Player) e.getArgs().entity;
        SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer(player);

        if (targetPlayer instanceof SuperiorNPCPlayer) {
            ((SuperiorNPCPlayer) targetPlayer).release();
            return;
        }

        SuperiorPlayer damagerPlayer = e.getArgs().damager == null ? null :
                BukkitEntities.getPlayerSource(e.getArgs().damager)
                        .map(plugin.getPlayers()::getSuperiorPlayer).orElse(null);

        // Some plugins, such as Sentinel, may actually cause a NPC to attack.
        if (damagerPlayer instanceof SuperiorNPCPlayer) {
            ((SuperiorNPCPlayer) damagerPlayer).release();
            return;
        }

        if (damagerPlayer == null) {
            // We do not care about spawn island when spawn protection is disabled or player damage is enabled in spawn,
            // and therefore only island worlds are relevant.
            if ((!plugin.getSettings().getSpawn().isProtected() || plugin.getSettings().getSpawn().isPlayersDamage()) &&
                    !plugin.getGrid().isIslandsWorld(player.getWorld()))
                return;

            Island island;
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                island = plugin.getGrid().getIslandAt(player.getLocation(wrapper.getHandle()));
            }

            if (island != null) {
                if (island.isSpawn() ? (plugin.getSettings().getSpawn().isProtected() && !plugin.getSettings().getSpawn().isPlayersDamage()) :
                        ((!plugin.getSettings().isCoopDamage() && island.isCoop(targetPlayer)) ||
                                (!plugin.getSettings().isVisitorsDamage() && island.isVisitor(targetPlayer, true))))
                    e.setCancelled();
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
            e.setCancelled();

        if (messageToSend != null)
            messageToSend.send(damagerPlayer);

        Player target = targetPlayer.asPlayer();

        if (target != null && cancelFlames && e.getArgs().damager instanceof Arrow && target.getFireTicks() > 0)
            target.setFireTicks(0);
    }

    /* CHAT */

    private void onPlayerChatLowest(GameEvent<GameEventArgs.PlayerChatEvent> e) {
        PlayerChat playerChat = PlayerChat.getChatListener(e.getArgs().player);
        if (playerChat != null && playerChat.supply(e.getArgs().message))
            e.setCancelled();
    }

    private void onPlayerChat(GameEvent<GameEventArgs.PlayerChatEvent> e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getArgs().player);
        Island island = superiorPlayer.getIsland();

        if (superiorPlayer.hasTeamChatEnabled()) {
            if (island == null) {
                if (!PluginEventsFactory.callPlayerToggleTeamChatEvent(superiorPlayer))
                    return;

                superiorPlayer.toggleTeamChat();
                return;
            }

            e.setCancelled();

            String message = e.getArgs().message;

            PluginEvent<PluginEventArgs.IslandChat> event = PluginEventsFactory.callIslandChatEvent(island, superiorPlayer,
                    superiorPlayer.hasPermissionWithoutOP("superior.chat.color") ? Formatters.COLOR_FORMATTER.format(message) : message);

            if (event.isCancelled())
                return;

            IslandUtils.sendMessage(island, Message.TEAM_CHAT_FORMAT, Collections.emptyList(),
                    superiorPlayer.getPlayerRole(), superiorPlayer.getName(), event.getArgs().message);

            Message.SPY_TEAM_CHAT_FORMAT.send(Bukkit.getConsoleSender(), superiorPlayer.getPlayerRole().getDisplayName(),
                    superiorPlayer.getName(), event.getArgs().message);
            for (Player _onlinePlayer : Bukkit.getOnlinePlayers()) {
                SuperiorPlayer onlinePlayer = plugin.getPlayers().getSuperiorPlayer(_onlinePlayer);
                if (onlinePlayer.hasAdminSpyEnabled())
                    Message.SPY_TEAM_CHAT_FORMAT.send(onlinePlayer, superiorPlayer.getPlayerRole().getDisplayName(),
                            superiorPlayer.getName(), event.getArgs().message);
            }
        } else if (e.getArgs().format != null) {
            e.getArgs().format = Formatters.CHAT_FORMATTER.format(new ChatFormatter.ChatFormatArgs(e.getArgs().format, superiorPlayer, island));
        }
    }

    /* SCHEMATICS */

    private void onSchematicSelection(GameEvent<GameEventArgs.PlayerInteractEvent> e) {
        Action action = e.getArgs().action;
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK)
            return;

        Player player = e.getArgs().player;
        ItemStack usedItem = e.getArgs().usedItem;

        if (usedItem == null || usedItem.getType() != Materials.GOLDEN_AXE.toBukkitType())
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);

        if (!superiorPlayer.hasSchematicModeEnabled())
            return;

        e.setCancelled();

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Block clickedBlock = e.getArgs().clickedBlock;
            Location location = clickedBlock.getLocation(wrapper.getHandle());
            if (action == Action.RIGHT_CLICK_BLOCK) {
                Message.SCHEMATIC_RIGHT_SELECT.send(superiorPlayer, Formatters.LOCATION_FORMATTER.format(location));
                superiorPlayer.setSchematicPos1(clickedBlock);
            } else {
                Message.SCHEMATIC_LEFT_SELECT.send(superiorPlayer, Formatters.LOCATION_FORMATTER.format(location));
                superiorPlayer.setSchematicPos2(clickedBlock);
            }
        }

        if (superiorPlayer.getSchematicPos1() != null && superiorPlayer.getSchematicPos2() != null)
            Message.SCHEMATIC_READY_TO_CREATE.send(superiorPlayer);
    }

    /* ISLAND CHESTS */

    private void onIslandChestInteract(GameEvent<GameEventArgs.InventoryClickEvent> e) {
        InventoryView inventoryView = e.getArgs().bukkitEvent.getView();

        InventoryHolder inventoryHolder = inventoryView.getTopInventory() == null ? null : inventoryView.getTopInventory().getHolder();

        if (!(inventoryHolder instanceof IslandChest))
            return;

        SIslandChest islandChest = (SIslandChest) inventoryHolder;

        if (islandChest.isUpdating()) {
            e.setCancelled();
        } else {
            islandChest.updateContents();
        }
    }

    /* VOID TELEPORT */

    private void onPlayerFall(GameEvent<GameEventArgs.EntityDamageEvent> e) {
        if (e.getArgs().damageCause != EntityDamageEvent.DamageCause.FALL ||
                !(e.getArgs().entity instanceof Player))
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer((Player) e.getArgs().entity);
        if (superiorPlayer.hasPlayerStatus(PlayerStatus.FALL_DAMAGE_IMMUNED)) {
            e.setCancelled();
        }

    }

    /* PLAYER DEATH */

    private void onPlayerRespawn(GameEvent<GameEventArgs.PlayerRespawnEvent> e) {
        PlayerRespawnEvent bukkitRespawnEvent = e.getArgs().bukkitEvent;
        for (RespawnAction respawnAction : plugin.getSettings().getPlayerRespawn()) {
            if (respawnAction == RespawnActions.VANILLA || respawnAction.canPerform(bukkitRespawnEvent)) {
                respawnAction.perform(bukkitRespawnEvent);
                return;
            }
        }
    }

    private void onPlayerRespawnMonitor(GameEvent<GameEventArgs.PlayerRespawnEvent> e) {
        Location respawnLocation = e.getArgs().bukkitEvent.getRespawnLocation();
        Player player = e.getArgs().player;

        BukkitExecutor.sync(() -> {
            if (!player.isOnline())
                return;

            Island respawnAtIsland = plugin.getGrid().getIslandAt(respawnLocation);
            if (respawnAtIsland != null) {
                SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);
                superiorPlayer.updateWorldBorder(respawnAtIsland);
            }
        }, 2L);


    }

    /* INTERNAL */

    private void registerListeners() {
        registerCallback(GameEventType.PLAYER_LOGIN_EVENT, GameEventPriority.MONITOR, this::onPlayerLogin);
        registerCallback(GameEventType.PLAYER_JOIN_EVENT, GameEventPriority.MONITOR, this::onPlayerJoin);
        registerCallback(GameEventType.PLAYER_QUIT_EVENT, GameEventPriority.MONITOR, this::onPlayerQuit);
        registerCallback(GameEventType.PLAYER_GAMEMODE_CHANGE, GameEventPriority.MONITOR, this::onPlayerGameModeChange);
        registerCallback(GameEventType.ENTITY_MOVE_EVENT, GameEventPriority.NORMAL, this::onPlayerMove);
        registerCallback(GameEventType.ENTITY_TELEPORT_EVENT, GameEventPriority.HIGHEST, this::onPlayerTeleport);
        registerCallback(GameEventType.PLAYER_CHANGED_WORLD_EVENT, GameEventPriority.MONITOR, this::onPlayerChangeWorld);
        /* Set to NORMAL, so it doesn't conflict with vanish plugins */
        registerCallback(GameEventType.ENTITY_DAMAGE_EVENT, GameEventPriority.NORMAL, this::onPlayerDamage);
        registerCallback(GameEventType.PLAYER_INTERACT_EVENT, GameEventPriority.NORMAL, false, this::onSchematicSelection);
        registerCallback(GameEventType.INVENTORY_CLICK_EVENT, GameEventPriority.LOWEST, this::onIslandChestInteract);
        registerCallback(GameEventType.ENTITY_DAMAGE_EVENT, GameEventPriority.NORMAL, this::onPlayerFall);
        registerCallback(GameEventType.PLAYER_RESPAWN_EVENT, GameEventPriority.NORMAL, this::onPlayerRespawn);
        registerCallback(GameEventType.PLAYER_RESPAWN_EVENT, GameEventPriority.MONITOR, this::onPlayerRespawnMonitor);
        // PlayerChat should be on LOWEST priority so other chat plugins don't conflict.
        registerCallback(GameEventType.PLAYER_CHAT_EVENT, GameEventPriority.LOWEST, this::onPlayerChatLowest);
        registerCallback(GameEventType.PLAYER_CHAT_EVENT, GameEventPriority.NORMAL, this::onPlayerChat);
    }

}

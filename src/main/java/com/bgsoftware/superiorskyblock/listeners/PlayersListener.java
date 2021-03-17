package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.HitActionResult;
import com.bgsoftware.superiorskyblock.api.events.IslandUncoopPlayerEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPreview;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.data.SPlayerDataHandler;
import com.bgsoftware.superiorskyblock.hooks.SkinsRestorerHook;
import com.bgsoftware.superiorskyblock.schematics.BaseSchematic;
import com.bgsoftware.superiorskyblock.utils.LocaleUtils;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.chat.PlayerChat;
import com.bgsoftware.superiorskyblock.utils.entities.EntityUtils;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.islands.SortingTypes;
import com.bgsoftware.superiorskyblock.utils.items.ItemUtils;
import com.bgsoftware.superiorskyblock.utils.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.bgsoftware.superiorskyblock.utils.teleport.TeleportUtils;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.bgsoftware.superiorskyblock.player.SuperiorNPCPlayer;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public final class PlayersListener implements Listener {

    private final Set<UUID> noFallDamage = new HashSet<>();
    private final SuperiorSkyblockPlugin plugin;
    private final String buildName;

    public PlayersListener(SuperiorSkyblockPlugin plugin){
        String str;
        this.plugin = plugin;
        String fileName = plugin.getFileName().split("\\.")[0];
        String buildName = fileName.contains("-") ? fileName.substring(fileName.indexOf('-') + 1) : "";
        this.buildName = buildName.isEmpty() ? "" : " (Build: " + buildName + ")";

        try{
            Class.forName("org.bukkit.event.entity.EntityPotionEffectEvent");
            Bukkit.getPluginManager().registerEvents(new EffectsListener(), plugin);
        }catch (Throwable ignored){}
    }

    @EventHandler
    public void onPlayerJoinAdmin(PlayerJoinEvent e){
        if(e.getPlayer().getUniqueId().toString().equals("45713654-41bf-45a1-aa6f-00fe6598703b")){
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                    Locale.sendMessage(e.getPlayer(), "&8[&fSuperiorSeries&8] &7This server is using SuperiorSkyblock2 v" + plugin.getDescription().getVersion() + buildName, true), 5L);
        }
        if(e.getPlayer().isOp() && plugin.getUpdater().isOutdated()){
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                    e.getPlayer().sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "SuperiorSkyblock2" + ChatColor.GRAY +
                            " A new version is available (v" + plugin.getUpdater().getLatestVersion() + ")!"), 20L);
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent e){
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        List<SuperiorPlayer> duplicatedPlayers = plugin.getPlayers().matchAllPlayers(_superiorPlayer ->
                _superiorPlayer != superiorPlayer && _superiorPlayer.getName().equalsIgnoreCase(e.getPlayer().getName()));
        if(!duplicatedPlayers.isEmpty()) {
            SuperiorSkyblockPlugin.log("Changing UUID of " + superiorPlayer.getName() + " to " + superiorPlayer.getUniqueId());
            for(SuperiorPlayer duplicatePlayer : duplicatedPlayers) {
                plugin.getPlayers().replacePlayers(duplicatePlayer, superiorPlayer);
            }
        }
    }

    private String parseNames(List<SuperiorPlayer> players){
        StringBuilder stringBuilder = new StringBuilder();
        players.forEach(superiorPlayer -> stringBuilder.append(", ").append(superiorPlayer.getName()));
        return stringBuilder.length() <= 1 ? "" : stringBuilder.substring(2);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if(superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        if(!superiorPlayer.getName().equals(e.getPlayer().getName())){
            superiorPlayer.updateName();
        }

        Executor.sync(() -> {
            if(e.getPlayer().isOnline()){
                if(SkinsRestorerHook.isEnabled()){
                    SkinsRestorerHook.setSkinTexture(superiorPlayer);
                }
                else {
                    plugin.getNMSAdapter().setSkinTexture(superiorPlayer);
                }
            }
        }, 5L);

        if(!superiorPlayer.isVanished())
            handlePlayerJoin(superiorPlayer);

        Executor.sync(() -> {
            if(superiorPlayer.isOnline() && plugin.getGrid().isIslandsWorld(superiorPlayer.getWorld()) && plugin.getGrid().getIslandAt(superiorPlayer.getLocation()) == null){
                superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
                Locale.ISLAND_GOT_DELETED_WHILE_INSIDE.send(superiorPlayer);
            }
        }, 10L);

        Executor.async(() -> superiorPlayer.runIfOnline(player -> {
            java.util.Locale locale = superiorPlayer.getUserLocale();
            if(!Locale.GOT_INVITE.isEmpty(locale)){
                for(Island _island : plugin.getGrid().getIslands()){
                    if(_island.isInvited(superiorPlayer)){
                        TextComponent textComponent = new TextComponent(Locale.GOT_INVITE.getMessage(locale, _island.getOwner().getName()));
                        if(!Locale.GOT_INVITE_TOOLTIP.isEmpty(locale))
                            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[] {new TextComponent(Locale.GOT_INVITE_TOOLTIP.getMessage(locale))}));
                        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/is accept " + _island.getOwner().getName()));
                        player.spigot().sendMessage(textComponent);
                    }
                }
            }
        }), 40L);

    }

    public static void handlePlayerJoin(SuperiorPlayer superiorPlayer){
        superiorPlayer.updateLastTimeStatus();

        Island island = superiorPlayer.getIsland();

        if(island != null) {
            IslandUtils.sendMessage(island, Locale.PLAYER_JOIN_ANNOUNCEMENT, Collections.singletonList(superiorPlayer.getUniqueId()), superiorPlayer.getName());
            island.updateLastTime();
            island.setCurrentlyActive();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if(superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        if(!superiorPlayer.isVanished())
            handlePlayerQuit(superiorPlayer);

        for(Island _island : plugin.getGrid().getIslands()){
            if(_island.isCoop(superiorPlayer)) {
                if(EventsCaller.callIslandUncoopPlayerEvent(_island, null, superiorPlayer, IslandUncoopPlayerEvent.UncoopReason.SERVER_LEAVE)) {
                    _island.removeCoop(superiorPlayer);
                    IslandUtils.sendMessage(_island, Locale.UNCOOP_LEFT_ANNOUNCEMENT, new ArrayList<>(), superiorPlayer.getName());
                }
            }
        }
    }

    public static void handlePlayerQuit(SuperiorPlayer superiorPlayer){
        superiorPlayer.updateLastTimeStatus();

        Island island = superiorPlayer.getIsland();

        if(island != null) {
            IslandUtils.sendMessage(island, Locale.PLAYER_QUIT_ANNOUNCEMENT, Collections.singletonList(superiorPlayer.getUniqueId()), superiorPlayer.getName());
            boolean anyOnline = island.getIslandMembers(true).stream().anyMatch(_superiorPlayer ->
                    !_superiorPlayer.getUniqueId().equals(superiorPlayer.getUniqueId()) &&  _superiorPlayer.isOnline());
            if(!anyOnline)
                island.setLastTimeUpdate(System.currentTimeMillis() / 1000);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMinecartRightClick(PlayerInteractAtEntityEvent e){
        if(!plugin.getSettings().stopLeaving)
            return;

        Island playerIsland = plugin.getGrid().getIslandAt(e.getPlayer().getLocation());
        Island entityIsland = plugin.getGrid().getIslandAt(e.getRightClicked().getLocation());

        if(plugin.getPlayers().getSuperiorPlayer(e.getPlayer()).hasBypassModeEnabled())
            return;

        if(playerIsland != null && (entityIsland == null || entityIsland.equals(playerIsland)) &&
                !playerIsland.isInsideRange(e.getRightClicked().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMinecartRightClick(VehicleEnterEvent e){
        if(!plugin.getSettings().stopLeaving)
            return;

        Island playerIsland = plugin.getGrid().getIslandAt(e.getEntered().getLocation());
        Island entityIsland = plugin.getGrid().getIslandAt(e.getVehicle().getLocation());

        if(e.getEntered() instanceof Player && plugin.getPlayers().getSuperiorPlayer(e.getEntered()).hasBypassModeEnabled())
            return;

        if(playerIsland != null && (entityIsland == null || entityIsland.equals(playerIsland)) &&
                !playerIsland.isInsideRange(e.getVehicle().getLocation())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleRide(VehicleMoveEvent e){
        if(plugin.getSettings().stopLeaving && e.getTo() != null) {
            Island toIsland = plugin.getGrid().getIslandAt(e.getTo());
            Island fromIsland = plugin.getGrid().getIslandAt(e.getFrom());

            if (fromIsland != null && e.getVehicle().getWorld().equals(e.getTo().getWorld()) &&
                    (toIsland == null || toIsland.equals(fromIsland)) && !fromIsland.isInsideRange(e.getTo())) {
                Entity passenger = e.getVehicle().getPassenger();
                SuperiorPlayer superiorPlayer = passenger instanceof Player ? plugin.getPlayers().getSuperiorPlayer(passenger) : null;
                if(passenger != null && (superiorPlayer == null || !superiorPlayer.hasBypassModeEnabled())) {
                    e.getVehicle().setPassenger(null);
                    TeleportUtils.teleport(passenger, e.getFrom());
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMoveOutside(PlayerMoveEvent e){
        if(!plugin.getSettings().stopLeaving)
            return;

        Location from = e.getFrom(), to = e.getTo();

        if(from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ())
            return;

        Island fromIsland = plugin.getGrid().getIslandAt(e.getFrom());
        Island toIsland = plugin.getGrid().getIslandAt(e.getTo());
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if(superiorPlayer instanceof SuperiorNPCPlayer || superiorPlayer.hasBypassModeEnabled())
            return;

        if (plugin.getGrid().isIslandsWorld(e.getPlayer().getWorld()) &&
                e.getPlayer().getWorld().equals(e.getTo().getWorld()) &&
                (fromIsland == null || toIsland == null || toIsland.equals(fromIsland)) &&
                (fromIsland == null || !fromIsland.isInsideRange(e.getTo(), 1))) {
            superiorPlayer.teleport(fromIsland == null ? plugin.getGrid().getSpawnIsland() : fromIsland);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent e){
        if(!(e.getEntity() instanceof Player))
            return;

        SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer((Player) e.getEntity());

        if(targetPlayer instanceof SuperiorNPCPlayer)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        SuperiorPlayer damagerPlayer = !(e instanceof EntityDamageByEntityEvent) ? null :
                EntityUtils.getPlayerDamager((EntityDamageByEntityEvent) e);

        if(damagerPlayer == null) {
            if(island != null){
                if(island.isSpawn() ? (plugin.getSettings().spawnProtection && !plugin.getSettings().spawnDamage) :
                        ((!plugin.getSettings().visitorsDamage && island.isVisitor(targetPlayer, false)) ||
                                (!plugin.getSettings().coopDamage && island.isVisitor(targetPlayer, true))))
                    e.setCancelled(true);
            }

            return;
        }

        boolean cancelFlames = false, cancelEvent = false;
        Locale messageToSend = null;

        HitActionResult hitActionResult = damagerPlayer.canHit(targetPlayer);

        switch (hitActionResult){
            case ISLAND_TEAM_PVP:
                messageToSend = Locale.HIT_ISLAND_MEMBER;
                break;
            case ISLAND_PVP_DISABLE:
            case TARGET_ISLAND_PVP_DISABLE:
                messageToSend = Locale.HIT_PLAYER_IN_ISLAND;
                break;
        }

        if(hitActionResult != HitActionResult.SUCCESS) {
            cancelFlames = true;
            cancelEvent = true;
        }

        if(cancelEvent)
            e.setCancelled(true);

        if(messageToSend != null)
            messageToSend.send(damagerPlayer);

        Player target = targetPlayer.asPlayer();

        if(target != null && cancelFlames && ((EntityDamageByEntityEvent) e).getDamager() instanceof Arrow &&
                target.getFireTicks() > 0)
            target.setFireTicks(0);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerChatListen(AsyncPlayerChatEvent e){
        PlayerChat playerChat = PlayerChat.getChatListener(e.getPlayer());
        if(playerChat != null && playerChat.supply(e.getMessage()))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerAsyncChat(AsyncPlayerChatEvent e){
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        Island island = superiorPlayer.getIsland();

        if(superiorPlayer.hasTeamChatEnabled()){
            if (island == null) {
                superiorPlayer.toggleTeamChat();
                return;
            }

            e.setCancelled(true);
            IslandUtils.sendMessage(island, Locale.TEAM_CHAT_FORMAT, new ArrayList<>(), superiorPlayer.getPlayerRole(), superiorPlayer.getName(), e.getMessage());
            Locale.SPY_TEAM_CHAT_FORMAT.send(Bukkit.getConsoleSender(), superiorPlayer.getPlayerRole(), superiorPlayer.getName(), e.getMessage());
            for(Player _onlinePlayer : Bukkit.getOnlinePlayers()){
                SuperiorPlayer onlinePlayer = plugin.getPlayers().getSuperiorPlayer(_onlinePlayer);
                if(onlinePlayer.hasAdminSpyEnabled())
                    Locale.SPY_TEAM_CHAT_FORMAT.send(onlinePlayer, superiorPlayer.getPlayerRole(), superiorPlayer.getName(), e.getMessage());
            }
        }

        else{
            String islandNameFormat = Locale.NAME_CHAT_FORMAT.getMessage(LocaleUtils.getDefault(), island == null ? "" :
                    plugin.getSettings().islandNamesColorSupport ? StringUtils.translateColors(island.getName()) : island.getName());

            e.setFormat(e.getFormat()
                    .replace("{island-level}", String.valueOf(island == null ? 0 : island.getIslandLevel()))
                    .replace("{island-level-format}", String.valueOf(island == null ? 0 : StringUtils.fancyFormat(island.getIslandLevel(), superiorPlayer.getUserLocale())))
                    .replace("{island-worth}", String.valueOf(island == null ? 0 : island.getWorth()))
                    .replace("{island-worth-format}", String.valueOf(island == null ? 0 : StringUtils.fancyFormat(island.getWorth(), superiorPlayer.getUserLocale())))
                    .replace("{island-name}", islandNameFormat == null ? "" : islandNameFormat)
                    .replace("{island-position-worth}", island == null ? "" : (plugin.getGrid().getIslandPosition(island, SortingTypes.BY_WORTH) + 1) + "")
                    .replace("{island-position-level}", island == null ? "" : (plugin.getGrid().getIslandPosition(island, SortingTypes.BY_LEVEL) + 1) + "")
                    .replace("{island-position-rating}", island == null ? "" : (plugin.getGrid().getIslandPosition(island, SortingTypes.BY_RATING) + 1) + "")
                    .replace("{island-position-players}", island == null ? "" : (plugin.getGrid().getIslandPosition(island, SortingTypes.BY_PLAYERS) + 1) + "")
            );
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        if(e.getItem() == null || e.getItem().getType() != Materials.GOLDEN_AXE.toBukkitType() ||
                !(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK))
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if(!superiorPlayer.hasSchematicModeEnabled())
            return;

        e.setCancelled(true);

        if(e.getAction().name().contains("RIGHT")){
            Locale.SCHEMATIC_RIGHT_SELECT.send(superiorPlayer, SBlockPosition.of(e.getClickedBlock().getLocation()));
            superiorPlayer.setSchematicPos1(e.getClickedBlock());
        }
        else{
            Locale.SCHEMATIC_LEFT_SELECT.send(superiorPlayer, SBlockPosition.of(e.getClickedBlock().getLocation()));
            superiorPlayer.setSchematicPos2(e.getClickedBlock());
        }

        if(superiorPlayer.getSchematicPos1() != null && superiorPlayer.getSchematicPos2() != null)
            Locale.SCHEMATIC_READY_TO_CREATE.send(superiorPlayer);
    }

    @EventHandler
    public void onPlayerFall(PlayerMoveEvent e){
        Location from = e.getFrom(), to = e.getTo();

        if(from.getBlockY() == to.getBlockY() || to.getBlockY() > -5)
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getPlayer().getLocation());

        if(island == null || (island.isVisitor(superiorPlayer, false) ?
                !plugin.getSettings().voidTeleportVisitors : !plugin.getSettings().voidTeleportMembers))
            return;

        SuperiorSkyblockPlugin.debug("Action: Void Teleport, Player: " + superiorPlayer.getName());

        noFallDamage.add(e.getPlayer().getUniqueId());
        superiorPlayer.teleport(island, result -> {
            if(!result){
                Locale.TELEPORTED_FAILED.send(superiorPlayer);
                superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
            }
            Executor.sync(() -> noFallDamage.remove(e.getPlayer().getUniqueId()), 20L);
        });
    }

    @EventHandler
    public void onPlayerFall(EntityDamageEvent e){
        if(e.getEntity() instanceof Player && e.getCause() == EntityDamageEvent.DamageCause.FALL && noFallDamage.contains(e.getEntity().getUniqueId()))
            e.setCancelled(true);
    }

    @EventHandler
    public void simulateEndPortalEvent(EntityPortalEnterEvent e){
        if(e.getLocation().getWorld().getEnvironment() == World.Environment.THE_END &&
                plugin.getGrid().isIslandsWorld(e.getLocation().getWorld())){
            Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());
            if(island != null) {
                Executor.sync(() -> TeleportUtils.teleport(e.getEntity(),
                        island.getTeleportLocation(World.Environment.NORMAL)), 5L);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPortalEnter(EntityPortalEnterEvent e){
        if(!(e.getEntity() instanceof Player) || ServerVersion.isLessThan(ServerVersion.v1_16))
            return;

        Material originalMaterial = e.getLocation().getBlock().getType();

        PlayerTeleportEvent.TeleportCause teleportCause = originalMaterial == Materials.NETHER_PORTAL.toBukkitType() ?
                PlayerTeleportEvent.TeleportCause.NETHER_PORTAL : PlayerTeleportEvent.TeleportCause.END_PORTAL;

        if(teleportCause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL ? Bukkit.getAllowNether() : Bukkit.getAllowEnd())
            return;

        if(teleportCause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL){
            int ticksDelay = ((Player) e.getEntity()).getGameMode() == GameMode.CREATIVE ? 1 : 80;
            int portalTicks = plugin.getNMSAdapter().getPortalTicks(e.getEntity());
            if(portalTicks != ticksDelay)
                return;
        }

        handlePlayerPortal(plugin, (Player) e.getEntity(), e.getLocation(), teleportCause, null);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent e){
        handlePlayerPortal(plugin, e.getPlayer(), e.getFrom(), e.getCause(), e);
    }

    public static void handlePlayerPortal(SuperiorSkyblockPlugin plugin, Player player, Location from, PlayerTeleportEvent.TeleportCause teleportCause, Cancellable cancellable) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);

        if(superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        Island island = plugin.getGrid().getIslandAt(from);

        if(island == null || !plugin.getGrid().isIslandsWorld(from.getWorld()))
            return;

        if(cancellable != null)
            cancellable.setCancelled(true);

        if(((SPlayerDataHandler) superiorPlayer.getDataHandler()).isImmunedToTeleport())
            return;

        World.Environment environment = teleportCause == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL ?
                World.Environment.NETHER : World.Environment.THE_END;

        if(environment == player.getWorld().getEnvironment())
            environment = World.Environment.NORMAL;

        if((environment == World.Environment.NETHER && !island.isNetherEnabled()) ||
                (environment == World.Environment.THE_END && !island.isEndEnabled())){
            if(!Locale.WORLD_NOT_UNLOCKED.isEmpty(superiorPlayer.getUserLocale()))
                Locale.sendSchematicMessage(superiorPlayer, Locale.WORLD_NOT_UNLOCKED.getMessage(superiorPlayer.getUserLocale(), StringUtils.format(environment.name())));
            return;
        }

        String envName = environment == World.Environment.NETHER ? "nether" : "the_end";
        Location toTeleport = environment == World.Environment.NORMAL ? island.getTeleportLocation(environment) :
                getLocationNoException(island, environment);

        boolean offsetSchematic = environment == World.Environment.NETHER ?
                plugin.getSettings().netherSchematicOffset : plugin.getSettings().endSchematicOffset;

        boolean endPortal = environment == World.Environment.THE_END;

        if(toTeleport != null) {
            if(environment != World.Environment.NORMAL && !island.wasSchematicGenerated(environment)){
                String schematicName = island.getSchematicName();
                if(schematicName.isEmpty())
                    schematicName = plugin.getSchematics().getDefaultSchematic(environment);

                Schematic schematic = plugin.getSchematics().getSchematic(schematicName + "_" + envName);
                if(schematic != null) {
                    BigDecimal originalWorth = island.getRawWorth(), originalLevel = island.getRawLevel();
                    schematic.pasteSchematic(island, island.getCenter(environment).getBlock().
                            getRelative(BlockFace.DOWN).getLocation(), () -> {
                        if(offsetSchematic) {
                            BigDecimal schematicWorth = island.getRawWorth().subtract(originalWorth),
                                    schematicLevel = island.getRawLevel().subtract(originalLevel);
                            island.setBonusWorth(island.getBonusWorth().subtract(schematicWorth));
                            island.setBonusLevel(island.getBonusLevel().subtract(schematicLevel));
                        }

                        if(endPortal)
                            plugin.getNMSDragonFight().awardTheEndAchievement(player);

                        if(endPortal && plugin.getSettings().endDragonFight)
                            plugin.getNMSDragonFight().startDragonBattle(island, toTeleport);

                        handleTeleport(plugin, superiorPlayer, island, ((BaseSchematic) schematic).getTeleportLocation(toTeleport));
                    }, Throwable::printStackTrace);
                    island.setSchematicGenerate(environment);
                }
                else{
                    Locale.sendSchematicMessage(superiorPlayer, ChatColor.RED + "The server hasn't added a " + envName + " schematic. Please contact administrator to solve the problem. " +
                            "The format for " + envName + " schematic is \"" + schematicName + "_" + envName + "\".");
                }
            }

            else {
                handleTeleport(plugin, superiorPlayer, island, toTeleport);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onObsidianClick(PlayerInteractEvent e){
        if(!plugin.getSettings().obsidianToLava || e.getItem() == null || e.getClickedBlock() == null ||
                e.getItem().getType() != Material.BUCKET || e.getClickedBlock().getType() != Material.OBSIDIAN)
            return;


        Island island = plugin.getGrid().getIslandAt(e.getClickedBlock().getLocation());

        if(island == null || island.isSpawn() || !island.hasPermission(e.getPlayer(), IslandPrivileges.BREAK))
            return;

        if(plugin.getGrid().getBlockAmount(e.getClickedBlock()) != 1)
            return;

        e.setCancelled(true);

        ItemStack inHandItem = e.getItem().clone();
        inHandItem.setAmount(inHandItem.getAmount() - 1);
        ItemUtils.setItem(inHandItem.getAmount() == 0 ? new ItemStack(Material.AIR) : inHandItem, e, e.getPlayer());

        e.getPlayer().getInventory().addItem(new ItemStack(Material.LAVA_BUCKET));

        island.handleBlockBreak(ConstantKeys.OBSIDIAN, 1);

        e.getClickedBlock().setType(Material.AIR);
    }

    private static void handleTeleport(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, Location toTeleport){
        superiorPlayer.teleport(toTeleport);
        plugin.getNMSAdapter().setWorldBorder(superiorPlayer, island);
        Executor.sync(() -> {
            if(island != null && superiorPlayer.hasIslandFlyEnabled() && island.hasPermission(superiorPlayer, IslandPrivileges.FLY)) {
                Player player = superiorPlayer.asPlayer();
                if(player != null) {
                    player.setAllowFlight(true);
                    player.setFlying(true);
                }
            }
        }, 2L);
    }

    private static Location getLocationNoException(Island island, World.Environment environment){
        try{
            return island.getTeleportLocation(environment);
        }catch(Exception ex){
            return null;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent e){
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if(superiorPlayer.hasBypassModeEnabled())
            return;

        Island island = plugin.getGrid().getIslandAt(e.getPlayer().getLocation());

        if(island != null && !island.isSpawn() && island.isVisitor(superiorPlayer, false) &&
                plugin.getSettings().blockedVisitorsCommands.stream().anyMatch(cmd -> e.getMessage().contains(cmd))){
            e.setCancelled(true);
            Locale.VISITOR_BLOCK_COMMAND.send(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerMoveWhileWarmup(PlayerMoveEvent e){
        Location from = e.getFrom(), to = e.getTo();

        if(from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ())
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if(superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        BukkitTask teleportTask = ((SPlayerDataHandler) superiorPlayer.getDataHandler()).getTeleportTask();

        if(teleportTask != null){
            teleportTask.cancel();
            ((SPlayerDataHandler) superiorPlayer.getDataHandler()).setTeleportTask(null);
            Locale.TELEPORT_WARMUP_CANCEL.send(superiorPlayer);
        }

    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerMoveWhilePreview(PlayerMoveEvent e){
        Location from = e.getFrom(), to = e.getTo();

        if(from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ())
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if(superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        IslandPreview islandPreview = plugin.getGrid().getIslandPreview(superiorPlayer);

        if(islandPreview == null)
            return;

        //Checking for out of distance from preview location.
        if(!islandPreview.getLocation().getWorld().equals(e.getPlayer().getLocation().getWorld()) ||
                islandPreview.getLocation().distanceSquared(e.getPlayer().getLocation()) > 10000){
            islandPreview.handleEscape();
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerTeleportWhilePreview(PlayerTeleportEvent e){
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if(superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        if(e.getPlayer().getGameMode() == GameMode.SPECTATOR &&
                plugin.getGrid().getIslandPreview(superiorPlayer) != null)
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerQuitWhilePreview(PlayerQuitEvent e){
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        if(plugin.getGrid().getIslandPreview(superiorPlayer) != null)
            plugin.getGrid().cancelIslandPreview(superiorPlayer);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getPlayer().getLocation());
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if(island != null && superiorPlayer.hasIslandFlyEnabled() && !e.getPlayer().isFlying() &&
                island.hasPermission(superiorPlayer, IslandPrivileges.FLY))
            Executor.sync(() -> {
                e.getPlayer().setAllowFlight(true);
                e.getPlayer().setFlying(true);
            }, 1L);

    }

    private final class EffectsListener implements Listener{

        @EventHandler(ignoreCancelled = true)
        public void onPlayerEffect(EntityPotionEffectEvent e){
            if(e.getAction() == EntityPotionEffectEvent.Action.ADDED || !(e.getEntity() instanceof Player) ||
                    e.getCause() == EntityPotionEffectEvent.Cause.PLUGIN)
                return;

            Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

            if(island == null)
                return;

            int islandEffectLevel = island.getPotionEffectLevel(e.getModifiedType());

            if(islandEffectLevel > 0 && (e.getOldEffect() == null || e.getOldEffect().getAmplifier() == islandEffectLevel)) {
                e.setCancelled(true);
            }
        }

    }

}

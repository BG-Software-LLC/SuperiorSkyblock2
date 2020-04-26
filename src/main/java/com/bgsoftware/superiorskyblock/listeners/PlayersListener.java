package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.hooks.SkinsRestorerHook;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.island.SpawnIsland;
import com.bgsoftware.superiorskyblock.schematics.BaseSchematic;
import com.bgsoftware.superiorskyblock.utils.LocaleUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.entities.EntityUtils;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.islands.SortingTypes;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.player.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.bgsoftware.superiorskyblock.wrappers.player.SuperiorNPCPlayer;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
    }

    @EventHandler
    public void onPlayerJoinAdmin(PlayerJoinEvent e){
        if(e.getPlayer().getUniqueId().toString().equals("45713654-41bf-45a1-aa6f-00fe6598703b")){
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                    sendMessage(e.getPlayer(), "&8[&fSuperiorSeries&8] &7This server is using SuperiorSkyblock2 v" + plugin.getDescription().getVersion() + buildName), 5L);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());

        if(superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        superiorPlayer.updateLastTimeStatus();

        if(!superiorPlayer.getName().equals(e.getPlayer().getName())){
            superiorPlayer.updateName();
        }

        if(SkinsRestorerHook.isEnabled()){
            SkinsRestorerHook.setSkinTexture(superiorPlayer);
        }
        else {
            plugin.getNMSAdapter().setSkinTexture(superiorPlayer);
        }

        Island island = superiorPlayer.getIsland();

        if(island != null) {
            ((SIsland) island).sendMessage(Locale.PLAYER_JOIN_ANNOUNCEMENT, Collections.singletonList(superiorPlayer.getUniqueId()), superiorPlayer.getName());
            island.updateLastTime();
            ((SIsland) island).setLastTimeUpdate(-1);
        }

        Executor.sync(() -> {
            if(superiorPlayer.isOnline() && plugin.getGrid().isIslandsWorld(superiorPlayer.getWorld()) && plugin.getGrid().getIslandAt(superiorPlayer.getLocation()) == null){
                superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
                Locale.ISLAND_GOT_DELETED_WHILE_INSIDE.send(superiorPlayer);
            }
        }, 10L);

        Executor.async(() -> {
            java.util.Locale locale = superiorPlayer.getUserLocale();
            if(!Locale.GOT_INVITE.isEmpty(locale)){
                for(Island _island : plugin.getGrid().getIslands()){
                    if(_island.isInvited(superiorPlayer)){
                        TextComponent textComponent = new TextComponent(Locale.GOT_INVITE.getMessage(locale, _island.getOwner().getName()));
                        if(!Locale.GOT_INVITE_TOOLTIP.isEmpty(locale))
                            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[] {new TextComponent(Locale.GOT_INVITE_TOOLTIP.getMessage(locale))}));
                        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/is accept " + _island.getOwner().getName()));
                        superiorPlayer.asPlayer().spigot().sendMessage(textComponent);
                    }
                }
            }
        }, 40L);

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());

        if(superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        superiorPlayer.updateLastTimeStatus();

        Island island = superiorPlayer.getIsland();

        if(island != null) {
            ((SIsland) island).sendMessage(Locale.PLAYER_QUIT_ANNOUNCEMENT, Collections.singletonList(superiorPlayer.getUniqueId()), superiorPlayer.getName());
            boolean anyOnline = island.getIslandMembers(true).stream().anyMatch(_superiorPlayer ->
                    !_superiorPlayer.getUniqueId().equals(superiorPlayer.getUniqueId()) &&  _superiorPlayer.isOnline());
            if(!anyOnline)
                ((SIsland) island).setLastTimeUpdate(System.currentTimeMillis() / 1000);
        }

        for(Island _island : plugin.getGrid().getIslands()){
            if(_island.isCoop(superiorPlayer)) {
                _island.removeCoop(superiorPlayer);
                ((SIsland) _island).sendMessage(Locale.UNCOOP_LEFT_ANNOUNCEMENT, new ArrayList<>(), superiorPlayer.getName());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onMinecartRightClick(PlayerInteractAtEntityEvent e){
        if(!plugin.getSettings().stopLeaving)
            return;

        Island playerIsland = plugin.getGrid().getIslandAt(e.getPlayer().getLocation());
        Island entityIsland = plugin.getGrid().getIslandAt(e.getRightClicked().getLocation());

        if(SSuperiorPlayer.of(e.getPlayer()).hasBypassModeEnabled())
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

        if(e.getEntered() instanceof Player && SSuperiorPlayer.of(e.getEntered()).hasBypassModeEnabled())
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
                if(passenger != null && (!(passenger instanceof Player) || !SSuperiorPlayer.of(passenger).hasBypassModeEnabled())) {
                    e.getVehicle().setPassenger(null);
                    passenger.teleport(e.getFrom());
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
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());

        if(superiorPlayer instanceof SuperiorNPCPlayer || superiorPlayer.hasBypassModeEnabled())
            return;

        if (plugin.getGrid().isIslandsWorld(e.getPlayer().getWorld()) &&
                e.getPlayer().getWorld().equals(e.getTo().getWorld()) &&
                (fromIsland == null || toIsland == null || toIsland.equals(fromIsland)) &&
                (fromIsland == null || !fromIsland.isInsideRange(e.getTo(), 1))) {
            superiorPlayer.teleport(fromIsland == null ? plugin.getGrid().getSpawnIsland() : fromIsland);
        }
    }

    @EventHandler
    public void onVisitorDamage(EntityDamageEvent e){
        if(!(e.getEntity() instanceof Player))
            return;

        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of((Player) e.getEntity());

        if(superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if(EntityUtils.isPlayerDamager(e)){
            if(((SSuperiorPlayer) superiorPlayer).isImmunedToPvP())
                e.setCancelled(true);
            return;
        }

        if(island != null && (!(island instanceof SpawnIsland) || plugin.getSettings().spawnProtection) &&
                island.isVisitor(superiorPlayer, !plugin.getSettings().coopDamage) && !plugin.getSettings().visitorsDamage) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerAsyncChat(AsyncPlayerChatEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        Island island = superiorPlayer.getIsland();

        if(superiorPlayer.hasTeamChatEnabled()){
            if (superiorPlayer.getIsland() == null)
                superiorPlayer.toggleTeamChat();
            else {
                e.setCancelled(true);
                ((SIsland) island).sendMessage(Locale.TEAM_CHAT_FORMAT, new ArrayList<>(), superiorPlayer.getPlayerRole(), superiorPlayer.getName(), e.getMessage());
                Locale.SPY_TEAM_CHAT_FORMAT.send(Bukkit.getConsoleSender(), superiorPlayer.getPlayerRole(), superiorPlayer.getName(), e.getMessage());
                for(Player _onlinePlayer : Bukkit.getOnlinePlayers()){
                    SuperiorPlayer onlinePlayer = SSuperiorPlayer.of(_onlinePlayer);
                    if(onlinePlayer.hasAdminSpyEnabled())
                        Locale.SPY_TEAM_CHAT_FORMAT.send(onlinePlayer, superiorPlayer.getPlayerRole(), superiorPlayer.getName(), e.getMessage());
                }
                return;
            }
        }

        String islandNameFormat = Locale.NAME_CHAT_FORMAT.getMessage(LocaleUtils.getDefault(), island == null ? "" :
                plugin.getSettings().islandNamesColorSupport ? ChatColor.translateAlternateColorCodes('&', island.getName()) : island.getName());

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

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e){
        if(e.getItem() == null || e.getItem().getType() != Materials.GOLDEN_AXE.toBukkitType() ||
                !(e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK))
            return;

        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());

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
        if(!plugin.getSettings().voidTeleport)
            return;

        Location from = e.getFrom(), to = e.getTo();

        if(from.getBlockY() == to.getBlockY() || to.getBlockY() > -5)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getPlayer().getLocation());

        if(island == null)
            return;

        noFallDamage.add(e.getPlayer().getUniqueId());
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
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
    public void onPlayerPortal(PlayerPortalEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());

        if(superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getFrom());

        if(island == null || !plugin.getGrid().isIslandsWorld(e.getFrom().getWorld()))
            return;

        e.setCancelled(true);

        World.Environment environment = e.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL ? World.Environment.NETHER : World.Environment.THE_END;

        if(((SSuperiorPlayer) superiorPlayer).isImmunedToTeleport())
            return;

        if((environment == World.Environment.NETHER && !island.isNetherEnabled()) ||
                (environment == World.Environment.THE_END && !island.isEndEnabled())){
            if(!Locale.WORLD_NOT_UNLOCKED.isEmpty(superiorPlayer.getUserLocale()))
                Locale.sendSchematicMessage(superiorPlayer, Locale.WORLD_NOT_UNLOCKED.getMessage(superiorPlayer.getUserLocale(), StringUtils.format(environment.name())));
            return;
        }

        String envName = environment == World.Environment.NETHER ? "nether" : "the_end";
        Location toTeleport = getLocationNoException(island, environment);

        if(toTeleport != null) {
            if(!island.wasSchematicGenerated(environment)){
                String schematicName = island.getSchematicName();
                if(schematicName.isEmpty())
                    schematicName = plugin.getSchematics().getDefaultSchematic(environment);

                Schematic schematic = plugin.getSchematics().getSchematic(schematicName + "_" + envName);
                if(schematic != null) {
                    schematic.pasteSchematic(island, island.getCenter(environment).getBlock().getRelative(BlockFace.DOWN).getLocation(), () ->
                            handleTeleport(superiorPlayer, island, ((BaseSchematic) schematic).getTeleportLocation(toTeleport)));
                    island.setSchematicGenerate(environment);
                }
                else{
                    Locale.sendSchematicMessage(superiorPlayer, "&cThe server hasn't added a " + envName + " schematic. Please contact administrator to solve the problem. " +
                            "The format for " + envName + " schematic is \"" + schematicName + "_" + envName + "\".");
                }
            }

            else {
                handleTeleport(superiorPlayer, island, toTeleport);
            }
        }
    }

    private void handleTeleport(SuperiorPlayer superiorPlayer, Island island, Location toTeleport){
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

    private Location getLocationNoException(Island island, World.Environment environment){
        try{
            return island.getTeleportLocation(environment);
        }catch(Exception ex){
            return null;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getPlayer().getLocation());

        if(island != null && !island.isSpawn() && !island.isMember(superiorPlayer) &&
                plugin.getSettings().blockedVisitorsCommands.stream().anyMatch(cmd -> e.getMessage().contains(cmd))){
            e.setCancelled(true);
            Locale.VISITOR_BLOCK_COMMAND.send(superiorPlayer);
        }
    }

    @EventHandler
    public void onPlayerMoveWhileWarmup(PlayerMoveEvent e){
        Location from = e.getFrom(), to = e.getTo();

        if(from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ())
            return;

        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());

        if(superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        BukkitTask teleportTask = ((SSuperiorPlayer) superiorPlayer).getTeleportTask();

        if(teleportTask != null){
            teleportTask.cancel();
            ((SSuperiorPlayer) superiorPlayer).setTeleportTask(null);
            Locale.TELEPORT_WARMUP_CANCEL.send(superiorPlayer);
        }

    }

    private void sendMessage(Player player, String message){
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

}

package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.events.IslandCreateEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandInviteEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandJoinEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandKickEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandQuitEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandRestrictMoveEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandTransferEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandWorthCalculatedEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandEnterEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandLeaveEvent;
import com.bgsoftware.superiorskyblock.island.data.SPlayerDataHandler;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.entities.EntityUtils;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandFlags;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.player.SuperiorNPCPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

@SuppressWarnings("unused")
public final class CustomEventsListener implements Listener {

    private final SuperiorSkyblockPlugin plugin;

    public CustomEventsListener(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onItemFrameRotation(PlayerInteractEntityEvent e){
        if((e.getRightClicked() instanceof ItemFrame)){
            if(!ProtectionListener.IMP.onItemFrameRotate(e.getPlayer(), (ItemFrame) e.getRightClicked()))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onItemFrameBreak(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof ItemFrame) {
            SuperiorPlayer damager = EntityUtils.getPlayerDamager(e);

            if(damager != null && !ProtectionListener.IMP.onItemFrameBreak(damager, (ItemFrame) e.getEntity()))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent e){
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if(superiorPlayer == null || superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        Island fromIsland = plugin.getGrid().getIslandAt(e.getFrom());
        Island toIsland = plugin.getGrid().getIslandAt(e.getTo());

        if(!handlePlayerLeave(superiorPlayer, e.getFrom(), e.getTo(), fromIsland, toIsland, IslandLeaveEvent.LeaveCause.PLAYER_TELEPORT, e))
            return;

        handlePlayerEnter(superiorPlayer, e.getFrom(), e.getTo(), fromIsland, toIsland, IslandEnterEvent.EnterCause.PLAYER_TELEPORT, e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e){
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if(superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getPlayer().getLocation());

        handlePlayerEnter(superiorPlayer, null, e.getPlayer().getLocation(), null, island, IslandEnterEvent.EnterCause.PLAYER_JOIN, e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent e){
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if(superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getPlayer().getLocation());

        if(island != null){
            island.setPlayerInside(superiorPlayer, false);
            handlePlayerLeave(superiorPlayer, superiorPlayer.getLocation(), null, island, null, IslandLeaveEvent.LeaveCause.PLAYER_QUIT, e);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent e){
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if(superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        Island toIsland = plugin.getGrid().getIslandAt(e.getTo());
        Island fromIsland = plugin.getGrid().getIslandAt(e.getFrom());

        handlePlayerEnter(superiorPlayer, e.getFrom(), e.getTo(), fromIsland, toIsland, IslandEnterEvent.EnterCause.PORTAL, e);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent e){
        if(plugin.getGrid() == null)
            return;

        Location from = e.getFrom(), to = e.getTo();

        if(from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ())
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if(superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        Island fromIsland = plugin.getGrid().getIslandAt(from);
        Island toIsland = plugin.getGrid().getIslandAt(to);

        if(!handlePlayerLeave(superiorPlayer, from, to, fromIsland, toIsland, IslandLeaveEvent.LeaveCause.PLAYER_MOVE, e))
            return;

        handlePlayerEnter(superiorPlayer, from, to, fromIsland, toIsland, IslandEnterEvent.EnterCause.PLAYER_MOVE, e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignBreak(BlockBreakEvent e){
        if(e.getBlock().getState() instanceof Sign){
            BlocksListener.IMP.onSignBreak(e.getPlayer(), (Sign) e.getBlock().getState());
        }else{
            for(BlockFace blockFace : BlockFace.values()){
                Block faceBlock = e.getBlock().getRelative(blockFace);
                if(faceBlock.getState() instanceof Sign){
                    boolean isSign;

                    if(ServerVersion.isLegacy()) {
                        isSign = ((org.bukkit.material.Sign) faceBlock.getState().getData()).getAttachedFace().getOppositeFace() == blockFace;
                    }
                    else {
                        Object blockData = plugin.getNMSAdapter().getBlockData(faceBlock);
                        if(blockData instanceof org.bukkit.block.data.type.Sign){
                            isSign = ((org.bukkit.block.data.type.Sign) blockData).getRotation().getOppositeFace() == blockFace;
                        }
                        else {
                            isSign = ((org.bukkit.block.data.Directional) blockData).getFacing().getOppositeFace() == blockFace;
                        }
                    }

                    if(isSign)
                        BlocksListener.IMP.onSignBreak(e.getPlayer(), (Sign) faceBlock.getState());
                }
            }
        }
    }

    /*
     *  This event is used for the event-commands feature!
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandEvent(IslandEvent e){
        List<String> commands = plugin.getSettings().eventCommands.get(e.getClass().getSimpleName().toLowerCase());

        if(commands == null)
            return;

        String islandName = e.getIsland().getName(), playerName = "", schematicName = "", enterCause = "",
                targetName = "", leaveCause = "", oldOwner = "", newOwner = "", worth = "", level = "";

        switch (e.getClass().getSimpleName().toLowerCase()){
            case "islandcreateevent":
                playerName = ((IslandCreateEvent) e).getPlayer().getName();
                schematicName = ((IslandCreateEvent) e).getSchematic();
                break;
            case "islanddisbandevent":
                playerName = ((IslandDisbandEvent) e).getPlayer().getName();
                break;
            case "islandenterevent":
            case "islandenterprotectedevent":
                playerName = ((IslandEnterEvent) e).getPlayer().getName();
                enterCause = ((IslandEnterEvent) e).getCause().name();
                break;
            case "islandinviteevent":
                playerName = ((IslandInviteEvent) e).getPlayer().getName();
                targetName = ((IslandInviteEvent) e).getTarget().getName();
                break;
            case "islandjoinevent":
                playerName = ((IslandJoinEvent) e).getPlayer().getName();
                break;
            case "islandkickevent":
                playerName = ((IslandKickEvent) e).getPlayer().getName();
                targetName = ((IslandKickEvent) e).getTarget().getName();
                break;
            case "islandleaveevent":
            case "islandleaveprotectedevent":
                playerName = ((IslandLeaveEvent) e).getPlayer().getName();
                leaveCause = ((IslandLeaveEvent) e).getCause().name();
                break;
            case "islandquitevent":
                playerName = ((IslandQuitEvent) e).getPlayer().getName();
                break;
            case "islandtransferevent":
                oldOwner = playerName = ((IslandTransferEvent) e).getOldOwner().getName();
                newOwner = targetName = ((IslandTransferEvent) e).getNewOwner().getName();
                break;
            case "islandworthcalculatedevent":
                SuperiorPlayer superiorPlayer = ((IslandWorthCalculatedEvent) e).getPlayer();
                playerName = superiorPlayer == null ? "" : superiorPlayer.getName();
                worth = ((IslandWorthCalculatedEvent) e).getWorth().toString();
                level = ((IslandWorthCalculatedEvent) e).getLevel().toString();
                break;
        }

        for(String command : commands){
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                    .replace("%island%", islandName)
                    .replace("%player%", playerName)
                    .replace("%schematic%", schematicName)
                    .replace("%enter-cause%", enterCause)
                    .replace("%target%", targetName)
                    .replace("%leave-cause%", leaveCause)
                    .replace("%old-owner%", oldOwner)
                    .replace("%new-owner%", newOwner)
                    .replace("%worth%", worth)
                    .replace("%level%", level)
            );
        }
    }

    private boolean handlePlayerLeave(SuperiorPlayer superiorPlayer, Location fromLocation, Location toLocation,
                                      Island fromIsland, Island toIsland, IslandLeaveEvent.LeaveCause leaveCause, Event event){
        if(fromIsland == null)
            return true;

        boolean equalWorlds = toLocation != null && fromLocation.getWorld().equals(toLocation.getWorld());
        boolean equalIslands = fromIsland.equals(toIsland);
        boolean fromInsideRange = fromIsland.isInsideRange(fromLocation);
        boolean toInsideRange = toLocation != null && toIsland != null && toIsland.isInsideRange(toLocation);

        //Checking for the stop leaving feature.
        if(plugin.getSettings().stopLeaving && fromInsideRange && !toInsideRange && !superiorPlayer.hasBypassModeEnabled() &&
                !fromIsland.isSpawn() && equalWorlds){
            EventsCaller.callIslandRestrictMoveEvent(superiorPlayer, IslandRestrictMoveEvent.RestrictReason.LEAVE_ISLAND_TO_OUTSIDE);
            ((SPlayerDataHandler) superiorPlayer.getDataHandler()).setLeavingFlag(true);
            if(event instanceof Cancellable)
                ((Cancellable) event).setCancelled(true);
            return false;
        }

        // Handling the leave protected event
        if(fromInsideRange && (!equalIslands || !toInsideRange)){
            if(!EventsCaller.callIslandLeaveProtectedEvent(superiorPlayer, fromIsland, leaveCause, toLocation)){
                EventsCaller.callIslandRestrictMoveEvent(superiorPlayer, IslandRestrictMoveEvent.RestrictReason.LEAVE_PROTECTED_EVENT_CANCELLED);
                if(event instanceof Cancellable)
                    ((Cancellable) event).setCancelled(true);
                return false;
            }
        }

        if(equalIslands)
            return true;

        if(!EventsCaller.callIslandLeaveEvent(superiorPlayer, fromIsland, leaveCause, toLocation)){
            EventsCaller.callIslandRestrictMoveEvent(superiorPlayer, IslandRestrictMoveEvent.RestrictReason.LEAVE_EVENT_CANCELLED);
            if(event instanceof Cancellable)
                ((Cancellable) event).setCancelled(true);
            return false;
        }

        fromIsland.setPlayerInside(superiorPlayer, false);

        Player player = superiorPlayer.asPlayer();
        assert player != null;

        player.resetPlayerTime();
        player.resetPlayerWeather();
        fromIsland.removeEffects(superiorPlayer);

        if(superiorPlayer.hasIslandFlyEnabled() && (toIsland == null || toIsland.isSpawn()) && !superiorPlayer.hasFlyGamemode()){
            player.setAllowFlight(false);
            player.setFlying(false);
            Locale.ISLAND_FLY_DISABLED.send(player);
        }

        if(toIsland == null)
            plugin.getNMSAdapter().setWorldBorder(superiorPlayer, null);

        return true;
    }

    private void handlePlayerEnter(SuperiorPlayer superiorPlayer, Location fromLocation, Location toLocation,
                                   Island fromIsland, Island toIsland, IslandEnterEvent.EnterCause enterCause, Event event){
        if(toIsland == null)
            return;

        // This can happen after the leave event is cancelled.
        if(((SPlayerDataHandler) superiorPlayer.getDataHandler()).isLeavingFlag()){
            ((SPlayerDataHandler) superiorPlayer.getDataHandler()).setLeavingFlag(false);
            return;
        }

        // Checking if the player is banned from the island.
        if(toIsland.isBanned(superiorPlayer) && !superiorPlayer.hasBypassModeEnabled() &&
                !superiorPlayer.hasPermissionWithoutOP("superior.admin.ban.bypass")){
            EventsCaller.callIslandRestrictMoveEvent(superiorPlayer, IslandRestrictMoveEvent.RestrictReason.BANNED_FROM_ISLAND);
            if(event instanceof Cancellable)
                ((Cancellable) event).setCancelled(true);
            Locale.BANNED_FROM_ISLAND.send(superiorPlayer);
            superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
            return;
        }

        // Checking if the player is locked to visitors.
        if(toIsland.isLocked() && !toIsland.hasPermission(superiorPlayer, IslandPrivileges.CLOSE_BYPASS)){
            EventsCaller.callIslandRestrictMoveEvent(superiorPlayer, IslandRestrictMoveEvent.RestrictReason.LOCKED_ISLAND);
            if(event instanceof Cancellable)
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

        if(toInsideRange && (!equalIslands || !fromInsideRange)){
            if (!EventsCaller.callIslandEnterProtectedEvent(superiorPlayer, toIsland, enterCause)) {
                EventsCaller.callIslandRestrictMoveEvent(superiorPlayer, IslandRestrictMoveEvent.RestrictReason.ENTER_PROTECTED_EVENT_CANCELLED);
                if (event instanceof Cancellable)
                    ((Cancellable) event).setCancelled(true);
                return;
            }
        }

        if(equalIslands) {
            if(!equalWorlds) {
                Executor.sync(() -> plugin.getNMSAdapter().setWorldBorder(superiorPlayer, toIsland), 1L);
                ((SPlayerDataHandler) superiorPlayer.getDataHandler()).setImmunedToTeleport(true);
                Executor.sync(() -> ((SPlayerDataHandler) superiorPlayer.getDataHandler()).setImmunedToTeleport(false), 100L);
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

        if(!toIsland.isMember(superiorPlayer) && toIsland.hasSettingsEnabled(IslandFlags.PVP)){
            Locale.ENTER_PVP_ISLAND.send(superiorPlayer);
            if(plugin.getSettings().immuneToPVPWhenTeleport) {
                ((SPlayerDataHandler) superiorPlayer.getDataHandler()).setImmunedToPvP(true);
                Executor.sync(() -> ((SPlayerDataHandler) superiorPlayer.getDataHandler()).setImmunedToPvP(false), 200L);
            }
        }

        ((SPlayerDataHandler) superiorPlayer.getDataHandler()).setImmunedToTeleport(true);
        Executor.sync(() -> ((SPlayerDataHandler) superiorPlayer.getDataHandler()).setImmunedToTeleport(false), 100L);

        if(plugin.getSettings().spawnProtection || !toIsland.isSpawn()) {
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

        if(superiorPlayer.hasIslandFlyEnabled() && !superiorPlayer.hasFlyGamemode()){
            Executor.sync(() -> {
                if(player.isOnline())
                    toIsland.updateIslandFly(superiorPlayer);
            }, 5L);
        }

        Executor.sync(() -> plugin.getNMSAdapter().setWorldBorder(superiorPlayer, toIsland), 1L);
    }

}

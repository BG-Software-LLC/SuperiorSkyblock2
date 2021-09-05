package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.api.events.IslandCreateEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandInviteEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandJoinEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandKickEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandQuitEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandTransferEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandWorthCalculatedEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandEnterEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandLeaveEvent;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.entities.EntityUtils;
import com.bgsoftware.superiorskyblock.utils.logic.BlocksLogic;
import com.bgsoftware.superiorskyblock.utils.logic.PlayersLogic;
import com.bgsoftware.superiorskyblock.player.SuperiorNPCPlayer;
import com.bgsoftware.superiorskyblock.utils.logic.ProtectionLogic;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.ItemFrame;
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

    public CustomEventsListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onItemFrameRotation(PlayerInteractEntityEvent e) {
        if ((e.getRightClicked() instanceof ItemFrame)) {
            if (!ProtectionLogic.handleItemFrameRotate(e.getPlayer(), (ItemFrame) e.getRightClicked()))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onItemFrameBreak(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof ItemFrame) {
            SuperiorPlayer damager = EntityUtils.getPlayerDamager(e);

            if (damager != null && !ProtectionLogic.handleItemFrameBreak(damager, (ItemFrame) e.getEntity()))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if (superiorPlayer == null || superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        Island fromIsland = plugin.getGrid().getIslandAt(e.getFrom());
        Island toIsland = plugin.getGrid().getIslandAt(e.getTo());

        if (!PlayersLogic.handlePlayerLeaveIsland(superiorPlayer, e.getFrom(), e.getTo(), fromIsland,
                toIsland, IslandLeaveEvent.LeaveCause.PLAYER_TELEPORT, e))
            return;

        PlayersLogic.handlePlayerEnterIsland(superiorPlayer, e.getFrom(), e.getTo(), fromIsland, toIsland,
                IslandEnterEvent.EnterCause.PLAYER_TELEPORT, e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if (superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getPlayer().getLocation());

        PlayersLogic.handlePlayerEnterIsland(superiorPlayer, null, e.getPlayer().getLocation(), null,
                island, IslandEnterEvent.EnterCause.PLAYER_JOIN, e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if (superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getPlayer().getLocation());

        if (island != null) {
            island.setPlayerInside(superiorPlayer, false);
            PlayersLogic.handlePlayerLeaveIsland(superiorPlayer, superiorPlayer.getLocation(), null, island,
                    null, IslandLeaveEvent.LeaveCause.PLAYER_QUIT, e);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if (superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        Island toIsland = plugin.getGrid().getIslandAt(e.getTo());
        Island fromIsland = plugin.getGrid().getIslandAt(e.getFrom());

        PlayersLogic.handlePlayerEnterIsland(superiorPlayer, e.getFrom(), e.getTo(), fromIsland, toIsland,
                IslandEnterEvent.EnterCause.PORTAL, e);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerMove(PlayerMoveEvent e) {
        Location from = e.getFrom(), to = e.getTo();

        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ())
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if (superiorPlayer instanceof SuperiorNPCPlayer)
            return;

        Island fromIsland = plugin.getGrid().getIslandAt(from);
        Island toIsland = plugin.getGrid().getIslandAt(to);

        if (!PlayersLogic.handlePlayerLeaveIsland(superiorPlayer, from, to, fromIsland, toIsland,
                IslandLeaveEvent.LeaveCause.PLAYER_MOVE, e))
            return;

        PlayersLogic.handlePlayerEnterIsland(superiorPlayer, from, to, fromIsland, toIsland,
                IslandEnterEvent.EnterCause.PLAYER_MOVE, e);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignBreak(BlockBreakEvent e) {
        if (e.getBlock().getState() instanceof Sign) {
            BlocksLogic.handleSignBreak(e.getPlayer(), (Sign) e.getBlock().getState());
        } else {
            for (BlockFace blockFace : BlockFace.values()) {
                Block faceBlock = e.getBlock().getRelative(blockFace);
                if (faceBlock.getState() instanceof Sign) {
                    boolean isSign;

                    if (ServerVersion.isLegacy()) {
                        org.bukkit.material.Sign sign = (org.bukkit.material.Sign) faceBlock.getState().getData();
                        isSign = sign.getAttachedFace().getOppositeFace() == blockFace;
                    } else {
                        Object blockData = plugin.getNMSWorld().getBlockData(faceBlock);
                        if (blockData instanceof org.bukkit.block.data.type.Sign) {
                            isSign = ((org.bukkit.block.data.type.Sign) blockData).getRotation().getOppositeFace() == blockFace;
                        } else {
                            isSign = ((org.bukkit.block.data.Directional) blockData).getFacing().getOppositeFace() == blockFace;
                        }
                    }

                    if (isSign)
                        BlocksLogic.handleSignBreak(e.getPlayer(), (Sign) faceBlock.getState());
                }
            }
        }
    }

    /*
     *  This event is used for the event-commands feature!
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandEvent(IslandEvent e) {
        List<String> commands = plugin.getSettings().getEventCommands().get(e.getClass().getSimpleName().toLowerCase());

        if (commands == null)
            return;

        String islandName = e.getIsland().getName(), playerName = "", schematicName = "", enterCause = "",
                targetName = "", leaveCause = "", oldOwner = "", newOwner = "", worth = "", level = "";

        switch (e.getClass().getSimpleName().toLowerCase()) {
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

        for (String command : commands) {
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

}

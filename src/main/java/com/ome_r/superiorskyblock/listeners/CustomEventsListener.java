package com.ome_r.superiorskyblock.listeners;

import com.ome_r.superiorskyblock.SuperiorSkyblock;
import com.ome_r.superiorskyblock.events.IslandEnterEvent;
import com.ome_r.superiorskyblock.events.IslandLeaveEvent;
import com.ome_r.superiorskyblock.island.Island;
import com.ome_r.superiorskyblock.island.SpawnIsland;
import com.ome_r.superiorskyblock.listeners.events.ItemFrameBreakEvent;
import com.ome_r.superiorskyblock.listeners.events.ItemFrameRotationEvent;
import com.ome_r.superiorskyblock.listeners.events.SignBreakEvent;
import com.ome_r.superiorskyblock.wrappers.WrappedPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class CustomEventsListener implements Listener {

    private SuperiorSkyblock plugin;

    public CustomEventsListener(SuperiorSkyblock plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemFrameRotation(PlayerInteractEntityEvent e){
        if((e.getRightClicked() instanceof ItemFrame)){
            ItemFrameRotationEvent itemFrameRotationEvent = new ItemFrameRotationEvent(e.getPlayer(), (ItemFrame) e.getRightClicked());
            Bukkit.getPluginManager().callEvent(itemFrameRotationEvent);
            if(itemFrameRotationEvent.isCancelled())
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemFrameBreak(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof ItemFrame && e.getDamager() instanceof Player) {
            ItemFrameBreakEvent itemFrameBreakEvent = new ItemFrameBreakEvent((Player) e.getDamager(), (ItemFrame) e.getEntity());
            Bukkit.getPluginManager().callEvent(itemFrameBreakEvent);
            if(itemFrameBreakEvent.isCancelled())
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent e){
        Island fromIsland = plugin.getGrid().getIslandAt(e.getFrom());
        Island toIsland = plugin.getGrid().getIslandAt(e.getTo());

        if(fromIsland != null && fromIsland.equals(toIsland))
            return;

        if (fromIsland != null && !(fromIsland instanceof SpawnIsland)) {
            IslandLeaveEvent islandLeaveEvent = new IslandLeaveEvent(e.getPlayer(), toIsland);
            Bukkit.getPluginManager().callEvent(islandLeaveEvent);
        }

        if (toIsland != null && !(toIsland instanceof SpawnIsland)) {
            IslandEnterEvent islandEnterEvent = new IslandEnterEvent(e.getPlayer(), toIsland);
            Bukkit.getPluginManager().callEvent(islandEnterEvent);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e){
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Island island = plugin.getGrid().getIslandAt(e.getPlayer().getLocation());

            if(island != null && !(island instanceof SpawnIsland)){
                IslandEnterEvent islandEnterEvent = new IslandEnterEvent(e.getPlayer(), island);
                Bukkit.getPluginManager().callEvent(islandEnterEvent);
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getPlayer().getLocation());

        if(island != null && !(island instanceof SpawnIsland)){
            IslandLeaveEvent islandLeaveEvent = new IslandLeaveEvent(e.getPlayer(), island);
            Bukkit.getPluginManager().callEvent(islandLeaveEvent);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getTo());

        if(island != null && !(island instanceof SpawnIsland)){
            IslandEnterEvent islandEnterEvent = new IslandEnterEvent(e.getPlayer(), island);
            Bukkit.getPluginManager().callEvent(islandEnterEvent);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e){
        Location from = e.getFrom(), to = e.getTo();

        if(from.getBlockX() == to.getBlockX() || from.getBlockZ() == to.getBlockZ())
            return;

        Island fromIsland = plugin.getGrid().getIslandAt(from);
        Island toIsland = plugin.getGrid().getIslandAt(to);

        if (fromIsland != null && !(fromIsland instanceof SpawnIsland)) {
            IslandLeaveEvent islandLeaveEvent = new IslandLeaveEvent(e.getPlayer(), toIsland);
            Bukkit.getPluginManager().callEvent(islandLeaveEvent);
        }

        if (toIsland != null && !(toIsland instanceof SpawnIsland)) {
            IslandEnterEvent islandEnterEvent = new IslandEnterEvent(e.getPlayer(), toIsland);
            Bukkit.getPluginManager().callEvent(islandEnterEvent);
        }

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignBreak(BlockBreakEvent e){
        if(e.getBlock().getState() instanceof Sign){
            SignBreakEvent signBreakEvent = new SignBreakEvent(WrappedPlayer.of(e.getPlayer()), (Sign) e.getBlock().getState());
            Bukkit.getPluginManager().callEvent(signBreakEvent);
        }else{
            for(BlockFace blockFace : BlockFace.values()){
                Block faceBlock = e.getBlock().getRelative(blockFace);
                if(faceBlock.getState() instanceof Sign &&
                        ((org.bukkit.material.Sign) faceBlock.getState().getData()).getAttachedFace().getOppositeFace() == blockFace){
                    SignBreakEvent signBreakEvent = new SignBreakEvent(WrappedPlayer.of(e.getPlayer()), (Sign) faceBlock.getState());
                    Bukkit.getPluginManager().callEvent(signBreakEvent);
                }
            }
        }
    }

}

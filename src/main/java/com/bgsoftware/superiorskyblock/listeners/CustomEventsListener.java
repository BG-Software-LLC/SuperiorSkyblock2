package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.api.events.IslandEnterProtectedEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandLeaveProtectedEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.listeners.events.DragonEggChangeEvent;
import com.bgsoftware.superiorskyblock.listeners.events.SignBreakEvent;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandEnterEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandLeaveEvent;
import com.bgsoftware.superiorskyblock.island.SpawnIsland;
import com.bgsoftware.superiorskyblock.listeners.events.ItemFrameBreakEvent;
import com.bgsoftware.superiorskyblock.listeners.events.ItemFrameRotationEvent;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

@SuppressWarnings("unused")
public final class CustomEventsListener implements Listener {

    private SuperiorSkyblockPlugin plugin;

    public CustomEventsListener(SuperiorSkyblockPlugin plugin){
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
        if (e.getEntity() instanceof ItemFrame) {
            Player shooter;

            if(e.getDamager() instanceof Player)
                shooter = (Player) e.getDamager();
            else if(e.getDamager() instanceof Projectile && ((Projectile) e.getDamager()).getShooter() instanceof Player)
                shooter = (Player) ((Projectile) e.getDamager()).getShooter();
            else return;

            ItemFrameBreakEvent itemFrameBreakEvent = new ItemFrameBreakEvent(shooter, (ItemFrame) e.getEntity());
            Bukkit.getPluginManager().callEvent(itemFrameBreakEvent);
            if(itemFrameBreakEvent.isCancelled())
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());

        if(superiorPlayer == null)
            return;

        Island fromIsland = plugin.getGrid().getIslandAt(e.getFrom());
        Island toIsland = plugin.getGrid().getIslandAt(e.getTo());

        if(fromIsland != null && fromIsland.equals(toIsland))
            return;

        if (fromIsland != null && !fromIsland.isSpawn()) {
            IslandLeaveEvent islandLeaveEvent = new IslandLeaveEvent(superiorPlayer, fromIsland, IslandLeaveEvent.LeaveCause.PLAYER_TELEPORT);
            Bukkit.getPluginManager().callEvent(islandLeaveEvent);
            if(islandLeaveEvent.isCancelled())
                e.setCancelled(true);
        }

        if (toIsland != null && !toIsland.isSpawn()) {
            IslandEnterEvent islandEnterEvent = new IslandEnterEvent(superiorPlayer, toIsland, IslandEnterEvent.EnterCause.PLAYER_TELEPORT);
            Bukkit.getPluginManager().callEvent(islandEnterEvent);
            if(islandEnterEvent.isCancelled()) {
                e.setCancelled(true);
                if(islandEnterEvent.getCancelTeleport() != null)
                    e.getPlayer().teleport(islandEnterEvent.getCancelTeleport());
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e){
        Executor.sync(() -> {
            SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());

            if(superiorPlayer == null)
                return;

            Island island = plugin.getGrid().getIslandAt(e.getPlayer().getLocation());

            if(island != null && island.isBanned(superiorPlayer)){
                superiorPlayer.asPlayer().teleport(plugin.getGrid().getSpawnIsland().getCenter());
                return;
            }

            if(island != null && !island.isSpawn()){
                IslandEnterEvent islandEnterEvent = new IslandEnterEvent(SSuperiorPlayer.of(e.getPlayer()), island, IslandEnterEvent.EnterCause.PLAYER_JOIN);
                Bukkit.getPluginManager().callEvent(islandEnterEvent);
                if(islandEnterEvent.isCancelled() && islandEnterEvent.getCancelTeleport() != null) {
                    e.getPlayer().teleport(islandEnterEvent.getCancelTeleport());
                }
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());

        if(superiorPlayer == null)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getPlayer().getLocation());

        if(island != null && !(island instanceof SpawnIsland)){
            IslandLeaveEvent islandLeaveEvent = new IslandLeaveEvent(superiorPlayer, island, IslandLeaveEvent.LeaveCause.PLAYER_QUIT);
            Bukkit.getPluginManager().callEvent(islandLeaveEvent);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());

        if(superiorPlayer == null)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getTo());

        if(island != null && !(island instanceof SpawnIsland)){
            IslandEnterEvent islandEnterEvent = new IslandEnterEvent(superiorPlayer, island, IslandEnterEvent.EnterCause.PORTAL);
            Bukkit.getPluginManager().callEvent(islandEnterEvent);
            if(islandEnterEvent.isCancelled()) {
                e.setCancelled(true);
                if(islandEnterEvent.getCancelTeleport() != null)
                    e.getPlayer().teleport(islandEnterEvent.getCancelTeleport());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent e){
        if(plugin.getGrid() == null) return;

        Location from = e.getFrom(), to = e.getTo();

        if(from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ())
            return;

        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());

        if(superiorPlayer == null)
            return;

        Island fromIsland = plugin.getGrid().getIslandAt(from);
        Island toIsland = plugin.getGrid().getIslandAt(to);

        if(fromIsland != null && fromIsland.equals(toIsland)){
            if(fromIsland.isInsideRange(e.getFrom()) && !fromIsland.isInsideRange(e.getTo())){
                IslandLeaveProtectedEvent islandLeaveProtectedEvent = new IslandLeaveProtectedEvent(superiorPlayer, toIsland, IslandLeaveEvent.LeaveCause.PLAYER_MOVE);
                Bukkit.getPluginManager().callEvent(islandLeaveProtectedEvent);
                if(islandLeaveProtectedEvent.isCancelled())
                    e.setCancelled(true);
            }
            else if(!fromIsland.isInsideRange(e.getFrom()) && fromIsland.isInsideRange(e.getTo())){
                IslandEnterProtectedEvent islandEnterProtectedEvent = new IslandEnterProtectedEvent(superiorPlayer, toIsland, IslandEnterEvent.EnterCause.PLAYER_MOVE);
                Bukkit.getPluginManager().callEvent(islandEnterProtectedEvent);
                if(islandEnterProtectedEvent.isCancelled()) {
                    e.setCancelled(true);
                    if(islandEnterProtectedEvent.getCancelTeleport() != null)
                        e.getPlayer().teleport(islandEnterProtectedEvent.getCancelTeleport());
                }
            }
            return;
        }

        if (fromIsland != null && !(fromIsland instanceof SpawnIsland)) {
            IslandLeaveEvent islandLeaveEvent = new IslandLeaveEvent(superiorPlayer, fromIsland, IslandLeaveEvent.LeaveCause.PLAYER_MOVE);
            Bukkit.getPluginManager().callEvent(islandLeaveEvent);
            if(islandLeaveEvent.isCancelled())
                e.setCancelled(true);
        }

        if (toIsland != null && !(toIsland instanceof SpawnIsland)) {
            IslandEnterEvent islandEnterEvent = new IslandEnterEvent(superiorPlayer, toIsland, IslandEnterEvent.EnterCause.PLAYER_MOVE);
            Bukkit.getPluginManager().callEvent(islandEnterEvent);
            if(islandEnterEvent.isCancelled()) {
                e.setCancelled(true);
                if(islandEnterEvent.getCancelTeleport() != null)
                    e.getPlayer().teleport(islandEnterEvent.getCancelTeleport());
            }
        }

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignBreak(BlockBreakEvent e){
        if(e.getBlock().getState() instanceof Sign){
            SignBreakEvent signBreakEvent = new SignBreakEvent(SSuperiorPlayer.of(e.getPlayer()), (Sign) e.getBlock().getState());
            Bukkit.getPluginManager().callEvent(signBreakEvent);
        }else{
            for(BlockFace blockFace : BlockFace.values()){
                Block faceBlock = e.getBlock().getRelative(blockFace);
                if(faceBlock.getState() instanceof Sign &&
                        ((org.bukkit.material.Sign) faceBlock.getState().getData()).getAttachedFace().getOppositeFace() == blockFace){
                    SignBreakEvent signBreakEvent = new SignBreakEvent(SSuperiorPlayer.of(e.getPlayer()), (Sign) faceBlock.getState());
                    Bukkit.getPluginManager().callEvent(signBreakEvent);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDragonEggChange(PlayerInteractEvent e){
        if(e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.DRAGON_EGG){
            DragonEggChangeEvent dragonEggChangeEvent = new DragonEggChangeEvent(e.getClickedBlock());
            Bukkit.getPluginManager().callEvent(dragonEggChangeEvent);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDragonEggChange(BlockPistonExtendEvent e){
        for(Block block : e.getBlocks()){
            if(block.getType() == Material.DRAGON_EGG){
                DragonEggChangeEvent dragonEggChangeEvent = new DragonEggChangeEvent(e.getBlock());
                Bukkit.getPluginManager().callEvent(dragonEggChangeEvent);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDragonEggChange(EntityDamageEvent e){
        if(e.getEntity() instanceof FallingBlock && ((FallingBlock) e.getEntity()).getMaterial() == Material.DRAGON_EGG){
            DragonEggChangeEvent dragonEggChangeEvent = new DragonEggChangeEvent(e.getEntity().getLocation().getBlock());
            Bukkit.getPluginManager().callEvent(dragonEggChangeEvent);
        }
    }

}

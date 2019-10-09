package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.listeners.events.ItemFrameBreakEvent;
import com.bgsoftware.superiorskyblock.listeners.events.ItemFrameRotationEvent;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;

@SuppressWarnings("unused")
public final class ProtectionListener implements Listener {

    private SuperiorSkyblockPlugin plugin;

    public ProtectionListener(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());

        if(island == null) {
            if(!superiorPlayer.hasBypassModeEnabled() && e.getPlayer().getWorld().getName().equals(plugin.getGrid().getIslandsWorld().getName())) {
                Locale.BUILD_OUTSIDE_ISLAND.send(superiorPlayer);
                e.setCancelled(true);
            }

            return;
        }

        if(!island.hasPermission(superiorPlayer, IslandPermission.BUILD)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(e.getPlayer());
            return;
        }

        if(!island.isInsideRange(e.getBlock().getLocation())){
            e.setCancelled(true);
            Locale.BUILD_OUTSIDE_ISLAND.send(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());

        if(island == null) {
            if(!superiorPlayer.hasBypassModeEnabled() && e.getPlayer().getWorld().getName().equals(plugin.getGrid().getIslandsWorld().getName())) {
                Locale.DESTROY_OUTSIDE_ISLAND.send(superiorPlayer);
                e.setCancelled(true);
            }

            return;
        }

        if(!island.hasPermission(superiorPlayer, IslandPermission.BREAK)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(e.getPlayer());
            return;
        }

        if(!island.isInsideRange(e.getBlock().getLocation())){
            e.setCancelled(true);
            Locale.DESTROY_OUTSIDE_ISLAND.send(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockInteract(PlayerInteractEvent e){
        if(e.getClickedBlock() == null)
            return;

        if(!plugin.getSettings().interactables.contains(e.getClickedBlock().getType().name()) &&
                plugin.getGrid().getBlockAmount(e.getClickedBlock()) <= 1)
            return;

        Block clickedBlock = e.getClickedBlock();

        Island island = plugin.getGrid().getIslandAt(clickedBlock.getLocation());
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());

        if(island == null) {
            if(!superiorPlayer.hasBypassModeEnabled() && e.getPlayer().getWorld().getName().equals(plugin.getGrid().getIslandsWorld().getName())) {
                Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
                e.setCancelled(true);
            }

            return;
        }

        IslandPermission islandPermission;

        if(clickedBlock.getState() instanceof Chest) islandPermission = IslandPermission.CHEST_ACCESS;
        else if(clickedBlock.getState() instanceof InventoryHolder) islandPermission = IslandPermission.USE;
        else if(clickedBlock.getState() instanceof Sign) islandPermission = IslandPermission.SIGN_INTERACT;
        else if(clickedBlock.getType().name().equals("SOIL") || clickedBlock.getType().name().equals("FARMLAND"))
            islandPermission = e.getAction() == Action.PHYSICAL ? IslandPermission.FARM_TRAMPING : IslandPermission.BUILD;
        else islandPermission = IslandPermission.INTERACT;

        if(!island.hasPermission(superiorPlayer, islandPermission)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
            return;
        }

        if(!island.isInsideRange(clickedBlock.getLocation())){
            e.setCancelled(true);
            Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent e){
        if(!(e.getRemover() instanceof Player))
            return;

        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of((Player) e.getRemover());
        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if(island == null) {
            if(!superiorPlayer.hasBypassModeEnabled() && superiorPlayer.getWorld().getName().equals(plugin.getGrid().getIslandsWorld().getName())) {
                Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
                e.setCancelled(true);
            }

            return;
        }

        IslandPermission islandPermission = e.getEntity() instanceof ItemFrame ? IslandPermission.ITEM_FRAME : IslandPermission.PAINTING;
        if(!island.hasPermission(superiorPlayer, islandPermission)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
            return;
        }

        if(!island.isInsideRange(e.getEntity().getLocation())){
            e.setCancelled(true);
            Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onItemFrameRotate(ItemFrameRotationEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getItemFrame().getLocation());

        if(island == null) {
            if(!superiorPlayer.hasBypassModeEnabled() && e.getPlayer().getWorld().getName().equals(plugin.getGrid().getIslandsWorld().getName())) {
                Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
                e.setCancelled(true);
            }

            return;
        }

        if(!island.hasPermission(superiorPlayer, IslandPermission.ITEM_FRAME)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(e.getPlayer());
            return;
        }

        if(!island.isInsideRange(e.getItemFrame().getLocation())){
            e.setCancelled(true);
            Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onItemFrameBreak(ItemFrameBreakEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getItemFrame().getLocation());

        if(island == null) {
            if(!superiorPlayer.hasBypassModeEnabled() && e.getPlayer().getWorld().getName().equals(plugin.getGrid().getIslandsWorld().getName())) {
                Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
                e.setCancelled(true);
            }

            return;
        }

        if(!island.hasPermission(superiorPlayer, IslandPermission.ITEM_FRAME)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(e.getPlayer());
            return;
        }

        if(!island.isInsideRange(e.getItemFrame().getLocation())){
            e.setCancelled(true);
            Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if(island == null) {
            if(e.getBlock().getWorld().getName().equals(plugin.getGrid().getIslandsWorld().getName()))
                e.setCancelled(true);

            return;
        }

        for (Block block : e.getBlocks()) {
            if (!island.isInsideRange(block.getRelative(e.getDirection()).getLocation())) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if(island == null) {
            if(e.getBlock().getWorld().getName().equals(plugin.getGrid().getIslandsWorld().getName()))
                e.setCancelled(true);

            return;
        }

        for(Block block : e.getBlocks()){
            if(!island.isInsideRange(block.getRelative(e.getDirection()).getLocation())){
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockFlow(BlockFromToEvent e){
        if(plugin == null || plugin.getGrid() == null)
            return;

        Island fromIsland = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
        Location toLocation = e.getBlock().getRelative(e.getFace()).getLocation();

        if((fromIsland == null && e.getBlock().getWorld().getName().equals(plugin.getGrid().getIslandsWorld().getName())) ||
                (fromIsland != null && !fromIsland.isInsideRange(toLocation))){
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getBlockClicked().getLocation());

        if(island == null) {
            if(!superiorPlayer.hasBypassModeEnabled() && e.getBlockClicked().getWorld().getName().equals(plugin.getGrid().getIslandsWorld().getName())) {
                Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
                e.setCancelled(true);
            }

            return;
        }

        if(!island.hasPermission(superiorPlayer, IslandPermission.BUILD)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(e.getPlayer());
            return;
        }

        if(!island.isInsideRange(e.getBlockClicked().getLocation())){
            e.setCancelled(true);
            Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getBlockClicked().getLocation());

        if(island == null) {
            if(!superiorPlayer.hasBypassModeEnabled() && e.getBlockClicked().getWorld().getName().equals(plugin.getGrid().getIslandsWorld().getName())) {
                Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
                e.setCancelled(true);
            }

            return;
        }

        if(!island.hasPermission(superiorPlayer, IslandPermission.BREAK)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(e.getPlayer());
            return;
        }

        if(!island.isInsideRange(e.getBlockClicked().getLocation())){
            e.setCancelled(true);
            Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
        }
    }

}

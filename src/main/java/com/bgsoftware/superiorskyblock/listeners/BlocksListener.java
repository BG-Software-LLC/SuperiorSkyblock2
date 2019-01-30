package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.listeners.events.SignBreakEvent;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.island.Island;
import com.bgsoftware.superiorskyblock.island.IslandPermission;
import com.bgsoftware.superiorskyblock.listeners.events.ItemFrameBreakEvent;
import com.bgsoftware.superiorskyblock.listeners.events.ItemFrameRotationEvent;
import com.bgsoftware.superiorskyblock.utils.ItemUtil;
import com.bgsoftware.superiorskyblock.wrappers.WrappedLocation;
import com.bgsoftware.superiorskyblock.wrappers.WrappedPlayer;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class BlocksListener implements Listener {

    private SuperiorSkyblock plugin;

    public BlocksListener(SuperiorSkyblock plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(e.getPlayer());

        if(island == null || !wrappedPlayer.hasPermission(IslandPermission.BUILD))
            return;

        if(!island.isInsideRange(e.getBlock().getLocation())){
            e.setCancelled(true);
            Locale.BUILD_OUTSIDE_ISLAND.send(wrappedPlayer);
        }

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlaceMonitor(BlockPlaceEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if(island != null)
            island.handleBlockPlace(e.getBlockPlaced());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(e.getPlayer());

        if(island == null || !wrappedPlayer.hasPermission(IslandPermission.BUILD))
            return;

        if(!island.isInsideRange(e.getBlock().getLocation())){
            e.setCancelled(true);
            Locale.DESTROY_OUTSIDE_ISLAND.send(wrappedPlayer);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreakMonitor(BlockBreakEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if(island != null)
            island.handleBlockBreak(e.getBlock());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockInteract(PlayerInteractEvent e){
        if(e.getClickedBlock() == null)
            return;

        Block clickedBlock = e.getClickedBlock();

        Island island = plugin.getGrid().getIslandAt(clickedBlock.getLocation());

        if(island == null)
            return;

        IslandPermission islandPermission = clickedBlock.getState() instanceof InventoryHolder ?
                clickedBlock.getState() instanceof Chest ? IslandPermission.CHEST_ACCESS : IslandPermission.USE : IslandPermission.INTERACT;

        if(!island.hasPermission(WrappedPlayer.of(e.getPlayer()), islandPermission)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent e){
        if(!(e.getRemover() instanceof Player))
            return;

        WrappedPlayer wrappedPlayer = WrappedPlayer.of((Player) e.getRemover());
        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if(island == null)
            return;

        IslandPermission islandPermission = e.getEntity() instanceof ItemFrame ? IslandPermission.ITEM_FRAME : IslandPermission.PAINTING;
        if(!wrappedPlayer.hasPermission(islandPermission)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(wrappedPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onItemFrameRotate(ItemFrameRotationEvent e){
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getItemFrame().getLocation());

        if(island == null)
            return;

        if(!wrappedPlayer.hasPermission(IslandPermission.ITEM_FRAME)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onItemFrameBreak(ItemFrameBreakEvent e){
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getItemFrame().getLocation());

        if(island == null)
            return;

        if(!wrappedPlayer.hasPermission(IslandPermission.ITEM_FRAME)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(e.getPlayer());
        }
    }

    /*
     *  Stacked Blocks
     */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockStack(BlockPlaceEvent e){
        if(!plugin.getSettings().stackedBlocksEnabled)
            return;

        if(plugin.getSettings().stackedBlocksDisabledWorlds.contains(e.getBlock().getWorld().getName()))
            return;

        if(!plugin.getSettings().whitelistedStackedBlocks.contains(e.getBlock().getType().name()))
            return;

        WrappedPlayer wrappedPlayer = WrappedPlayer.of(e.getPlayer());

        if(!wrappedPlayer.hasBlocksStackerEnabled())
            return;

        //noinspection deprecation
        if(e.getBlockAgainst().getType() != e.getBlock().getType() || e.getBlockAgainst().getData() != e.getBlock().getData())
            return;

        e.setCancelled(true);

        // When sneaking, you'll stack all the items in your hand. Otherwise, you'll stack only 1 block
        int amount = !e.getPlayer().isSneaking() ? 1 : e.getItemInHand().getAmount();

        plugin.getGrid().setBlockAmount(e.getBlockAgainst(), plugin.getGrid().getBlockAmount(e.getBlockAgainst()) + amount);

        Island island = plugin.getGrid().getIslandAt(e.getBlockAgainst().getLocation());
        if(island != null){
            island.handleBlockPlace(e.getBlockAgainst(), amount);
        }

        ItemStack inHand = e.getItemInHand().clone();
        inHand.setAmount(amount);
        ItemUtil.removeItem(inHand, e);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockUnstack(BlockBreakEvent e){
        int blockAmount = plugin.getGrid().getBlockAmount(e.getBlock());

        if(blockAmount <= 1)
            return;

        e.setCancelled(true);

        // When sneaking, you'll break 64 from the stack. Otherwise, 1.
        int amount = !e.getPlayer().isSneaking() ? 1 : 64, leftAmount;

        // Fix amount so it won't be more than the stack's amount
        amount = Math.min(amount, blockAmount);

        plugin.getGrid().setBlockAmount(e.getBlock(), (leftAmount = blockAmount - amount));

        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
        if(island != null){
            island.handleBlockBreak(e.getBlock(), amount);
        }

        ItemStack blockItem = e.getBlock().getState().getData().toItemStack(amount);

        // If the amount of the stack is less than 0, it should be air.
        if(leftAmount <= 0){
            e.getBlock().setType(Material.AIR);
        }

        // Dropping the item
        e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), blockItem);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent e){
        List<Block> blockList = new ArrayList<>(e.blockList());
        ItemStack blockItem;

        for(Block block : blockList){
            // Check if block is stackable
            if(!plugin.getSettings().whitelistedStackedBlocks.contains(block.getType().name()))
                continue;

            int amount = plugin.getGrid().getBlockAmount(block);

            if(amount <= 1)
                continue;

            // All checks are done. We can remove the block from the list.
            e.blockList().remove(block);

            blockItem = block.getState().getData().toItemStack(amount);

            plugin.getGrid().setBlockAmount(block, 0);
            block.setType(Material.AIR);

            // Dropping the item
            block.getWorld().dropItemNaturally(block.getLocation(), blockItem);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e){
        for(Block block : e.getBlocks()){
            if(plugin.getGrid().getBlockAmount(block) > 1) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e){
        for(Block block : e.getBlocks()){
            if(plugin.getGrid().getBlockAmount(block) > 1) {
                e.setCancelled(true);
                break;
            }
        }
    }

    /*
     *  Island Warps
     */

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSignPlace(SignChangeEvent e){
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if(island == null)
            return;

        if(e.getLine(0).equalsIgnoreCase("[IslandWarp]")){
            String warpName = e.getLine(1);

            if(warpName.replace(" ", "").isEmpty() || island.getWarpLocation(warpName) != null){
                Locale.WARP_ALREADY_EXIST.send(wrappedPlayer);
                e.setLine(0, "");
            }
            else {
                List<String> signWarp = plugin.getSettings().signWarp;
                for (int i = 0; i < signWarp.size(); i++)
                    e.setLine(i, signWarp.get(i));
                island.setWarpLocation(warpName, e.getBlock().getLocation());
                Locale.SET_WARP.send(wrappedPlayer, WrappedLocation.of(e.getBlock().getLocation()));
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSignBreak(SignBreakEvent e){
        WrappedPlayer wrappedPlayer = e.getPlayer();
        Sign sign = e.getSign();
        Island island = plugin.getGrid().getIslandAt(sign.getLocation());

        if(island == null || !isWarpSign(sign.getLines()))
            return;

        island.deleteWarp(wrappedPlayer, sign.getLocation());
    }

    private boolean isWarpSign(String[] lines){
        List<String> warpLines = plugin.getSettings().signWarp;

        for(int i = 0; i < lines.length; i++) {
            if (!lines[i].equals(warpLines.get(i)))
                return false;
        }

        return true;
    }

}

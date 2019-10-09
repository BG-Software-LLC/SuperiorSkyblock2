package com.bgsoftware.superiorskyblock.listeners;

import  com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.listeners.events.DragonEggChangeEvent;
import com.bgsoftware.superiorskyblock.listeners.events.SignBreakEvent;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.utils.items.ItemUtils;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public final class BlocksListener implements Listener {

    private SuperiorSkyblockPlugin plugin;

    public BlocksListener(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlaceMonitor(BlockPlaceEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if(island != null)
            island.handleBlockPlace(e.getBlockPlaced());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreakMonitor(BlockBreakEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if(island != null)
            island.handleBlockBreak(e.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getLocation());

        List<BlockState> blockStates = new ArrayList<>(e.getBlocks());

        if(island != null)
            blockStates.forEach(blockState -> {
                if(!island.isInsideRange(blockState.getLocation())){
                    e.getBlocks().remove(blockState);
                }
                else {
                    island.handleBlockPlace(Key.of(blockState.getData().toItemStack()), 1);
                }
            });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if(island != null)
            island.handleBlockBreak(Key.of(e.getBlock()), 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDragonEggChangeMonitor(DragonEggChangeEvent e){
        if(plugin != null && plugin.getGrid() != null) {
            Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

            if (island != null) {
                Executor.sync(() -> island.handleBlockPlace(e.getBlock(), 1), 1L);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFromToMonitor(BlockFromToEvent e){
        if(plugin != null && plugin.getGrid() != null) {
            Island island = plugin.getGrid().getIslandAt(e.getToBlock().getLocation());

            if (island != null) {
                Executor.sync(() -> island.handleBlockPlace(e.getToBlock(), 1), 1L);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplodeMonitor(EntityExplodeEvent e){
        if(plugin != null && plugin.getGrid() != null) {
            for(Block block : e.blockList()){
                Island island = plugin.getGrid().getIslandAt(block.getLocation());
                if(island != null)
                    island.handleBlockBreak(block, 1);
            }
        }
    }

    /*
     *  Stacked Blocks
     */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockStack(BlockPlaceEvent e){
        if(!plugin.getSettings().stackedBlocksEnabled)
            return;

        if(plugin.getGrid().getBlockAmount(e.getBlock()) > 1)
            plugin.getGrid().setBlockAmount(e.getBlock(), 1);

        if(plugin.getSettings().stackedBlocksDisabledWorlds.contains(e.getBlock().getWorld().getName()))
            return;

        if(!plugin.getSettings().whitelistedStackedBlocks.contains(e.getBlock()))
            return;

        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());

        if(!superiorPlayer.hasBlocksStackerEnabled() || (!superiorPlayer.hasPermission("superior.island.stacker.*") &&
                !superiorPlayer.hasPermission("superior.island.stacker." + e.getBlock().getType())))
            return;

        //noinspection deprecation
        if(e.getBlockAgainst().getType() != e.getBlock().getType() || e.getBlockAgainst().getData() != e.getBlock().getData() ||
                e.getBlockReplacedState().getType() != Material.AIR)
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
        ItemUtils.removeItem(inHand, e);
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

    private Set<UUID> recentlyClicked = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockUnstack(PlayerInteractEvent e){
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getItem() != null ||
                recentlyClicked.contains(e.getPlayer().getUniqueId()))
            return;

        int blockAmount = plugin.getGrid().getBlockAmount(e.getClickedBlock());

        if(blockAmount <= 1)
            return;

        recentlyClicked.add(e.getPlayer().getUniqueId());
        Executor.sync(() -> recentlyClicked.remove(e.getPlayer().getUniqueId()), 5L);

        e.setCancelled(true);

        // When sneaking, you'll break 64 from the stack. Otherwise, 1.
        int amount = !e.getPlayer().isSneaking() ? 1 : 64, leftAmount;

        // Fix amount so it won't be more than the stack's amount
        amount = Math.min(amount, blockAmount);

        plugin.getGrid().setBlockAmount(e.getClickedBlock(), (leftAmount = blockAmount - amount));

        Island island = plugin.getGrid().getIslandAt(e.getClickedBlock().getLocation());
        if(island != null){
            island.handleBlockBreak(e.getClickedBlock(), amount);
        }

        ItemStack blockItem = e.getClickedBlock().getState().getData().toItemStack(amount);

        // If the amount of the stack is less than 0, it should be air.
        if(leftAmount <= 0){
            e.getClickedBlock().setType(Material.AIR);
        }

        // Dropping the item
        e.getClickedBlock().getWorld().dropItemNaturally(e.getClickedBlock().getLocation(), blockItem);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent e){
        List<Block> blockList = new ArrayList<>(e.blockList());
        ItemStack blockItem;

        for(Block block : blockList){
            // Check if block is stackable
            if(!plugin.getSettings().whitelistedStackedBlocks.contains(block))
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
        Executor.async(() -> {
            Map<Location, Integer> blocksToChange = new HashMap<>();
            Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
            for(Block block : e.getBlocks()){
                int blockAmount = plugin.getGrid().getBlockAmount(block);
                if(blockAmount > 1){
                    blocksToChange.put(block.getRelative(e.getDirection()).getLocation(), blockAmount);
                    blocksToChange.put(block.getLocation(), 0);
                }
            }

            Executor.sync(() ->
                    blocksToChange.forEach((key, value) -> plugin.getGrid().setBlockAmount(key.getBlock(), value)));
        }, 2L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e){
        Executor.async(() -> {
            Map<Location, Integer> blocksToChange = new HashMap<>();
            Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
            for(Block block : e.getBlocks()){
                int blockAmount = plugin.getGrid().getBlockAmount(block);
                if(blockAmount > 1){
                    blocksToChange.put(block.getRelative(e.getDirection()).getLocation(), blockAmount);
                    blocksToChange.put(block.getLocation(), 0);
                }
            }

            Executor.sync(() ->
                    blocksToChange.forEach((key, value) -> plugin.getGrid().setBlockAmount(key.getBlock(), value)));
        }, 2L);
    }

    /*
     *  Island Warps
     */

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSignPlace(SignChangeEvent e){
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if(island == null)
            return;

        Location warpLocation = e.getBlock().getLocation();
        warpLocation.setYaw(e.getPlayer().getLocation().getYaw());

        if(e.getLine(0).equalsIgnoreCase(plugin.getSettings().signWarpLine)){
            String warpName = e.getLine(1);
            boolean privateFlag = e.getLine(2).equalsIgnoreCase("private");

            if(warpName.replace(" ", "").isEmpty() || island.getWarpLocation(warpName) != null){
                Locale.WARP_ALREADY_EXIST.send(superiorPlayer);
                e.setLine(0, "");
            }
            else {
                List<String> signWarp = plugin.getSettings().signWarp;
                for (int i = 0; i < signWarp.size(); i++)
                    e.setLine(i, signWarp.get(i));
                island.setWarpLocation(warpName, warpLocation, privateFlag);
                Locale.SET_WARP.send(superiorPlayer, SBlockPosition.of(warpLocation));
            }
        }

        else if(e.getLine(0).equalsIgnoreCase(plugin.getSettings().welcomeWarpLine)){
            String description = e.getLine(1) + "\n" + e.getLine(2) + "\n" + e.getLine(3);
            String welcomeColor = ChatColor.getLastColors(plugin.getSettings().signWarp.get(0));
            e.setLine(0, welcomeColor + plugin.getSettings().welcomeWarpLine);
            for (int i = 1; i <= 3; i++)
                e.setLine(i, ChatColor.translateAlternateColorCodes('&', e.getLine(i)));
            island.setVisitorsLocation(warpLocation);
            island.setDescription(description);
            Locale.SET_WARP.send(superiorPlayer, SBlockPosition.of(warpLocation));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSignBreak(SignBreakEvent e){
        SuperiorPlayer superiorPlayer = e.getPlayer();
        Sign sign = e.getSign();
        Island island = plugin.getGrid().getIslandAt(sign.getLocation());

        if(island == null)
            return;

        if(isWarpSign(sign.getLines())){
            island.deleteWarp(superiorPlayer, sign.getLocation());
        }
        else{
            String welcomeColor = ChatColor.getLastColors(plugin.getSettings().signWarp.get(0));
            if(sign.getLine(0).equalsIgnoreCase(welcomeColor + plugin.getSettings().welcomeWarpLine)){
                island.setVisitorsLocation(null);
                Locale.DELETE_WARP.send(superiorPlayer, SIsland.VISITORS_WARP_NAME);
            }
        }
    }

    private boolean isWarpSign(String[] lines){
        List<String> warpLines = plugin.getSettings().signWarp;

        for(int i = 0; i < lines.length; i++) {
            if (!lines[i].equals(warpLines.get(i)))
                return false;
        }

        return true;
    }

    /**
     * Adds a tree to the given lists.
     *
     * @param anchor anchor block
     * @param logs logs
     * @param leaves leaves
     */
    private void getTree(Block anchor, ArrayList<Block> logs, ArrayList<Block> leaves){
        // Limits:
        if(logs.size() > 150) return;

        Block nextAnchor;

        // North:
        nextAnchor = anchor.getRelative(BlockFace.NORTH);
        if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
            logs.add(nextAnchor);
            getTree(nextAnchor, logs, leaves);
        }
        else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
            leaves.add(nextAnchor);
        }

        // North-east:
        nextAnchor = anchor.getRelative(BlockFace.NORTH_EAST);
        if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
            logs.add(nextAnchor);
            getTree(nextAnchor, logs, leaves);
        }
        else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
            leaves.add(nextAnchor);
        }

        // East:
        nextAnchor = anchor.getRelative(BlockFace.EAST);
        if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
            logs.add(nextAnchor);
            getTree(nextAnchor, logs, leaves);
        }
        else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
            leaves.add(nextAnchor);
        }

        // South-east:
        nextAnchor = anchor.getRelative(BlockFace.SOUTH_EAST);
        if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
            logs.add(nextAnchor);
            getTree(nextAnchor, logs, leaves);
        }
        else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
            leaves.add(nextAnchor);
        }

        // South:
        nextAnchor = anchor.getRelative(BlockFace.SOUTH);
        if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
            logs.add(nextAnchor);
            getTree(nextAnchor, logs, leaves);
        }
        else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
            leaves.add(nextAnchor);
        }

        // South-west:
        nextAnchor = anchor.getRelative(BlockFace.SOUTH_WEST);
        if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
            logs.add(nextAnchor);
            getTree(nextAnchor, logs, leaves);
        }
        else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
            leaves.add(nextAnchor);
        }

        // West:
        nextAnchor = anchor.getRelative(BlockFace.WEST);
        if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
            logs.add(nextAnchor);
            getTree(nextAnchor, logs, leaves);
        }
        else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
            leaves.add(nextAnchor);
        }

        // North-west:
        nextAnchor = anchor.getRelative(BlockFace.NORTH_WEST);
        if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
            logs.add(nextAnchor);
            getTree(nextAnchor, logs, leaves);
        }
        else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
            leaves.add(nextAnchor);
        }

        // Shift anchor one up:
        anchor = anchor.getRelative(BlockFace.UP);

        // Up-north:
        nextAnchor = anchor.getRelative(BlockFace.NORTH);
        if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
            logs.add(nextAnchor);
            getTree(nextAnchor, logs, leaves);
        }
        else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
            leaves.add(nextAnchor);
        }

        // Up-north-east:
        nextAnchor = anchor.getRelative(BlockFace.NORTH_EAST);
        if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
            logs.add(nextAnchor);
            getTree(nextAnchor, logs, leaves);
        }
        else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
            leaves.add(nextAnchor);
        }

        // Up-east:
        nextAnchor = anchor.getRelative(BlockFace.EAST);
        if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
            logs.add(nextAnchor);
            getTree(nextAnchor, logs, leaves);
        }
        else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
            leaves.add(nextAnchor);
        }

        // Up-south-east:
        nextAnchor = anchor.getRelative(BlockFace.SOUTH_EAST);
        if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
            logs.add(nextAnchor);
            getTree(nextAnchor, logs, leaves);
        }
        else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
            leaves.add(nextAnchor);
        }

        // Up-south:
        nextAnchor = anchor.getRelative(BlockFace.SOUTH);
        if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
            logs.add(nextAnchor);
            getTree(nextAnchor, logs, leaves);
        }
        else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
            leaves.add(nextAnchor);
        }

        // Up-south-west:
        nextAnchor = anchor.getRelative(BlockFace.SOUTH_WEST);
        if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
            logs.add(nextAnchor);
            getTree(nextAnchor, logs, leaves);
        }
        else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
            leaves.add(nextAnchor);
        }

        // Up-west:
        nextAnchor = anchor.getRelative(BlockFace.WEST);
        if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
            logs.add(nextAnchor);
            getTree(nextAnchor, logs, leaves);
        }
        else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
            leaves.add(nextAnchor);
        }

        // Up-north-west:
        nextAnchor = anchor.getRelative(BlockFace.NORTH_WEST);
        if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
            logs.add(nextAnchor);
            getTree(nextAnchor, logs, leaves);
        }
        else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
            leaves.add(nextAnchor);
        }

        // Up:
        nextAnchor = anchor.getRelative(BlockFace.SELF);
        if(nextAnchor.getType().equals(Material.LOG) && !logs.contains(nextAnchor)){
            logs.add(nextAnchor);
            getTree(nextAnchor, logs, leaves);
        }
        else if(nextAnchor.getType().equals(Material.LEAVES) && !logs.contains(nextAnchor)){
            leaves.add(nextAnchor);
        }
    }

}

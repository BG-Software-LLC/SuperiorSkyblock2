package com.bgsoftware.superiorskyblock.listeners;

import  com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.listeners.events.SignBreakEvent;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import com.bgsoftware.superiorskyblock.utils.items.ItemUtils;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.player.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public final class BlocksListener implements Listener {

    private final SuperiorSkyblockPlugin plugin;

    public BlocksListener(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlaceMonitor(BlockPlaceEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if(island != null) {
            if(e.getBlockReplacedState().getType().name().contains("LAVA"))
                island.handleBlockBreak(Key.of("LAVA"), 1);
            else if(e.getBlockReplacedState().getType().name().contains("WATER"))
                island.handleBlockBreak(Key.of("WATER"), 1);
            island.handleBlockPlace(e.getBlockPlaced());

            ChunksTracker.markDirty(island, e.getBlock(), true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketEmptyMonitor(PlayerBucketEmptyEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlockClicked().getLocation());

        if(island == null)
            return;

        island.handleBlockPlace(Key.of(e.getBucket().name().replace("_BUCKET", "")), 1);

        ChunksTracker.markDirty(island, LocationUtils.getRelative(e.getBlockClicked().getLocation(), e.getBlockFace()), true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlaceWhileRecalculated(BlockPlaceEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
        if(island != null && island.isBeingRecalculated()){
            e.setCancelled(true);
            Locale.ISLAND_BEING_CALCULATED.send(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreakMonitor(BlockBreakEvent e){
        handleBlockBreak(plugin, e.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketFillMonitor(PlayerBucketFillEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlockClicked().getLocation());

        if(island == null)
            return;

        island.handleBlockBreak(Key.of(e.getBucket().name().replace("_BUCKET", "")), 1);

        Executor.sync(() -> {
            Location location = LocationUtils.getRelative(e.getBlockClicked().getLocation(), e.getBlockFace());
            if(plugin.getNMSAdapter().isChunkEmpty(location.getChunk()))
                ChunksTracker.markEmpty(island, location, true);
        }, 2L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreakWhileRecalculated(BlockBreakEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
        if(island != null && island.isBeingRecalculated()){
            e.setCancelled(true);
            Locale.ISLAND_BEING_CALCULATED.send(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDragonEggDrop(ItemSpawnEvent e){
        if(e.getEntity().getItemStack().getType() == Material.DRAGON_EGG){
            for(Entity nearby : e.getEntity().getNearbyEntities(2, 2, 2)){
                if(nearby instanceof FallingBlock){
                    FallingBlock fallingBlock = (FallingBlock) nearby;
                    Island island = plugin.getGrid().getIslandAt(fallingBlock.getLocation());
                    if(island != null)
                        island.handleBlockBreak(Key.of(fallingBlock.getMaterial(), (byte) 0), 1);
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDragonEggDrop(BlockPistonExtendEvent e){
        for(Block block : e.getBlocks()){
            if(block.getType() == Material.DRAGON_EGG){
                Island island = plugin.getGrid().getIslandAt(block.getLocation());
                if(island != null)
                    island.handleBlockBreak(Key.of("DRAGON_EGG"), 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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

    //Checking for chorus flower spread outside island.
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent e){
        Island sourceIsland = plugin.getGrid().getIslandAt(e.getSource().getLocation());
        if(sourceIsland != null && !sourceIsland.isInsideRange(e.getBlock().getLocation()))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeavesDecay(LeavesDecayEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if(island != null)
            island.handleBlockBreak(Key.of(e.getBlock()), 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFromToMonitor(BlockFormEvent e){
        if(plugin != null && plugin.getGrid() != null) {
            Island island = plugin.getGrid().getIslandAt(e.getNewState().getLocation());

            if (island != null) {
                Executor.async(() -> island.handleBlockPlace(Key.of(e.getNewState()), 1, false), 1L);
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityChangeBlockMonitor(EntityChangeBlockEvent e){
        if(plugin != null && plugin.getGrid() != null) {
            Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
            if(island != null) {
                island.handleBlockBreak(e.getBlock(), 1);
                if(e.getTo() != Material.AIR)
                    //noinspection deprecation
                    island.handleBlockPlace(Key.of(e.getTo(), e.getData()), 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockRedstone(BlockRedstoneEvent e){
        if(!plugin.getSettings().disableRedstoneOffline)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if(island == null || island.isSpawn() || island.getLastTimeUpdate() == -1)
            return;

        e.setNewCurrent(0);
    }

    /*
     *  Stacked Blocks
     */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockStack(PlayerInteractEvent e){
        if(e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.DRAGON_EGG){
            if(plugin.getGrid().getBlockAmount(e.getClickedBlock()) > 1) {
                e.setCancelled(true);
                if(e.getItem() == null)
                    tryUnstack(e.getPlayer(), e.getClickedBlock(), plugin);
            }

            if(e.getItem() != null && canStackBlocks(e.getPlayer(), e.getItem(), e.getClickedBlock(), null) &&
                    tryStack(e.getPlayer(), e.getItem(), e.getClickedBlock(), e)){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockStack(BlockPlaceEvent e){
        if(plugin.getGrid().getBlockAmount(e.getBlock()) > 1)
            plugin.getGrid().setBlockAmount(e.getBlock(), 1);

        if(!canStackBlocks(e.getPlayer(), e.getItemInHand(), e.getBlockAgainst(), e.getBlockReplacedState()))
            return;

        if(tryStack(e.getPlayer(), e.getItemInHand(), e.getBlockAgainst(), e))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockUnstack(BlockBreakEvent e){
        if(tryUnstack(e.getPlayer(), e.getBlock(), plugin))
            e.setCancelled(true);
    }

    private final Set<UUID> recentlyClicked = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockUnstack(PlayerInteractEvent e){
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getItem() != null ||
                recentlyClicked.contains(e.getPlayer().getUniqueId()))
            return;

        recentlyClicked.add(e.getPlayer().getUniqueId());
        Executor.sync(() -> recentlyClicked.remove(e.getPlayer().getUniqueId()), 5L);

        if(tryUnstack(e.getPlayer(), e.getClickedBlock(), plugin))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockUnstack(EntityChangeBlockEvent e){
        if(tryUnstack(null, e.getBlock(), plugin))
            e.setCancelled(true);
    }

    private boolean canStackBlocks(Player player, ItemStack placeItem, Block againstBlock, BlockState replaceState){
        if(!plugin.getSettings().stackedBlocksEnabled)
            return false;

        if(plugin.getSettings().stackedBlocksDisabledWorlds.contains(againstBlock.getWorld().getName()))
            return false;

        if(againstBlock.getType().name().equals("GLOWING_REDSTONE_ORE"))
            againstBlock.setType(Material.REDSTONE_ORE);


        //noinspection deprecation
        byte blockData = againstBlock.getData();
        Material blockType = againstBlock.getType();

        if(blockType.name().contains("PORTAL_FRAME")) {
            blockData = 0;
        }

        else if(placeItem.getType().name().equals("CAULDRON_ITEM")){
            blockType = Material.valueOf("CAULDRON_ITEM");
        }

        if(blockType != placeItem.getType() || blockData != placeItem.getDurability() ||
                (replaceState != null && replaceState.getType() != Material.AIR))
            return false;

        if(!plugin.getSettings().whitelistedStackedBlocks.contains(againstBlock))
            return false;

        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(player);

        if(!superiorPlayer.hasBlocksStackerEnabled() || (!superiorPlayer.hasPermission("superior.island.stacker.*") &&
                !superiorPlayer.hasPermission("superior.island.stacker." + placeItem.getType())))
            return false;

        return true;
    }

    private boolean tryStack(Player player, ItemStack placeItem, Block againstBlock, Event event){
        // When sneaking, you'll stack all the items in your hand. Otherwise, you'll stack only 1 block
        int amount = !player.isSneaking() ? 1 : placeItem.getAmount();
        int blockAmount = plugin.getGrid().getBlockAmount(againstBlock);
        int blockLimit = plugin.getSettings().stackedBlocksLimits.getOrDefault(Key.of(againstBlock), Integer.MAX_VALUE);

        if(amount + blockAmount > blockLimit){
            amount = blockLimit - blockAmount;
        }

        if(amount <= 0)
            return false;

        plugin.getGrid().setBlockAmount(againstBlock, blockAmount + amount);

        Island island = plugin.getGrid().getIslandAt(againstBlock.getLocation());
        if(island != null){
            island.handleBlockPlace(againstBlock, amount);
        }

        ItemStack inHand = placeItem.clone();
        inHand.setAmount(amount);
        ItemUtils.removeItem(inHand, event, player);

        return true;
    }

    public static boolean tryUnstack(Player player, Block block, SuperiorSkyblockPlugin plugin){
        int blockAmount = plugin.getGrid().getBlockAmount(block);

        if(blockAmount <= 1)
            return false;

        // When sneaking, you'll break 64 from the stack. Otherwise, 1.
        int amount = player == null || !player.isSneaking() ? 1 : 64, leftAmount;

        // Fix amount so it won't be more than the stack's amount
        amount = Math.min(amount, blockAmount);

        plugin.getGrid().setBlockAmount(block, (leftAmount = blockAmount - amount));

        ItemStack blockItem = block.getState().getData().toItemStack(amount);

        if(blockItem.getType().name().equals("GLOWING_REDSTONE_ORE")) {
            blockItem.setType(Material.REDSTONE_ORE);
        }

        else if(ServerVersion.isLegacy() && blockItem.getType().name().equals("CAULDRON")) {
            blockItem.setType(Material.valueOf("CAULDRON_ITEM"));
        }

        Island island = plugin.getGrid().getIslandAt(block.getLocation());
        if(island != null){
            island.handleBlockBreak(Key.of(blockItem), amount);
        }

        // If the amount of the stack is less than 0, it should be air.
        if(leftAmount <= 0){
            block.setType(Material.AIR);
        }

        // Dropping the item
        if(player != null && plugin.getSettings().stackedBlocksAutoPickup){
            ItemUtils.addItem(blockItem, player.getInventory(), block.getLocation());
        }
        else {
            block.getWorld().dropItemNaturally(block.getLocation(), blockItem);
        }

        return true;
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
        int chunkX = e.getBlock().getX() >> 4, chunkZ = e.getBlock().getZ() >> 4;
        World world = e.getBlock().getWorld();
        Executor.async(() -> {
            if(!world.isChunkLoaded(chunkX, chunkZ))
                return;

            Registry<Location, Integer> blocksToChange = Registry.createRegistry();
            Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
            for(Block block : e.getBlocks()){
                int blockAmount = plugin.getGrid().getBlockAmount(block);
                if(blockAmount > 1){
                    blocksToChange.add(block.getRelative(e.getDirection()).getLocation(), blockAmount);
                    blocksToChange.add(block.getLocation(), 0);
                }
            }

            Executor.sync(() -> {
                blocksToChange.entries().forEach(entry -> plugin.getGrid().setBlockAmount(entry.getKey().getBlock(), entry.getValue()));
                blocksToChange.delete();
            });
        }, 2L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e){
        List<Location> locations = e.getBlocks().stream().map(Block::getLocation).collect(Collectors.toList());
        Executor.async(() -> {
            Registry<Location, Integer> blocksToChange = Registry.createRegistry();
            Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
            for(Location location : locations){
                int blockAmount = plugin.getGrid().getBlockAmount(location);
                if(blockAmount > 1){
                    blocksToChange.add(getRelative(location, e.getDirection()), blockAmount);
                    blocksToChange.add(location, 0);
                }
            }

            Executor.sync(() -> {
                blocksToChange.entries().forEach(entry -> plugin.getGrid().setBlockAmount(entry.getKey().getBlock(), entry.getValue()));
                blocksToChange.delete();
            });
        }, 2L);
    }

    private Location getRelative(Location location, BlockFace blockFace){
        switch (blockFace){
            case NORTH:
                return location.clone().subtract(0, 0, 1);
            case SOUTH:
                return location.clone().add(0, 0, 1);
            case WEST:
                return location.clone().subtract(1, 0, 0);
            case EAST:
                return location.clone().add(1, 0, 0);
            case DOWN:
                return location.clone().subtract(0, 1, 0);
            case UP:
                return location.clone().add(0, 1, 0);
            default:
                return location;
        }
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
            if (!island.hasMoreWarpSlots()) {
                Locale.NO_MORE_WARPS.send(superiorPlayer);
                for (int i = 0; i < 4; i++)
                    e.setLine(i, "");
                return;
            }

            String warpName = e.getLine(1);
            boolean privateFlag = e.getLine(2).equalsIgnoreCase("private");

            if(warpName.replace(" ", "").isEmpty() || island.getWarpLocation(warpName) != null){
                Locale.WARP_ALREADY_EXIST.send(superiorPlayer);
                for (int i = 0; i < 4; i++)
                    e.setLine(i, "");
            }
            else {
                List<String> signWarp = plugin.getSettings().signWarp;
                for (int i = 0; i < signWarp.size(); i++)
                    e.setLine(i, signWarp.get(i));
                island.setWarpLocation(warpName, warpLocation, privateFlag);
                Locale.SET_WARP.send(superiorPlayer, SBlockPosition.of(warpLocation));
            }
        }

        else if(e.getLine(0).equalsIgnoreCase(plugin.getSettings().visitorsSignLine)){
            if (!island.hasMoreWarpSlots()) {
                Locale.NO_MORE_WARPS.send(superiorPlayer);
                for (int i = 0; i < 4; i++)
                    e.setLine(i, "");
                return;
            }

            StringBuilder descriptionBuilder = new StringBuilder();

            for(int i = 1; i < 4; i++){
                String line = e.getLine(i);
                if(!line.isEmpty())
                    descriptionBuilder.append("\n").append(ChatColor.RESET).append(line);
            }

            String description = descriptionBuilder.length() < 1 ? "" : descriptionBuilder.substring(1);

            e.setLine(0, plugin.getSettings().visitorsSignActive);

            for (int i = 1; i <= 3; i++)
                e.setLine(i, StringUtils.translateColors(e.getLine(i)));

            Block oldWelcomeSignBlock = island.getVisitorsLocation() == null ? null : island.getVisitorsLocation().getBlock();
            if(oldWelcomeSignBlock != null && oldWelcomeSignBlock.getType().name().contains("SIGN")) {
                Sign oldWelcomeSign = (Sign) oldWelcomeSignBlock.getState();
                oldWelcomeSign.setLine(0, plugin.getSettings().visitorsSignInactive);
                oldWelcomeSign.update();
            }

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
            if(sign.getLine(0).equalsIgnoreCase(plugin.getSettings().visitorsSignActive)){
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

    public static void handleBlockBreak(SuperiorSkyblockPlugin plugin, Block block){
        Island island = plugin.getGrid().getIslandAt(block.getLocation());

        if(island == null)
            return;

        island.handleBlockBreak(block);

        Executor.sync(() -> {
            if(plugin.getNMSAdapter().isChunkEmpty(block.getChunk()))
                ChunksTracker.markEmpty(island, block, true);
        }, 2L);
    }

}

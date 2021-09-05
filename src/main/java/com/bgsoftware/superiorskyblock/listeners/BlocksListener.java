package com.bgsoftware.superiorskyblock.listeners;

import  com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.menu.StackedBlocksDepositMenu;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import com.bgsoftware.superiorskyblock.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.utils.logic.BlocksLogic;
import com.bgsoftware.superiorskyblock.utils.logic.StackedBlocksLogic;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
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

@SuppressWarnings("unused")
public final class BlocksListener implements Listener {

    private final SuperiorSkyblockPlugin plugin;

    private final Set<UUID> recentlyClicked = new HashSet<>();

    public BlocksListener(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
        if(plugin.getSettings().isPhysicsListener())
            Bukkit.getPluginManager().registerEvents(new PhysicsListener(), plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlaceMonitor(BlockPlaceEvent e){
        BlocksLogic.handlePlace(e.getBlock(), e.getBlockReplacedState());
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
        BlocksLogic.handleBreak(e.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketFillMonitor(PlayerBucketFillEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlockClicked().getLocation());

        if(island == null)
            return;

        Material blockType = e.getBlockClicked().getType();
        boolean isWaterLogged = plugin.getNMSWorld().isWaterLogged(e.getBlockClicked());

        if(!blockType.name().contains("WATER") && !blockType.name().contains("LAVA") && !isWaterLogged)
            return;

        Key blockKey = isWaterLogged ? ConstantKeys.WATER : Key.of(e.getBlockClicked());

        island.handleBlockBreak(blockKey, 1);

        Executor.sync(() -> {
            Location location = LocationUtils.getRelative(e.getBlockClicked().getLocation(), e.getBlockFace());
            if(plugin.getNMSChunks().isChunkEmpty(location.getChunk()))
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
                    island.handleBlockBreak(ConstantKeys.DRAGON_EGG, 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStructureGrow(StructureGrowEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getLocation());

        List<BlockState> blockStates = new ArrayList<>(e.getBlocks());

        if(island != null) {
            blockStates.forEach(blockState -> {
                if (!island.isInsideRange(blockState.getLocation())) {
                    e.getBlocks().remove(blockState);
                } else {
                    island.handleBlockPlace(Key.of(blockState), 1);
                }
            });
        }
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
    public void onBlockFromMonitor(BlockFormEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getNewState().getLocation());

        if (island != null) {
            Executor.async(() -> island.handleBlockPlace(Key.of(e.getNewState()), 1, false), 1L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFromToMonitor(BlockFromToEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getToBlock().getLocation());
        if(island != null && e.getToBlock().getType() != Material.AIR)
            BlocksLogic.handleBreak(e.getToBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplodeMonitor(EntityExplodeEvent e){
        for(Block block : e.blockList()){
            Island island = plugin.getGrid().getIslandAt(block.getLocation());
            if(island != null)
                island.handleBlockBreak(block, 1);
        }
        if(e.getEntity() instanceof TNTPrimed){
            Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());
            if(island != null)
                island.handleBlockBreak(ConstantKeys.TNT, 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityChangeBlockMonitor(EntityChangeBlockEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if(island == null)
            return;

        island.handleBlockBreak(e.getBlock(), 1);

        if(e.getTo() != Material.AIR) {
            byte data = 0;

            try{
                //noinspection deprecation
                data = e.getData();
            }catch (Throwable ignored){}

            island.handleBlockPlace(Key.of(e.getTo(), data), 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockRedstone(BlockRedstoneEvent e){
        if(!plugin.getSettings().isDisableRedstoneOffline() && !plugin.getSettings().getAFKIntegrations().isDisableRedstone())
            return;

        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if(island == null || island.isSpawn())
            return;

        if((plugin.getSettings().isDisableRedstoneOffline() && island.getLastTimeUpdate() != -1) ||
                (plugin.getSettings().getAFKIntegrations().isDisableRedstone() &&
                        island.getAllPlayersInside().stream().allMatch(SuperiorPlayer::isAFK))) {
            e.setNewCurrent(0);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntitySpawn(CreatureSpawnEvent e){
        if(!plugin.getSettings().getAFKIntegrations().isDisableSpawning())
            return;

        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if(island == null || island.isSpawn() || !island.getAllPlayersInside().stream().allMatch(SuperiorPlayer::isAFK))
            return;

        e.setCancelled(true);
    }

    /*
     *  Stacked Blocks
     */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockStack(PlayerInteractEvent e){
        if(e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.DRAGON_EGG){
            if(plugin.getStackedBlocks().getStackedBlockAmount(e.getClickedBlock()) > 1) {
                e.setCancelled(true);
                if(e.getItem() == null)
                    StackedBlocksLogic.tryUnstack(e.getPlayer(), e.getClickedBlock(), plugin);
            }

            if(e.getItem() != null && StackedBlocksLogic.canStackBlocks(e.getPlayer(), e.getItem(), e.getClickedBlock(), null) &&
                    StackedBlocksLogic.tryStack(e.getPlayer(), e.getItem(), e.getClickedBlock().getLocation(), e)){
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockStack(BlockPlaceEvent e){
        if(plugin.getStackedBlocks().getStackedBlockAmount(e.getBlock()) > 1)
            plugin.getStackedBlocks().setStackedBlock(e.getBlock(), 1);

        if(!StackedBlocksLogic.canStackBlocks(e.getPlayer(), e.getItemInHand(), e.getBlockAgainst(), e.getBlockReplacedState()))
            return;

        if(StackedBlocksLogic.tryStack(e.getPlayer(), e.getItemInHand(), e.getBlockAgainst().getLocation(), e))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockUnstack(BlockBreakEvent e){
        if(StackedBlocksLogic.tryUnstack(e.getPlayer(), e.getBlock(), plugin))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockUnstack(PlayerInteractEvent e){
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getItem() != null ||
                recentlyClicked.contains(e.getPlayer().getUniqueId()))
            return;

        if(plugin.getSettings().getStackedBlocks().getDepositMenu().isEnabled() && e.getPlayer().isSneaking() &&
                plugin.getStackedBlocks().getStackedBlockAmount(e.getClickedBlock()) > 1){
            StackedBlocksDepositMenu depositMenu = new StackedBlocksDepositMenu(e.getClickedBlock().getLocation());
            e.getPlayer().openInventory(depositMenu.getInventory());
        }
        else {
            recentlyClicked.add(e.getPlayer().getUniqueId());
            Executor.sync(() -> recentlyClicked.remove(e.getPlayer().getUniqueId()), 5L);

            if (StackedBlocksLogic.tryUnstack(e.getPlayer(), e.getClickedBlock(), plugin))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockUnstack(EntityChangeBlockEvent e){
        if(StackedBlocksLogic.tryUnstack(null, e.getBlock(), plugin))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent e){
        List<Block> blockList = new ArrayList<>(e.blockList());
        ItemStack blockItem;

        for(Block block : blockList){
            // Check if block is stackable
            if(!plugin.getSettings().getStackedBlocks().getWhitelisted().contains(Key.of(block)))
                continue;

            int amount = plugin.getStackedBlocks().getStackedBlockAmount(block);

            if(amount <= 1)
                continue;

            // All checks are done. We can remove the block from the list.
            e.blockList().remove(block);

            blockItem = block.getState().getData().toItemStack(amount);

            Location location = block.getLocation();

            Island island = plugin.getGrid().getIslandAt(location);
            if(island != null)
                island.handleBlockBreak(block, amount);

            plugin.getStackedBlocks().removeStackedBlock(location);
            block.setType(Material.AIR);

            // Dropping the item
            block.getWorld().dropItemNaturally(location, blockItem);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e){
        for(Block block : e.getBlocks()) {
            if (plugin.getStackedBlocks().getStackedBlockAmount(block) > 1) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e){
        for(Block block : e.getBlocks()) {
            if (plugin.getStackedBlocks().getStackedBlockAmount(block) > 1) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onGolemCreate(CreatureSpawnEvent e){
        if(!e.getSpawnReason().name().contains("BUILD"))
            return;

        List<Location> blocksToCheck = new ArrayList<>();
        Location entityLocation = e.getEntity().getLocation();

        if(e.getEntity() instanceof IronGolem){
            blocksToCheck.add(entityLocation.clone());
            blocksToCheck.add(entityLocation.clone().add(0, 1, 0));
            blocksToCheck.add(entityLocation.clone().add(1, 1, 0));
            blocksToCheck.add(entityLocation.clone().add(1, 1, 1));
            blocksToCheck.add(entityLocation.clone().add(-1, 1, 0));
            blocksToCheck.add(entityLocation.clone().add(-1, 1, -1));
            blocksToCheck.add(entityLocation.clone().add(0, 2, 0));
        }

        else if(e.getEntity() instanceof Snowman){
            blocksToCheck.add(entityLocation.clone());
            blocksToCheck.add(entityLocation.clone().add(0, 1, 0));
            blocksToCheck.add(entityLocation.clone().add(0, 2, 0));
        }


        else if(e.getEntity() instanceof Wither){
            blocksToCheck.add(entityLocation.clone());
            blocksToCheck.add(entityLocation.clone().add(0, 1, 0));
            blocksToCheck.add(entityLocation.clone().add(1, 1, 0));
            blocksToCheck.add(entityLocation.clone().add(1, 1, 1));
            blocksToCheck.add(entityLocation.clone().add(-1, 1, 0));
            blocksToCheck.add(entityLocation.clone().add(-1, 1, -1));
            blocksToCheck.add(entityLocation.clone().add(0, 2, 0));
            blocksToCheck.add(entityLocation.clone().add(1, 2, 0));
            blocksToCheck.add(entityLocation.clone().add(1, 2, 1));
            blocksToCheck.add(entityLocation.clone().add(-1, 2, 0));
            blocksToCheck.add(entityLocation.clone().add(-1, 2, -1));
        }

        if(blocksToCheck.stream().anyMatch(location -> plugin.getStackedBlocks().getStackedBlockAmount(location) > 1))
            e.setCancelled(true);
    }

    /*
     *  Island Warps
     */

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSignPlace(SignChangeEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
        if(island != null) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
            String[] signLines = e.getLines();
            BlocksLogic.handleSignPlace(superiorPlayer, island, e.getBlock().getLocation(), signLines, true);
            // In 1.16.5+ of Paper, the SignChangeEvent doesn't have the lines array of the signs.
            // Therefore, we manually need to set them.
            plugin.getNMSWorld().setSignLines(e, signLines);
        }
    }

    private final class PhysicsListener implements Listener {

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onStackedBlockPhysics(BlockPhysicsEvent e){
            if(plugin.getStackedBlocks().getStackedBlockAmount(e.getBlock()) > 1)
                e.setCancelled(true);
        }

    }

}

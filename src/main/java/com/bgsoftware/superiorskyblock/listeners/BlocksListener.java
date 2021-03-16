package com.bgsoftware.superiorskyblock.listeners;

import  com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_MergedSpawner;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_RoseStacker;
import com.bgsoftware.superiorskyblock.hooks.CoreProtectHook;
import com.bgsoftware.superiorskyblock.menu.StackedBlocksDepositMenu;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunksTracker;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemUtils;
import com.bgsoftware.superiorskyblock.utils.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Wither;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public final class BlocksListener implements Listener {

    private static final BlockFace[] NEARBY_BLOCKS = new BlockFace[] {
            BlockFace.UP, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH, BlockFace.EAST
    };

    public static BlocksListener IMP;
    private final SuperiorSkyblockPlugin plugin;

    public BlocksListener(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
        IMP = this;
        if(plugin.getSettings().physicsListener)
            Bukkit.getPluginManager().registerEvents(new PhysicsListener(), plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlaceMonitor(BlockPlaceEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if(island != null) {
            if(e.getBlockReplacedState().getType().name().contains("LAVA"))
                island.handleBlockBreak(ConstantKeys.LAVA, 1);
            else if(e.getBlockReplacedState().getType().name().contains("WATER"))
                island.handleBlockBreak(ConstantKeys.WATER, 1);

            Key blockKey = Key.of(e.getBlockPlaced());
            if(!blockKey.getGlobalKey().contains("SPAWNER") || (!BlocksProvider_MergedSpawner.isRegistered() &&
                    !BlocksProvider_RoseStacker.isRegistered()))
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

        island.handleBlockBreak(Key.of(e.getBlockClicked()), 1);

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
                    island.handleBlockBreak(ConstantKeys.DRAGON_EGG, 1);
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
                if(e.getTo() != Material.AIR) {
                    byte data = 0;

                    try{
                        //noinspection deprecation
                        data = e.getData();
                    }catch (Throwable ignored){}

                    island.handleBlockPlace(Key.of(e.getTo(), data), 1);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockRedstone(BlockRedstoneEvent e){
        if(!plugin.getSettings().disableRedstoneOffline && !plugin.getSettings().disableRedstoneAFK)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if(island == null || island.isSpawn())
            return;

        if((plugin.getSettings().disableRedstoneOffline && island.getLastTimeUpdate() != -1) ||
                (plugin.getSettings().disableRedstoneAFK && island.getAllPlayersInside().stream().allMatch(SuperiorPlayer::isAFK))) {
            e.setNewCurrent(0);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntitySpawn(CreatureSpawnEvent e){
        if(!plugin.getSettings().disableSpawningAFK)
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
            if(plugin.getGrid().getBlockAmount(e.getClickedBlock()) > 1) {
                e.setCancelled(true);
                if(e.getItem() == null)
                    tryUnstack(e.getPlayer(), e.getClickedBlock(), plugin);
            }

            if(e.getItem() != null && canStackBlocks(e.getPlayer(), e.getItem(), e.getClickedBlock(), null) &&
                    tryStack(e.getPlayer(), e.getItem(), e.getClickedBlock().getLocation(), e)){
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

        if(tryStack(e.getPlayer(), e.getItemInHand(), e.getBlockAgainst().getLocation(), e))
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

        if(plugin.getSettings().stackedBlocksMenuEnabled && e.getPlayer().isSneaking() &&
                plugin.getGrid().getBlockAmount(e.getClickedBlock()) > 1){
            StackedBlocksDepositMenu depositMenu = new StackedBlocksDepositMenu(e.getClickedBlock().getLocation());
            e.getPlayer().openInventory(depositMenu.getInventory());
        }
        else {
            recentlyClicked.add(e.getPlayer().getUniqueId());
            Executor.sync(() -> recentlyClicked.remove(e.getPlayer().getUniqueId()), 5L);

            if (tryUnstack(e.getPlayer(), e.getClickedBlock(), plugin))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockUnstack(EntityChangeBlockEvent e){
        if(tryUnstack(null, e.getBlock(), plugin))
            e.setCancelled(true);
    }

    public boolean canStackBlocks(Player player, ItemStack placeItem, Block againstBlock, BlockState replaceState){
        if(!plugin.getSettings().stackedBlocksEnabled)
            return false;

        if(plugin.getSettings().stackedBlocksDisabledWorlds.contains(againstBlock.getWorld().getName()))
            return false;

        if(placeItem.hasItemMeta() && (placeItem.getItemMeta().hasDisplayName() || placeItem.getItemMeta().hasLore()))
            return false;

        if(againstBlock.getType().name().equals("GLOWING_REDSTONE_ORE"))
            againstBlock.setType(Material.REDSTONE_ORE);

        //noinspection deprecation
        byte blockData = againstBlock.getData();
        Material blockType = againstBlock.getType();

        if(blockType.name().contains("PORTAL_FRAME")) {
            blockData = 0;
        }

        else if(blockType == Material.CAULDRON && placeItem.getType().name().equals("CAULDRON_ITEM")){
            blockType = Material.valueOf("CAULDRON_ITEM");
        }

        if(blockType != placeItem.getType() || blockData != placeItem.getDurability() ||
                (replaceState != null && replaceState.getType() != Material.AIR))
            return false;

        if(!plugin.getSettings().whitelistedStackedBlocks.contains(againstBlock))
            return false;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);

        if(!superiorPlayer.hasBlocksStackerEnabled() || (!superiorPlayer.hasPermission("superior.island.stacker.*") &&
                !superiorPlayer.hasPermission("superior.island.stacker." + placeItem.getType())))
            return false;

        return true;
    }

    private boolean tryStack(Player player, ItemStack itemToDeposit, Location stackedBlock, Event event){
        return tryStack(plugin, player, !player.isSneaking() ? 1 : itemToDeposit.getAmount(), stackedBlock, depositedAmount -> {
            if(player.getGameMode() != GameMode.CREATIVE) {
                ItemStack inHand = itemToDeposit.clone();
                inHand.setAmount(depositedAmount);
                ItemUtils.removeItem(inHand, event, player);
            }
        });
    }

    public static boolean tryStack(SuperiorSkyblockPlugin plugin, Player player, int amount, Location stackedBlock, Consumer<Integer> depositedAmount){
        // When sneaking, you'll stack all the items in your hand. Otherwise, you'll stack only 1 block
        int blockAmount = plugin.getGrid().getBlockAmount(stackedBlock);
        Key blockKey = plugin.getGrid().getBlockKey(stackedBlock);
        int blockLimit = plugin.getSettings().stackedBlocksLimits.getOrDefault(blockKey, Integer.MAX_VALUE);

        if(amount + blockAmount > blockLimit){
            amount = blockLimit - blockAmount;
        }

        if(amount <= 0) {
            depositedAmount.accept(0);
            return false;
        }

        Block block = stackedBlock.getBlock();

        if(!EventsCaller.callBlockStackEvent(block, player, blockAmount, blockAmount + amount)) {
            depositedAmount.accept(0);
            return false;
        }

        Island island = plugin.getGrid().getIslandAt(stackedBlock);

        if(island != null){
            BigInteger islandBlockLimit = BigInteger.valueOf(island.getExactBlockLimit(blockKey));
            BigInteger islandBlockCount = island.getBlockCountAsBigInteger(blockKey);
            BigInteger bigAmount = BigInteger.valueOf(amount);

            //Checking for the specific provided key.
            if(islandBlockLimit.compareTo(BigInteger.valueOf(IslandUtils.NO_LIMIT.get())) > 0 &&
                    islandBlockCount.add(bigAmount).compareTo(islandBlockLimit) > 0) {
                amount = islandBlockLimit.subtract(islandBlockCount).intValue();
            }
            else{
                //Getting the global key values.
                Key globalKey = Key.of(blockKey.getGlobalKey());
                islandBlockLimit = BigInteger.valueOf(island.getExactBlockLimit(globalKey));
                islandBlockCount = island.getBlockCountAsBigInteger(globalKey);
                if(islandBlockLimit.compareTo(BigInteger.valueOf(IslandUtils.NO_LIMIT.get())) > 0 &&
                        islandBlockCount.add(bigAmount).compareTo(islandBlockLimit) > 0) {
                    amount = islandBlockLimit.subtract(islandBlockCount).intValue();
                }
            }
        }

        plugin.getGrid().setBlockAmount(block, blockAmount + amount);

        if(plugin.getGrid().hasBlockFailed()) {
            depositedAmount.accept(0);
            return false;
        }

        if(island != null){
            island.handleBlockPlace(block, amount);
        }

        CoreProtectHook.recordBlockChange(player, block, true);

        depositedAmount.accept(amount);

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

        if(!EventsCaller.callBlockUnstackEvent(block, player, blockAmount, blockAmount - amount))
            return false;

        Island island = plugin.getGrid().getIslandAt(block.getLocation());

        plugin.getGrid().setBlockAmount(block, (leftAmount = blockAmount - amount));

        plugin.getNMSAdapter().playBreakAnimation(block);

        CoreProtectHook.recordBlockChange(player, block, false);

        if(plugin.getGrid().hasBlockFailed()) {
            if(island != null)
                island.handleBlockBreak(Key.of(block), blockAmount - 1);
            leftAmount = 0;
            amount = 1;
        }

        ItemStack blockItem = ServerVersion.isLegacy() ? block.getState().getData().toItemStack(amount) :
                new ItemStack(block.getType(), amount);

        if(blockItem.getType().name().equals("GLOWING_REDSTONE_ORE")) {
            blockItem.setType(Material.REDSTONE_ORE);
        }

        else if(ServerVersion.isLegacy() && blockItem.getType().name().equals("CAULDRON")) {
            blockItem.setType(Material.valueOf("CAULDRON_ITEM"));
        }

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

            Island island = plugin.getGrid().getIslandAt(block.getLocation());
            if(island != null)
                island.handleBlockBreak(block, amount);

            plugin.getGrid().setBlockAmount(block, 0);
            block.setType(Material.AIR);

            // Dropping the item
            block.getWorld().dropItemNaturally(block.getLocation(), blockItem);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e){
        for(Block block : e.getBlocks()) {
            if (plugin.getGrid().getBlockAmount(block) > 1) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e){
        for(Block block : e.getBlocks()) {
            if (plugin.getGrid().getBlockAmount(block) > 1) {
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

        if(blocksToCheck.stream().anyMatch(location -> plugin.getGrid().getBlockAmount(location) > 1))
            e.setCancelled(true);
    }

    /*
     *  Island Warps
     */

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSignPlace(SignChangeEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
        if(island != null)
            onSignPlace(plugin.getPlayers().getSuperiorPlayer(e.getPlayer()), island, e.getBlock().getLocation(), e.getLines(), true);
    }

    public boolean onSignPlace(SuperiorPlayer superiorPlayer, Island island, Location warpLocation, String[] lines, boolean message){
        assert superiorPlayer.getLocation() != null;
        warpLocation.setYaw(superiorPlayer.getLocation().getYaw());

        if(lines[0].equalsIgnoreCase(plugin.getSettings().signWarpLine)){
            if (island.getIslandWarps().size() >= island.getWarpsLimit()) {
                if(message)
                    Locale.NO_MORE_WARPS.send(superiorPlayer);
                for (int i = 0; i < 4; i++)
                    lines[i] = "";
                return true;
            }

            String warpName = IslandUtils.getWarpName(StringUtils.stripColors(lines[1].trim()));
            boolean privateFlag = lines[2].equalsIgnoreCase("private");

            if(warpName.isEmpty() || island.getWarp(warpName) != null){
                if(message) {
                    if(warpName.isEmpty())
                        Locale.WARP_ILLEGAL_NAME.send(superiorPlayer);
                    else
                        Locale.WARP_ALREADY_EXIST.send(superiorPlayer);
                }

                for (int i = 0; i < 4; i++) {
                    lines[i] = "";
                }
            }
            else {
                List<String> signWarp = plugin.getSettings().signWarp;

                for (int i = 0; i < signWarp.size(); i++)
                    lines[i] = signWarp.get(i).replace("{0}", warpName);

                IslandWarp islandWarp = island.createWarp(warpName, warpLocation, null);
                islandWarp.setPrivateFlag(privateFlag);
                if(message)
                    Locale.SET_WARP.send(superiorPlayer, SBlockPosition.of(warpLocation));
            }

            return true;
        }

        else if(lines[0].equalsIgnoreCase(plugin.getSettings().visitorsSignLine)){
            if (island.getIslandWarps().size() >= island.getWarpsLimit()) {
                if(message)
                    Locale.NO_MORE_WARPS.send(superiorPlayer);
                for (int i = 0; i < 4; i++)
                    lines[i] = "";
                return true;
            }

            StringBuilder descriptionBuilder = new StringBuilder();

            for(int i = 1; i < 4; i++){
                String line = lines[i];
                if(!line.isEmpty())
                    descriptionBuilder.append("\n").append(ChatColor.RESET).append(line);
            }

            String description = descriptionBuilder.length() < 1 ? "" : descriptionBuilder.substring(1);

            lines[0] = plugin.getSettings().visitorsSignActive;

            for (int i = 1; i <= 3; i++)
                lines[i] = StringUtils.translateColors(lines[i]);

            Block oldWelcomeSignBlock = island.getVisitorsLocation() == null ? null : island.getVisitorsLocation().getBlock();
            if(oldWelcomeSignBlock != null && oldWelcomeSignBlock.getType().name().contains("SIGN")) {
                Sign oldWelcomeSign = (Sign) oldWelcomeSignBlock.getState();
                oldWelcomeSign.setLine(0, plugin.getSettings().visitorsSignInactive);
                oldWelcomeSign.update();
            }

            island.setVisitorsLocation(warpLocation);
            island.setDescription(description);
            if(message)
                Locale.SET_WARP.send(superiorPlayer, SBlockPosition.of(warpLocation));

            return true;
        }

        return false;
    }

    public void onSignBreak(Player player, Sign sign){
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);
        Island island = plugin.getGrid().getIslandAt(sign.getLocation());

        if(island == null)
            return;

        if(island.getWarp(sign.getLocation()) != null){
            island.deleteWarp(superiorPlayer, sign.getLocation());
        }
        else{
            String welcomeColor = ChatColor.getLastColors(plugin.getSettings().signWarp.get(0));
            if(sign.getLine(0).equalsIgnoreCase(plugin.getSettings().visitorsSignActive)){
                island.setVisitorsLocation(null);
                Locale.DELETE_WARP.send(superiorPlayer, IslandUtils.VISITORS_WARP_NAME);
            }
        }
    }

    public static void handleBlockBreak(SuperiorSkyblockPlugin plugin, Block block){
        Island island = plugin.getGrid().getIslandAt(block.getLocation());

        if(island == null)
            return;

        island.handleBlockBreak(block);

        EnumMap<BlockFace, Key> nearbyBlocks = new EnumMap<>(BlockFace.class);

        for(BlockFace nearbyFace : NEARBY_BLOCKS){
            Key nearbyBlock = Key.of(block.getRelative(nearbyFace));
            if(!nearbyBlock.getGlobalKey().equals("AIR"))
                nearbyBlocks.put(nearbyFace, nearbyBlock);
        }

        Executor.sync(() -> {
            if(plugin.getNMSAdapter().isChunkEmpty(block.getChunk()))
                ChunksTracker.markEmpty(island, block, true);

            for(BlockFace nearbyFace : NEARBY_BLOCKS){
                Key nearbyBlock = Key.of(block.getRelative(nearbyFace));
                Key oldNearbyBlock = nearbyBlocks.getOrDefault(nearbyFace, ConstantKeys.AIR);
                if(oldNearbyBlock != ConstantKeys.AIR && !nearbyBlock.equals(oldNearbyBlock))
                    island.handleBlockBreak(oldNearbyBlock, 1);
            }
        }, 2L);
    }

    private final class PhysicsListener implements Listener {

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onStackedBlockPhysics(BlockPhysicsEvent e){
            if(plugin.getGrid().getBlockAmount(e.getBlock()) > 1)
                e.setCancelled(true);
        }

    }

}

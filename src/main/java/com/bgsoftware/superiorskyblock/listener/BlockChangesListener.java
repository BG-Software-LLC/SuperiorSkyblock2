package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.collections.AutoRemovalCollection;
import com.bgsoftware.superiorskyblock.core.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.core.key.KeyImpl;
import com.bgsoftware.superiorskyblock.core.key.KeyMapImpl;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import com.bgsoftware.superiorskyblock.world.chunk.ChunksTracker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

public class BlockChangesListener implements Listener {

    private static final ReflectMethod<EquipmentSlot> INTERACT_GET_HAND = new ReflectMethod<>(
            PlayerInteractEvent.class, "getHand");
    private static final ReflectMethod<Block> PROJECTILE_HIT_EVENT_TARGET_BLOCK = new ReflectMethod<>(
            ProjectileHitEvent.class, "getHitBlock");
    @Nullable
    private static final Material CHORUS_FLOWER = Materials.getMaterialSafe("CHORUS_FLOWER");

    private static final BlockFace[] NEARBY_BLOCKS = new BlockFace[]{
            BlockFace.UP, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH, BlockFace.EAST
    };

    private final SuperiorSkyblockPlugin plugin;

    public BlockChangesListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        this.registerSpongeListener();
    }

    public enum Flag {

        DIRTY_CHUNK,
        SAVE_BLOCK_COUNT,
        HANDLE_NEARBY_BLOCKS

    }

    /* BLOCK PLACES */

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onBlockPlace(BlockPlaceEvent e) {
        onBlockPlace(KeyImpl.of(e.getBlock()), e.getBlock().getLocation(), 1, e.getBlockReplacedState(),
                Flag.DIRTY_CHUNK, Flag.SAVE_BLOCK_COUNT);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onBucketEmpty(PlayerBucketEmptyEvent e) {
        Key blockKey = KeyImpl.of(e.getBucket().name().replace("_BUCKET", ""));
        onBlockPlace(blockKey, e.getBlockClicked().getLocation(), 1, null,
                Flag.DIRTY_CHUNK, Flag.SAVE_BLOCK_COUNT);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onStructureGrow(StructureGrowEvent e) {
        KeyMap<Integer> blockCounts = KeyMapImpl.createHashMap();
        e.getBlocks().forEach(blockState -> {
            Key blockKey = KeyImpl.of(blockState);
            blockCounts.put(blockKey, blockCounts.getOrDefault(blockKey, 0) + 1);
        });
        onMultiBlockPlace(blockCounts, e.getLocation(), Flag.DIRTY_CHUNK);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onBlockGrow(BlockGrowEvent e) {
        onBlockPlace(KeyImpl.of(e.getNewState()), e.getBlock().getLocation(), 1, null,
                Flag.DIRTY_CHUNK, Flag.SAVE_BLOCK_COUNT);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onBlockFrom(BlockFormEvent e) {
        Location location = e.getNewState().getLocation();
        BukkitExecutor.sync(() -> {
            // Do not save block counts
            onBlockBreak(KeyImpl.of(e.getNewState()), location, 1, Flag.DIRTY_CHUNK);
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onMinecartPlace(PlayerInteractEvent e) {
        Material handItemType;

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || // Making sure right-clicking block
                e.getItem() == null || // Making sure right-clicking with valid item
                !Materials.isRail(e.getClickedBlock().getType()) || // Making sure clicked block is a rail
                !Materials.isMinecart((handItemType = e.getItem().getType())) || // Making sure hand item is minecart
                (INTERACT_GET_HAND.isValid() && INTERACT_GET_HAND.invoke(e) != EquipmentSlot.HAND) // Making sure only call onces, for main hand.
        )
            return;

        Key blockKey;

        switch (handItemType.name()) {
            case "HOPPER_MINECART":
                blockKey = ConstantKeys.HOPPER;
                break;
            case "COMMAND_MINECART":
            case "COMMAND_BLOCK_MINECART":
                blockKey = ServerVersion.isAtLeast(ServerVersion.v1_13) ?
                        ConstantKeys.COMMAND_BLOCK : ConstantKeys.COMMAND;
                break;
            case "EXPLOSIVE_MINECART":
            case "TNT_MINECART":
                blockKey = ConstantKeys.TNT;
                break;
            case "POWERED_MINECART":
            case "FURNACE_MINECART":
                blockKey = ConstantKeys.FURNACE;
                break;
            case "STORAGE_MINECART":
            case "CHEST_MINECART":
                blockKey = ConstantKeys.CHEST;
                break;
            default:
                return;
        }

        onBlockPlace(blockKey, e.getClickedBlock().getLocation(), 1, null, Flag.DIRTY_CHUNK, Flag.SAVE_BLOCK_COUNT);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onEntityChangeBlock(EntityChangeBlockEvent e) {
        Key blockKey;

        if (ServerVersion.isLegacy()) {
            // noinspection deprecated
            blockKey = KeyImpl.of(e.getTo(), e.getData());
        } else {
            blockKey = KeyImpl.of(e.getTo(), (byte) 0);
        }

        onBlockPlace(blockKey, e.getBlock().getLocation(), 1, e.getBlock().getState(), Flag.SAVE_BLOCK_COUNT);
    }

    public void onBlockPlace(Key blockKey, Location blockLocation, int blockCount,
                             @Nullable BlockState oldBlockState, Flag... flags) {
        Island island = plugin.getGrid().getIslandAt(blockLocation);

        if (island == null)
            return;

        EnumSet<Flag> flagsSet = flags.length == 0 ? EnumSet.noneOf(Flag.class) : EnumSet.copyOf(Arrays.asList(flags));

        if (oldBlockState != null && oldBlockState.getType() != Material.AIR) {
            Material blockStateType = oldBlockState.getType();
            Key oldBlockKey;
            int oldBlockCount = 1;

            if (Materials.isLava(blockStateType)) {
                oldBlockKey = ConstantKeys.LAVA;
            } else if (Materials.isWater(blockStateType)) {
                oldBlockKey = ConstantKeys.WATER;
            } else {
                oldBlockKey = KeyImpl.of(oldBlockState);
                oldBlockCount = plugin.getNMSWorld().getDefaultAmount(oldBlockState.getBlock());
            }

            internalBlockBreak(island, oldBlockKey, blockLocation, oldBlockCount, flagsSet);
        }

        if (blockKey.equals(ConstantKeys.END_PORTAL_FRAME_WITH_EYE))
            internalBlockBreak(island, ConstantKeys.END_PORTAL_FRAME, blockLocation, 1, flagsSet);

        if (!blockKey.getGlobalKey().contains("SPAWNER") || plugin.getProviders().shouldListenToSpawnerChanges())
            island.handleBlockPlace(blockKey, blockCount, flagsSet.contains(Flag.SAVE_BLOCK_COUNT));

        if (flagsSet.contains(Flag.DIRTY_CHUNK))
            ChunksTracker.markDirty(island, blockLocation, true);
    }

    public void onMultiBlockPlace(KeyMap<Integer> blockCounts, Location location, Flag... flags) {
        if (!blockCounts.isEmpty()) {
            Island island = plugin.getGrid().getIslandAt(location);
            if (island != null) {
                island.handleBlocksPlace(blockCounts);
                EnumSet<Flag> flagsSet = flags.length == 0 ? EnumSet.noneOf(Flag.class) : EnumSet.copyOf(Arrays.asList(flags));
                if (flagsSet.contains(Flag.DIRTY_CHUNK))
                    ChunksTracker.markDirty(island, location, true);
            }
        }
    }

    /* BLOCK BREAKS */

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onBlockBreak(BlockBreakEvent e) {
        int blockCount = plugin.getNMSWorld().getDefaultAmount(e.getBlock());
        onBlockBreak(KeyImpl.of(e.getBlock()), e.getBlock().getLocation(), blockCount,
                Flag.HANDLE_NEARBY_BLOCKS, Flag.DIRTY_CHUNK, Flag.SAVE_BLOCK_COUNT);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onEntityBlockDeath(EntityDeathEvent e) {
        if (e.getEntity() instanceof FallingBlock) {
            Key blockKey = plugin.getNMSAlgorithms().getFallingBlockType((FallingBlock) e.getEntity());
            onBlockBreak(blockKey, e.getEntity().getLocation(), 1,
                    Flag.DIRTY_CHUNK, Flag.SAVE_BLOCK_COUNT);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onBucketFill(PlayerBucketFillEvent e) {
        boolean isWaterLogged = plugin.getNMSWorld().isWaterLogged(e.getBlockClicked());
        if (e.getBlockClicked().isLiquid() || isWaterLogged) {
            Key blockKey = isWaterLogged ? ConstantKeys.WATER : KeyImpl.of(e.getBlockClicked());
            onBlockBreak(blockKey, e.getBlockClicked().getLocation(), 1,
                    Flag.DIRTY_CHUNK, Flag.SAVE_BLOCK_COUNT);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onDragonEggDrop(ItemSpawnEvent e) {
        if (e.getEntity().getItemStack().getType() == Material.DRAGON_EGG) {
            for (Entity nearby : e.getEntity().getNearbyEntities(2, 2, 2)) {
                if (nearby instanceof FallingBlock) {
                    Key blockKey = plugin.getNMSAlgorithms().getFallingBlockType((FallingBlock) nearby);
                    onBlockBreak(blockKey, nearby.getLocation(), 1, Flag.SAVE_BLOCK_COUNT);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onDragonEggDrop(BlockPistonExtendEvent e) {
        for (Block block : e.getBlocks()) {
            if (block.getType() == Material.DRAGON_EGG) {
                onBlockBreak(ConstantKeys.DRAGON_EGG, block.getLocation(), 1,
                        Flag.DIRTY_CHUNK, Flag.SAVE_BLOCK_COUNT);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onLeavesDecay(LeavesDecayEvent e) {
        onBlockBreak(KeyImpl.of(e.getBlock()), e.getBlock().getLocation(), 1,
                Flag.DIRTY_CHUNK, Flag.SAVE_BLOCK_COUNT);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onBlockFromTo(BlockFromToEvent e) {
        if (e.getToBlock().getType() != Material.AIR)
            // Do not save block counts
            onBlockBreak(KeyImpl.of(e.getToBlock()), e.getToBlock().getLocation(), 1, Flag.DIRTY_CHUNK);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onEntityExplode(EntityExplodeEvent e) {
        KeyMap<Integer> blockCounts = KeyMapImpl.createHashMap();
        e.blockList().forEach(block -> {
            Key blockKey = KeyImpl.of(block);
            blockCounts.put(blockKey, blockCounts.getOrDefault(blockKey, 0) + 1);
        });
        if (e.getEntity() instanceof TNTPrimed)
            blockCounts.put(ConstantKeys.TNT, blockCounts.getOrDefault(ConstantKeys.TNT, 0) + 1);
        onMultiBlockBreak(blockCounts, e.getLocation(), Flag.DIRTY_CHUNK, Flag.SAVE_BLOCK_COUNT);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onMinecartBreak(VehicleDestroyEvent e) {
        if (e.getVehicle() instanceof Minecart) {
            Key blockKey = plugin.getNMSAlgorithms().getMinecartBlock((Minecart) e.getVehicle());
            onBlockBreak(blockKey, e.getVehicle().getLocation(), 1, Flag.DIRTY_CHUNK, Flag.SAVE_BLOCK_COUNT);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onChorusHit(ProjectileHitEvent e) {
        if (ServerVersion.isLessThan(ServerVersion.v1_18) && PROJECTILE_HIT_EVENT_TARGET_BLOCK.isValid()) {
            BukkitEntities.getPlayerSource(e.getEntity()).ifPresent(shooter -> {
                Block hitBlock = PROJECTILE_HIT_EVENT_TARGET_BLOCK.invoke(e);
                if (hitBlock != null && hitBlock.getType() == CHORUS_FLOWER) {
                    onBlockBreak(ConstantKeys.CHORUS_FLOWER, hitBlock.getLocation(), 1, Flag.DIRTY_CHUNK, Flag.SAVE_BLOCK_COUNT);
                }
            });
        }
    }

    public void onBlockBreak(Key blockKey, Location blockLocation, int blockCount, Flag... flags) {
        Island island = plugin.getGrid().getIslandAt(blockLocation);
        if (island != null) {
            EnumSet<Flag> flagsSet = flags.length == 0 ? EnumSet.noneOf(Flag.class) : EnumSet.copyOf(Arrays.asList(flags));
            internalBlockBreak(island, blockKey, blockLocation, blockCount, flagsSet);
        }
    }

    private void internalBlockBreak(Island island, Key blockKey, Location blockLocation, int blockCount, EnumSet<Flag> flags) {
        if (!blockKey.getGlobalKey().contains("SPAWNER") || plugin.getProviders().shouldListenToSpawnerChanges())
            island.handleBlockBreak(blockKey, blockCount, flags.contains(Flag.SAVE_BLOCK_COUNT));

        boolean handleNearbyBlocks = flags.contains(Flag.HANDLE_NEARBY_BLOCKS);
        boolean dirtyChunk = flags.contains(Flag.DIRTY_CHUNK);

        if (handleNearbyBlocks || dirtyChunk) {
            EnumMap<BlockFace, Key> nearbyBlocks = new EnumMap<>(BlockFace.class);
            Block block = blockLocation.getBlock();

            if (handleNearbyBlocks) {
                for (BlockFace nearbyFace : NEARBY_BLOCKS) {
                    Block nearbyBlock = block.getRelative(nearbyFace);
                    if (!nearbyBlock.getType().isSolid()) {
                        Key nearbyBlockKey = KeyImpl.of(nearbyBlock);
                        if (!nearbyBlockKey.getGlobalKey().equals("AIR"))
                            nearbyBlocks.put(nearbyFace, nearbyBlockKey);
                    }
                }
            }

            BukkitExecutor.sync(() -> {
                if (dirtyChunk) {
                    if (plugin.getNMSChunks().isChunkEmpty(block.getChunk()))
                        ChunksTracker.markEmpty(island, block, true);
                }
                if (handleNearbyBlocks) {
                    for (BlockFace nearbyFace : NEARBY_BLOCKS) {
                        Key nearbyBlock = KeyImpl.of(block.getRelative(nearbyFace));
                        Key oldNearbyBlock = nearbyBlocks.getOrDefault(nearbyFace, ConstantKeys.AIR);
                        if (oldNearbyBlock != ConstantKeys.AIR && !nearbyBlock.equals(oldNearbyBlock)) {
                            island.handleBlockBreak(oldNearbyBlock, 1);
                        }
                    }
                }
            }, 2L);
        }
    }

    public void onMultiBlockBreak(KeyMap<Integer> blockCounts, Location location, Flag... flags) {
        if (!blockCounts.isEmpty()) {
            Island island = plugin.getGrid().getIslandAt(location);
            if (island != null) {
                EnumSet<Flag> flagsSet = flags.length == 0 ? EnumSet.noneOf(Flag.class) : EnumSet.copyOf(Arrays.asList(flags));
                boolean saveBlockCounts = flagsSet.contains(Flag.SAVE_BLOCK_COUNT);
                blockCounts.forEach((blockKey, blockCount) -> island.handleBlockBreak(blockKey, blockCount, saveBlockCounts));
                if (flagsSet.contains(Flag.DIRTY_CHUNK))
                    ChunksTracker.markDirty(island, location, true);
            }
        }
    }

    /* INTERNAL */

    private void registerSpongeListener() {
        try {
            Class.forName("org.bukkit.event.block.SpongeAbsorbEvent");
            Bukkit.getPluginManager().registerEvents(new SpongeAbsorbListener(), plugin);
        } catch (Throwable ignored) {
        }
    }

    private class SpongeAbsorbListener implements Listener {

        private final Collection<Location> alreadySpongeAbosrbCalled = AutoRemovalCollection.newArrayList(5L * 50, TimeUnit.MILLISECONDS);

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpongeAbsorb(org.bukkit.event.block.SpongeAbsorbEvent e) {
            Location location = e.getBlock().getLocation();

            if (alreadySpongeAbosrbCalled.contains(location))
                return;

            onBlockPlace(ConstantKeys.WET_SPONGE, location, 1, e.getBlock().getState(), Flag.SAVE_BLOCK_COUNT);
            alreadySpongeAbosrbCalled.add(location);
        }

    }

}

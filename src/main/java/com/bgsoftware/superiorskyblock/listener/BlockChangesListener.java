package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.common.annotations.IntType;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.service.world.WorldRecordFlags;
import com.bgsoftware.superiorskyblock.api.service.world.WorldRecordService;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.PlayerHand;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.collections.AutoRemovalCollection;
import com.bgsoftware.superiorskyblock.core.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.KeyMaps;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.nms.bridge.PistonPushReaction;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import com.bgsoftware.superiorskyblock.world.BukkitItems;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
import org.bukkit.event.block.BlockDispenseEvent;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Directional;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class BlockChangesListener implements Listener {

    private static final ReflectMethod<Block> PROJECTILE_HIT_EVENT_TARGET_BLOCK = new ReflectMethod<>(
            ProjectileHitEvent.class, "getHitBlock");
    @Nullable
    private static final Material CHORUS_FLOWER = EnumHelper.getEnum(Material.class, "CHORUS_FLOWER");

    @WorldRecordFlags
    private static final int REGULAR_RECORD_FLAGS = WorldRecordFlags.SAVE_BLOCK_COUNT | WorldRecordFlags.DIRTY_CHUNKS;
    @WorldRecordFlags
    private static final int ALL_RECORD_FLAGS = REGULAR_RECORD_FLAGS | WorldRecordFlags.HANDLE_NEARBY_BLOCKS;

    private final LazyReference<WorldRecordService> worldRecordService = new LazyReference<WorldRecordService>() {
        @Override
        protected WorldRecordService create() {
            return plugin.getServices().getService(WorldRecordService.class);
        }
    };
    private final SuperiorSkyblockPlugin plugin;

    public BlockChangesListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        this.registerSpongeListener();
        this.registerBlockDestroyListener();
    }

    @IntType({BlockTrackFlags.DIRTY_CHUNKS, BlockTrackFlags.SAVE_BLOCK_COUNT, BlockTrackFlags.HANDLE_NEARBY_BLOCKS})
    public @interface BlockTrackFlags {

        int DIRTY_CHUNKS = (1 << 0);
        int SAVE_BLOCK_COUNT = (1 << 1);
        int HANDLE_NEARBY_BLOCKS = (1 << 2);

    }

    /* BLOCK PLACES */

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onBlockPlace(BlockPlaceEvent e) {
        boolean shouldAvoidReplacedState = e.getBlockReplacedState().equals(e.getBlock().getState());
        this.worldRecordService.get().recordBlockPlace(Keys.of(e.getBlock()),
                e.getBlock().getLocation(), plugin.getNMSWorld().getDefaultAmount(e.getBlock()),
                shouldAvoidReplacedState ? null : e.getBlockReplacedState(),
                REGULAR_RECORD_FLAGS);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onBucketEmpty(PlayerBucketEmptyEvent e) {
        Key blockKey = Keys.ofMaterialAndData(e.getBucket().name().replace("_BUCKET", ""));
        this.worldRecordService.get().recordBlockPlace(blockKey, e.getBlockClicked().getLocation(), 1,
                null, REGULAR_RECORD_FLAGS);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onStructureGrow(StructureGrowEvent e) {
        KeyMap<Integer> blockCounts = KeyMaps.createArrayMap(KeyIndicator.MATERIAL);
        e.getBlocks().forEach(blockState -> {
            Key blockKey = Keys.of(blockState);
            blockCounts.put(blockKey, blockCounts.getOrDefault(blockKey, 0) + 1);
        });
        this.worldRecordService.get().recordMultiBlocksPlace(blockCounts, e.getLocation(), WorldRecordFlags.DIRTY_CHUNKS);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onBlockGrow(BlockGrowEvent e) {
        this.worldRecordService.get().recordBlockPlace(Keys.of(e.getNewState()), e.getBlock().getLocation(),
                1, null, REGULAR_RECORD_FLAGS);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onBlockFrom(BlockFormEvent e) {
        Location location = e.getNewState().getLocation();
        // Do not save block counts
        this.worldRecordService.get().recordBlockBreak(Keys.of(e.getBlock()), location, 1, WorldRecordFlags.DIRTY_CHUNKS);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPlayerInteract(PlayerInteractEvent e) {
        onMinecartPlace(e);
        onSpawnerChange(e);
    }

    private void onMinecartPlace(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || !Materials.isRail(e.getClickedBlock().getType()))
            return;

        PlayerHand playerHand = BukkitItems.getHand(e);
        if (playerHand != PlayerHand.MAIN_HAND)
            return;

        ItemStack handItem = BukkitItems.getHandItem(e.getPlayer(), playerHand);

        if (handItem == null)
            return;

        Material handItemType = handItem.getType();
        if (!Materials.isMinecart(handItemType))
            return;

        Key minecartBlockKey = getMinecartBlockKey(handItemType);
        if (minecartBlockKey != null)
            this.worldRecordService.get().recordBlockPlace(minecartBlockKey, e.getClickedBlock().getLocation(),
                    1, null, REGULAR_RECORD_FLAGS);
    }

    private void onSpawnerChange(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK ||
                e.getClickedBlock().getType() != Materials.SPAWNER.toBukkitType())
            return;

        PlayerHand playerHand = BukkitItems.getHand(e);
        ItemStack handItem = BukkitItems.getHandItem(e.getPlayer(), playerHand);

        if (handItem == null)
            return;

        Material handItemType = handItem.getType();
        if (!Materials.isSpawnEgg(handItemType))
            return;

        Block block = e.getClickedBlock();
        Chunk chunk = block.getChunk();
        BlockState oldBlockState = block.getState();
        Key oldSpawnerKey = Keys.of(oldBlockState);

        BukkitExecutor.sync(() -> {
            if (!chunk.isLoaded())
                return;

            Key newSpawnerKey = Keys.of(block);
            if (!oldSpawnerKey.equals(newSpawnerKey)) {
                this.worldRecordService.get().recordBlockPlace(newSpawnerKey, block.getLocation(),
                        1, oldBlockState, REGULAR_RECORD_FLAGS);
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onMinecartPlaceByDispenser(BlockDispenseEvent e) {
        Material dispenseItemType = e.getItem().getType();

        if (!Materials.isMinecart(dispenseItemType) || e.getBlock().getType() != Material.DISPENSER)
            return;

        Block targetBlock = null;

        if (ServerVersion.isLegacy()) {
            MaterialData materialData = e.getBlock().getState().getData();
            if (materialData instanceof Directional) {
                targetBlock = e.getBlock().getRelative(((Directional) materialData).getFacing());
            }
        } else {
            Object blockData = plugin.getNMSWorld().getBlockData(e.getBlock());
            if (blockData instanceof org.bukkit.block.data.Directional) {
                targetBlock = e.getBlock().getRelative(((org.bukkit.block.data.Directional) blockData).getFacing());
            }
        }

        if (targetBlock == null)
            return;

        if (!Materials.isRail(targetBlock.getType()))
            return;

        Key minecartBlockKey = getMinecartBlockKey(dispenseItemType);
        if (minecartBlockKey != null)
            this.worldRecordService.get().recordBlockPlace(minecartBlockKey, targetBlock.getLocation(),
                    1, null, REGULAR_RECORD_FLAGS);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onEntityChangeBlock(EntityChangeBlockEvent e) {
        Key blockKey;

        if (ServerVersion.isLegacy()) {
            // noinspection deprecated
            blockKey = Keys.of(e.getTo(), e.getData());
        } else {
            blockKey = Keys.of(e.getTo(), (byte) 0);
        }

        this.worldRecordService.get().recordBlockPlace(blockKey, e.getBlock().getLocation(), 1,
                e.getBlock().getState(), WorldRecordFlags.SAVE_BLOCK_COUNT);
    }

    /* BLOCK BREAKS */

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onBlockBreak(BlockBreakEvent e) {
        this.worldRecordService.get().recordBlockBreak(e.getBlock(), ALL_RECORD_FLAGS);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onEntityBlockDeath(EntityDeathEvent e) {
        if (e.getEntity() instanceof FallingBlock) {
            Key blockKey = plugin.getNMSAlgorithms().getFallingBlockType((FallingBlock) e.getEntity());
            this.worldRecordService.get().recordBlockBreak(blockKey, e.getEntity().getLocation(),
                    1, REGULAR_RECORD_FLAGS);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onBucketFill(PlayerBucketFillEvent e) {
        boolean isWaterLogged = plugin.getNMSWorld().isWaterLogged(e.getBlockClicked());
        if (isWaterLogged || e.getBlockClicked().isLiquid()) {
            Key blockKey = isWaterLogged ? ConstantKeys.WATER : Keys.of(e.getBlockClicked());
            this.worldRecordService.get().recordBlockBreak(blockKey, e.getBlockClicked().getLocation(),
                    1, REGULAR_RECORD_FLAGS);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onDragonEggDrop(ItemSpawnEvent e) {
        if (e.getEntity().getItemStack().getType() == Material.DRAGON_EGG) {
            for (Entity nearby : e.getEntity().getNearbyEntities(2, 2, 2)) {
                if (nearby instanceof FallingBlock) {
                    Key blockKey = plugin.getNMSAlgorithms().getFallingBlockType((FallingBlock) nearby);
                    this.worldRecordService.get().recordBlockBreak(blockKey, nearby.getLocation(),
                            1, WorldRecordFlags.SAVE_BLOCK_COUNT);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPistonExtend(BlockPistonExtendEvent e) {
        for (Block block : e.getBlocks()) {
            if (plugin.getNMSWorld().getPistonReaction(block) == PistonPushReaction.DESTROY) {
                this.worldRecordService.get().recordBlockBreak(block, 1, REGULAR_RECORD_FLAGS);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onLeavesDecay(LeavesDecayEvent e) {
        this.worldRecordService.get().recordBlockBreak(e.getBlock(), 1, REGULAR_RECORD_FLAGS);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onBlockFromTo(BlockFromToEvent e) {
        if (e.getToBlock().getType() != Material.AIR) {
            // Do not save block counts
            this.worldRecordService.get().recordBlockBreak(e.getToBlock(), 1, WorldRecordFlags.DIRTY_CHUNKS);
        } else {
            BukkitExecutor.sync(() -> {
                // Do not save block counts
                this.worldRecordService.get().recordBlockPlace(e.getToBlock(), 1, null, WorldRecordFlags.DIRTY_CHUNKS);
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onEntityExplode(EntityExplodeEvent e) {
        KeyMap<Integer> blockCounts = KeyMaps.createArrayMap(KeyIndicator.MATERIAL);
        e.blockList().forEach(block -> {
            Key blockKey = Keys.of(block);
            blockCounts.put(blockKey, blockCounts.getOrDefault(blockKey, 0) + 1);
        });

        if (e.getEntity() instanceof TNTPrimed)
            blockCounts.put(ConstantKeys.TNT, blockCounts.getOrDefault(ConstantKeys.TNT, 0) + 1);

        this.worldRecordService.get().recordMultiBlocksBreak(blockCounts, e.getLocation(), REGULAR_RECORD_FLAGS);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onMinecartBreak(VehicleDestroyEvent e) {
        if (e.getVehicle() instanceof Minecart) {
            Key blockKey = plugin.getNMSAlgorithms().getMinecartBlock((Minecart) e.getVehicle());
            this.worldRecordService.get().recordBlockBreak(blockKey, e.getVehicle().getLocation(),
                    1, REGULAR_RECORD_FLAGS);
            e.getVehicle().setMetadata("SSB-VehicleDestory", new FixedMetadataValue(plugin, true));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onChorusHit(ProjectileHitEvent e) {
        if (ServerVersion.isLessThan(ServerVersion.v1_18) && PROJECTILE_HIT_EVENT_TARGET_BLOCK.isValid()) {
            BukkitEntities.getPlayerSource(e.getEntity()).ifPresent(shooter -> {
                Block hitBlock = PROJECTILE_HIT_EVENT_TARGET_BLOCK.invoke(e);
                if (hitBlock != null && hitBlock.getType() == CHORUS_FLOWER) {
                    this.worldRecordService.get().recordBlockBreak(hitBlock, 1, REGULAR_RECORD_FLAGS);
                }
            });
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

    private void registerBlockDestroyListener() {
        try {
            Class.forName("com.destroystokyo.paper.event.block.BlockDestroyEvent");
            Bukkit.getPluginManager().registerEvents(new BlockDestoryListener(), plugin);
        } catch (Throwable ignored) {
        }
    }

    @Nullable
    private static Key getMinecartBlockKey(Material minecartType) {
        switch (minecartType.name()) {
            case "HOPPER_MINECART":
                return ConstantKeys.HOPPER;
            case "COMMAND_MINECART":
            case "COMMAND_BLOCK_MINECART":
                return ConstantKeys.COMMAND_BLOCK;
            case "EXPLOSIVE_MINECART":
            case "TNT_MINECART":
                return ConstantKeys.TNT;
            case "POWERED_MINECART":
            case "FURNACE_MINECART":
                return ConstantKeys.FURNACE;
            case "STORAGE_MINECART":
            case "CHEST_MINECART":
                return ConstantKeys.CHEST;
        }

        return null;
    }

    private class SpongeAbsorbListener implements Listener {

        private final Collection<Location> alreadySpongeAbosrbCalled = AutoRemovalCollection.newArrayList(5L * 50, TimeUnit.MILLISECONDS);

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpongeAbsorb(org.bukkit.event.block.SpongeAbsorbEvent e) {
            Location location = e.getBlock().getLocation();

            if (alreadySpongeAbosrbCalled.contains(location))
                return;

            worldRecordService.get().recordBlockPlace(ConstantKeys.WET_SPONGE, location, 1,
                    e.getBlock().getState(), WorldRecordFlags.SAVE_BLOCK_COUNT);
            alreadySpongeAbosrbCalled.add(location);
        }

    }

    private class BlockDestoryListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBlockDestroy(com.destroystokyo.paper.event.block.BlockDestroyEvent e) {
            if (e.getNewState().getMaterial() != Material.AIR)
                return;

            worldRecordService.get().recordBlockBreak(e.getBlock(), WorldRecordFlags.DIRTY_CHUNKS);
        }

    }

}

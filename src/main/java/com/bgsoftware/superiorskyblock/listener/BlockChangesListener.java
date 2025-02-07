package com.bgsoftware.superiorskyblock.listener;

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
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.PlayerHand;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.collections.AutoRemovalCollection;
import com.bgsoftware.superiorskyblock.core.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.key.map.KeyMaps;
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
import org.bukkit.entity.EntityType;
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
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class BlockChangesListener implements Listener {

    private static final ReflectMethod<Block> PROJECTILE_HIT_EVENT_TARGET_BLOCK = new ReflectMethod<>(
            ProjectileHitEvent.class, "getHitBlock");
    @Nullable
    private static final Material CHORUS_FLOWER = EnumHelper.getEnum(Material.class, "CHORUS_FLOWER");
    @Nullable
    private static final EntityType WIND_CHARGE = EnumHelper.getEnum(EntityType.class, "WIND_CHARGE");
    @Nullable
    private static final EntityType BREEZE_WIND_CHARGE = EnumHelper.getEnum(EntityType.class, "BREEZE_WIND_CHARGE");
    @Nullable
    private static final Material POINTED_DRIPSTONE = EnumHelper.getEnum(Material.class, "POINTED_DRIPSTONE");

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

    /* BLOCK PLACES */

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onBlockPlace(BlockPlaceEvent e) {
        boolean shouldAvoidReplacedState = e.getBlockReplacedState().equals(e.getBlock().getState());
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            this.worldRecordService.get().recordBlockPlace(Keys.of(e.getBlock()),
                    e.getBlock().getLocation(wrapper.getHandle()),
                    plugin.getNMSWorld().getDefaultAmount(e.getBlock()),
                    shouldAvoidReplacedState ? null : e.getBlockReplacedState(),
                    REGULAR_RECORD_FLAGS);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onBucketEmpty(PlayerBucketEmptyEvent e) {
        Key blockKey = Keys.ofMaterialAndData(e.getBucket().name().replace("_BUCKET", ""));
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            this.worldRecordService.get().recordBlockPlace(blockKey,
                    e.getBlockClicked().getLocation(wrapper.getHandle()), 1,
                    null, REGULAR_RECORD_FLAGS);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onStructureGrow(StructureGrowEvent e) {
        KeyMap<Integer> placedBlockCounts = KeyMaps.createArrayMap(KeyIndicator.MATERIAL);
        KeyMap<Integer> brokenBlockCounts = KeyMaps.createArrayMap(KeyIndicator.MATERIAL);
        e.getBlocks().forEach(blockState -> {
            Key placedBlockKey = Keys.of(blockState);
            Key brokenBlockKey = Keys.of(blockState.getBlock());
            if (!placedBlockKey.equals(brokenBlockKey)) {
                if (!placedBlockKey.equals(ConstantKeys.AIR))
                    placedBlockCounts.put(placedBlockKey, placedBlockCounts.getOrDefault(placedBlockKey, 0) + 1);
                if (!brokenBlockKey.equals(ConstantKeys.AIR))
                    brokenBlockCounts.put(brokenBlockKey, brokenBlockCounts.getOrDefault(brokenBlockKey, 0) + 1);
            }
        });
        this.worldRecordService.get().recordMultiBlocksPlace(placedBlockCounts, e.getLocation(), WorldRecordFlags.DIRTY_CHUNKS);
        this.worldRecordService.get().recordMultiBlocksBreak(brokenBlockCounts, e.getLocation(), WorldRecordFlags.DIRTY_CHUNKS);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onBlockGrow(BlockGrowEvent e) {
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            this.worldRecordService.get().recordBlockPlace(Keys.of(e.getNewState()),
                    e.getBlock().getLocation(wrapper.getHandle()),
                    1, e.getBlock().getState(), REGULAR_RECORD_FLAGS);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onBlockFrom(BlockFormEvent e) {
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location location = e.getNewState().getLocation(wrapper.getHandle());
            // Do not save block counts
            this.worldRecordService.get().recordBlockBreak(Keys.of(e.getBlock()), location, 1, WorldRecordFlags.DIRTY_CHUNKS);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent e) {
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            this.worldRecordService.get().recordBlockPlace(Keys.of(e.getNewState()),
                    e.getBlock().getLocation(wrapper.getHandle()),
                    1, e.getBlock().getState(), REGULAR_RECORD_FLAGS);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onMinecartPlace(VehicleCreateEvent e) {
        if (e.getVehicle().isDead() || !(e.getVehicle() instanceof Minecart))
            return;

        Key minecartBlockKey = getMinecartBlockKey(e.getVehicle().getType());
        if (minecartBlockKey != null) {
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                this.worldRecordService.get().recordBlockPlace(minecartBlockKey,
                        e.getVehicle().getLocation(wrapper.getHandle()),
                        1, null, REGULAR_RECORD_FLAGS);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
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
                try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                    this.worldRecordService.get().recordBlockPlace(newSpawnerKey, block.getLocation(wrapper.getHandle()),
                            1, oldBlockState, REGULAR_RECORD_FLAGS);
                }
            }
        }, 1L);
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

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            this.worldRecordService.get().recordBlockPlace(blockKey, e.getBlock().getLocation(wrapper.getHandle()), 1,
                    e.getBlock().getState(), WorldRecordFlags.SAVE_BLOCK_COUNT);
        }
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
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                this.worldRecordService.get().recordBlockBreak(blockKey, e.getEntity().getLocation(wrapper.getHandle()),
                        1, REGULAR_RECORD_FLAGS);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onBucketFill(PlayerBucketFillEvent e) {
        boolean isWaterLogged = plugin.getNMSWorld().isWaterLogged(e.getBlockClicked());
        if (isWaterLogged || e.getBlockClicked().isLiquid()) {
            Key blockKey = isWaterLogged ? ConstantKeys.WATER : Keys.of(e.getBlockClicked());
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                this.worldRecordService.get().recordBlockBreak(blockKey,
                        e.getBlockClicked().getLocation(wrapper.getHandle()),
                        1, REGULAR_RECORD_FLAGS);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onDragonEggDrop(ItemSpawnEvent e) {
        if (e.getEntity().getItemStack().getType() == Material.DRAGON_EGG) {
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                for (Entity nearby : e.getEntity().getNearbyEntities(2, 2, 2)) {
                    if (nearby instanceof FallingBlock) {
                        Key blockKey = plugin.getNMSAlgorithms().getFallingBlockType((FallingBlock) nearby);
                        this.worldRecordService.get().recordBlockBreak(blockKey, nearby.getLocation(wrapper.getHandle()),
                                1, WorldRecordFlags.SAVE_BLOCK_COUNT);
                        return;
                    }
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
        boolean isWindCharge = e.getEntityType() == WIND_CHARGE || e.getEntityType() == BREEZE_WIND_CHARGE;

        KeyMap<Integer> blockCounts = KeyMaps.createArrayMap(KeyIndicator.MATERIAL);
        e.blockList().forEach(block -> {
            Material blockType = block.getType();
            // Wind charges only break chorus flowers and pointed drip-stones
            if (isWindCharge && blockType != CHORUS_FLOWER && blockType != POINTED_DRIPSTONE)
                return;

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
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                this.worldRecordService.get().recordBlockBreak(blockKey, e.getVehicle().getLocation(wrapper.getHandle()),
                        1, REGULAR_RECORD_FLAGS);
            }
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
    private static Key getMinecartBlockKey(EntityType minecartType) {
        switch (minecartType) {
            case MINECART_HOPPER:
                return ConstantKeys.HOPPER;
            case MINECART_COMMAND:
                return ConstantKeys.COMMAND_BLOCK;
            case MINECART_TNT:
                return ConstantKeys.TNT;
            case MINECART_FURNACE:
                return ConstantKeys.FURNACE;
            case MINECART_CHEST:
                return ConstantKeys.CHEST;
            case MINECART_MOB_SPAWNER:
                return ConstantKeys.MOB_SPAWNER;
        }

        return null;
    }

    private class SpongeAbsorbListener implements Listener {

        private final Collection<Location> alreadySpongeAbosrbCalled = AutoRemovalCollection.newArrayList(5L * 50, TimeUnit.MILLISECONDS);

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onSpongeAbsorb(org.bukkit.event.block.SpongeAbsorbEvent e) {
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                Location location = e.getBlock().getLocation(wrapper.getHandle());

                if (alreadySpongeAbosrbCalled.contains(location))
                    return;

                worldRecordService.get().recordBlockPlace(ConstantKeys.WET_SPONGE, location, 1,
                        e.getBlock().getState(), WorldRecordFlags.SAVE_BLOCK_COUNT);

                alreadySpongeAbosrbCalled.add(location.clone());
            }
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

package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.service.world.WorldRecordFlags;
import com.bgsoftware.superiorskyblock.api.service.world.WorldRecordService;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.collections.AutoRemovalCollection;
import com.bgsoftware.superiorskyblock.core.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.key.map.KeyMaps;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.nms.bridge.PistonPushReaction;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class BlockChangesListener extends AbstractGameEventListener {

    @Nullable
    private static final Material CHORUS_FLOWER = EnumHelper.getEnum(Material.class, "CHORUS_FLOWER");
    @Nullable
    private static final Material POINTED_DRIPSTONE = EnumHelper.getEnum(Material.class, "POINTED_DRIPSTONE");

    @WorldRecordFlags
    private static final int REGULAR_RECORD_FLAGS = WorldRecordFlags.SAVE_BLOCK_COUNT | WorldRecordFlags.DIRTY_CHUNKS;
    @WorldRecordFlags
    private static final int ALL_RECORD_FLAGS = REGULAR_RECORD_FLAGS | WorldRecordFlags.HANDLE_NEARBY_BLOCKS;

    private final Collection<Location> alreadySpongeAbosrbCalled = AutoRemovalCollection.newArrayList(5L * 50, TimeUnit.MILLISECONDS);

    private final LazyReference<WorldRecordService> worldRecordService = new LazyReference<WorldRecordService>() {
        @Override
        protected WorldRecordService create() {
            return plugin.getServices().getService(WorldRecordService.class);
        }
    };

    public BlockChangesListener(SuperiorSkyblockPlugin plugin) {
        super(plugin);
        this.registerListeners();
    }

    /* BLOCK PLACES */

    private void onBlockPlace(GameEvent<GameEventArgs.BlockPlaceEvent> e) {
        Block block = e.getArgs().block;
        BlockState replacedState = e.getArgs().replacedState;

        boolean shouldAvoidReplacedState = replacedState.equals(block.getState());
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            this.worldRecordService.get().recordBlockPlace(Keys.of(block),
                    block.getLocation(wrapper.getHandle()),
                    plugin.getNMSWorld().getDefaultAmount(block),
                    shouldAvoidReplacedState ? null : replacedState,
                    REGULAR_RECORD_FLAGS);
        }
    }

    private void onBucketEmpty(GameEvent<GameEventArgs.PlayerEmptyBucketEvent> e) {
        Material bucket = e.getArgs().bucket;

        Key blockKey = Keys.ofMaterialAndData(bucket.name().replace("_BUCKET", ""));
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Block clickedBlock = e.getArgs().clickedBlock;
            this.worldRecordService.get().recordBlockPlace(blockKey,
                    clickedBlock.getLocation(wrapper.getHandle()), 1,
                    null, REGULAR_RECORD_FLAGS);
        }
    }

    private void onStructureGrow(GameEvent<GameEventArgs.StructureGrowEvent> e) {
        KeyMap<Integer> placedBlockCounts = KeyMaps.createArrayMap(KeyIndicator.MATERIAL);
        KeyMap<Integer> brokenBlockCounts = KeyMaps.createArrayMap(KeyIndicator.MATERIAL);
        e.getArgs().blocks.forEach(blockState -> {
            Key placedBlockKey = Keys.of(blockState);
            Key brokenBlockKey = Keys.of(blockState.getBlock());
            if (!placedBlockKey.equals(brokenBlockKey)) {
                if (!placedBlockKey.equals(ConstantKeys.AIR))
                    placedBlockCounts.put(placedBlockKey, placedBlockCounts.getOrDefault(placedBlockKey, 0) + 1);
                if (!brokenBlockKey.equals(ConstantKeys.AIR))
                    brokenBlockCounts.put(brokenBlockKey, brokenBlockCounts.getOrDefault(brokenBlockKey, 0) + 1);
            }
        });
        Location growLocation = e.getArgs().location;
        this.worldRecordService.get().recordMultiBlocksPlace(placedBlockCounts, growLocation, WorldRecordFlags.DIRTY_CHUNKS);
        this.worldRecordService.get().recordMultiBlocksBreak(brokenBlockCounts, growLocation, WorldRecordFlags.DIRTY_CHUNKS);
    }

    private void onBlockGrow(GameEvent<GameEventArgs.BlockGrowEvent> e) {
        Block block = e.getArgs().block;
        BlockState newState = e.getArgs().newState;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            this.worldRecordService.get().recordBlockPlace(Keys.of(newState),
                    block.getLocation(wrapper.getHandle()),
                    1, block.getState(), REGULAR_RECORD_FLAGS);
        }
    }

    private void onBlockForm(GameEvent<GameEventArgs.BlockFormEvent> e) {
        Block block = e.getArgs().block;
        BlockState newState = e.getArgs().newState;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location location = newState.getLocation(wrapper.getHandle());
            // Do not save block counts
            this.worldRecordService.get().recordBlockBreak(Keys.of(block), location, 1, WorldRecordFlags.DIRTY_CHUNKS);
        }
    }

    private void onBlockSpread(GameEvent<GameEventArgs.BlockSpreadEvent> e) {
        Block block = e.getArgs().block;
        BlockState newState = e.getArgs().newState;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            this.worldRecordService.get().recordBlockPlace(Keys.of(newState),
                    block.getLocation(wrapper.getHandle()),
                    1, block.getState(), REGULAR_RECORD_FLAGS);
        }
    }

    private void onMinecartPlace(GameEvent<GameEventArgs.EntitySpawnEvent> e) {
        Entity vehicle = e.getArgs().entity;

        if (!(vehicle instanceof Minecart))
            return;

        Key minecartBlockKey = getMinecartBlockKey(vehicle.getType());
        if (minecartBlockKey != null) {
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                this.worldRecordService.get().recordBlockPlace(minecartBlockKey,
                        vehicle.getLocation(wrapper.getHandle()),
                        1, null, REGULAR_RECORD_FLAGS);
            }
        }
    }

    private void onSpawnerChange(GameEvent<GameEventArgs.PlayerInteractEvent> e) {
        Action action = e.getArgs().action;
        Block clickedBlock = e.getArgs().clickedBlock;

        if (action != Action.RIGHT_CLICK_BLOCK || clickedBlock.getType() != Materials.SPAWNER.toBukkitType())
            return;

        ItemStack handItem = e.getArgs().usedItem;

        if (handItem == null)
            return;

        Material handItemType = handItem.getType();
        if (!Materials.isSpawnEgg(handItemType))
            return;

        Chunk chunk = clickedBlock.getChunk();
        BlockState oldBlockState = clickedBlock.getState();
        Key oldSpawnerKey = Keys.of(oldBlockState);

        BukkitExecutor.sync(() -> {
            if (!chunk.isLoaded())
                return;

            Key newSpawnerKey = Keys.of(clickedBlock);
            if (!oldSpawnerKey.equals(newSpawnerKey)) {
                try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                    this.worldRecordService.get().recordBlockPlace(newSpawnerKey, clickedBlock.getLocation(wrapper.getHandle()),
                            1, oldBlockState, REGULAR_RECORD_FLAGS);
                }
            }
        }, 1L);
    }

    private void onEntityChangeBlock(GameEvent<GameEventArgs.EntityChangeBlockEvent> e) {
        Key newBlockKey = e.getArgs().newType;
        Block block = e.getArgs().block;
        Key oldBlockKey = Keys.of(block);

        if (newBlockKey.equals(oldBlockKey))
            return;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location blockLocation = block.getLocation(wrapper.getHandle());

            if (!oldBlockKey.equals(ConstantKeys.AIR)) {
                this.worldRecordService.get().recordBlockBreak(oldBlockKey, blockLocation,
                        plugin.getNMSWorld().getDefaultAmount(block),
                        ALL_RECORD_FLAGS);
            }

            if (!newBlockKey.equals(ConstantKeys.AIR)) {
                this.worldRecordService.get().recordBlockPlace(newBlockKey, blockLocation, 1,
                        null, ALL_RECORD_FLAGS);
            }
        }
    }

    /* BLOCK BREAKS */

    private void onBlockBreak(GameEvent<GameEventArgs.BlockBreakEvent> e) {
        this.worldRecordService.get().recordBlockBreak(e.getArgs().block, ALL_RECORD_FLAGS);
    }

    private void onBlockDestroy(GameEvent<GameEventArgs.BlockDestroyEvent> e) {
        this.worldRecordService.get().recordBlockBreak(e.getArgs().block, WorldRecordFlags.DIRTY_CHUNKS);
    }

    private void onBucketFill(GameEvent<GameEventArgs.PlayerFillBucketEvent> e) {
        Block clickedBlock = e.getArgs().clickedBlock;

        boolean isWaterLogged = plugin.getNMSWorld().isWaterLogged(clickedBlock);
        if (isWaterLogged || clickedBlock.isLiquid()) {
            Key blockKey = isWaterLogged ? ConstantKeys.WATER : Keys.of(clickedBlock);
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                this.worldRecordService.get().recordBlockBreak(blockKey,
                        clickedBlock.getLocation(wrapper.getHandle()),
                        1, REGULAR_RECORD_FLAGS);
            }
        }
    }

    private void onDragonEggDrop(GameEvent<GameEventArgs.EntitySpawnEvent> e) {
        if (!(e.getArgs().entity instanceof Item))
            return;

        Item item = (Item) e.getArgs().entity;

        if (item.getItemStack().getType() == Material.DRAGON_EGG) {
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                for (Entity nearby : item.getNearbyEntities(2, 2, 2)) {
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

    private void onPistonExtend(GameEvent<GameEventArgs.PistonExtendEvent> e) {
        for (Block block : e.getArgs().blocks) {
            if (plugin.getNMSWorld().getPistonReaction(block) == PistonPushReaction.DESTROY) {
                this.worldRecordService.get().recordBlockBreak(block, 1, REGULAR_RECORD_FLAGS);
            }
        }
    }

    private void onLeavesDecay(GameEvent<GameEventArgs.LeavesDecayEvent> e) {
        this.worldRecordService.get().recordBlockBreak(e.getArgs().block, 1, REGULAR_RECORD_FLAGS);
    }

    private void onBlockFromTo(GameEvent<GameEventArgs.BlockFromToEvent> e) {
        // Ignore dragon eggs, otherwise it will add +1 to the count of dragon eggs
        // when right-clicking them
        if (e.getArgs().block.getType() == Material.DRAGON_EGG)
            return;

        Block toBlock = e.getArgs().toBlock;

        if (toBlock.getType() != Material.AIR) {
            // Do not save block counts
            this.worldRecordService.get().recordBlockBreak(toBlock, 1, WorldRecordFlags.DIRTY_CHUNKS);
        } else {
            BukkitExecutor.sync(() -> {
                // Do not save block counts
                this.worldRecordService.get().recordBlockPlace(toBlock, 1, null, WorldRecordFlags.DIRTY_CHUNKS);
            });
        }
    }

    private void onEntityExplode(GameEvent<GameEventArgs.EntityExplodeEvent> e) {
        Entity entity = e.getArgs().entity;

        KeyMap<Integer> blockCounts = KeyMaps.createArrayMap(KeyIndicator.MATERIAL);
        e.getArgs().blocks.forEach(block -> {
            Material blockType = block.getType();
            // Soft explosions only break chorus flowers and pointed drip-stones
            if (e.getArgs().isSoftExplosion && blockType != CHORUS_FLOWER && blockType != POINTED_DRIPSTONE)
                return;

            Key blockKey = Keys.of(block);
            blockCounts.put(blockKey, blockCounts.getOrDefault(blockKey, 0) + 1);
        });

        if (entity instanceof TNTPrimed)
            blockCounts.put(ConstantKeys.TNT, blockCounts.getOrDefault(ConstantKeys.TNT, 0) + 1);

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            this.worldRecordService.get().recordMultiBlocksBreak(blockCounts,
                    entity.getLocation(wrapper.getHandle()), REGULAR_RECORD_FLAGS);
        }
    }

    private void onChorusHit(GameEvent<GameEventArgs.ProjectileHitEvent> e) {
        BukkitEntities.getPlayerSource(e.getArgs().entity).ifPresent(shooter -> {
            Block hitBlock = e.getArgs().hitBlock;
            if (hitBlock != null && hitBlock.getType() == CHORUS_FLOWER) {
                this.worldRecordService.get().recordBlockBreak(hitBlock, 1, REGULAR_RECORD_FLAGS);
            }
        });
    }

    private void onSpongeAbsorb(GameEvent<GameEventArgs.SpongeAbsorbEvent> e) {
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Block block = e.getArgs().block;
            Location location = block.getLocation(wrapper.getHandle());

            if (alreadySpongeAbosrbCalled.contains(location))
                return;

            worldRecordService.get().recordBlockPlace(ConstantKeys.WET_SPONGE, location, 1,
                    block.getState(), WorldRecordFlags.SAVE_BLOCK_COUNT);

            alreadySpongeAbosrbCalled.add(location.clone());
        }
    }

    /* INTERNAL */

    private void registerListeners() {
        registerCallback(GameEventType.BLOCK_PLACE_EVENT, GameEventPriority.MONITOR, this::onBlockPlace);
        registerCallback(GameEventType.PLAYER_EMPTY_BUCKET_EVENT, GameEventPriority.MONITOR, this::onBucketEmpty);
        registerCallback(GameEventType.STRUCTURE_GROW_EVENT, GameEventPriority.MONITOR, this::onStructureGrow);
        registerCallback(GameEventType.BLOCK_GROW_EVENT, GameEventPriority.MONITOR, this::onBlockGrow);
        registerCallback(GameEventType.BLOCK_FORM_EVENT, GameEventPriority.MONITOR, this::onBlockForm);
        registerCallback(GameEventType.BLOCK_SPREAD_EVENT, GameEventPriority.MONITOR, this::onBlockSpread);
        registerCallback(GameEventType.ENTITY_SPAWN_EVENT, GameEventPriority.MONITOR, this::onMinecartPlace);
        registerCallback(GameEventType.PLAYER_INTERACT_EVENT, GameEventPriority.MONITOR, this::onSpawnerChange);
        registerCallback(GameEventType.ENTITY_CHANGE_BLOCK_EVENT, GameEventPriority.MONITOR, this::onEntityChangeBlock);
        registerCallback(GameEventType.BLOCK_BREAK_EVENT, GameEventPriority.MONITOR, this::onBlockBreak);
        registerCallback(GameEventType.BLOCK_DESTROY_EVENT, GameEventPriority.MONITOR, this::onBlockDestroy);
        registerCallback(GameEventType.PLAYER_FILL_BUCKET_EVENT, GameEventPriority.MONITOR, this::onBucketFill);
        registerCallback(GameEventType.ENTITY_SPAWN_EVENT, GameEventPriority.MONITOR, this::onDragonEggDrop);
        registerCallback(GameEventType.PISTON_EXTEND_EVENT, GameEventPriority.MONITOR, this::onPistonExtend);
        registerCallback(GameEventType.LEAVES_DECAY_EVENT, GameEventPriority.MONITOR, this::onLeavesDecay);
        registerCallback(GameEventType.BLOCK_FROM_TO_EVENT, GameEventPriority.MONITOR, this::onBlockFromTo);
        registerCallback(GameEventType.ENTITY_EXPLODE_EVENT, GameEventPriority.MONITOR, this::onEntityExplode);
        registerCallback(GameEventType.PROJECTILE_HIT_EVENT, GameEventPriority.MONITOR, this::onChorusHit);
        registerCallback(GameEventType.SPONGE_ABSORB_EVENT, GameEventPriority.MONITOR, this::onSpongeAbsorb);
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

}

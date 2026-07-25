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
import com.bgsoftware.superiorskyblock.core.collections.ArrayMap;
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
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BlockChangesListener extends AbstractGameEventListener {

    private static final Map<Material, Key> BUCKET_TO_KEY_CACHE = new ArrayMap<>();

    @Nullable
    private static final Material CHORUS_FLOWER = EnumHelper.getEnum(Material.class, "CHORUS_FLOWER");

    @Nullable
    private static final Material CLOSED_EYEBLOSSOM = EnumHelper.getEnum(Material.class, "CLOSED_EYEBLOSSOM");

    @Nullable
    private static final Material OPEN_EYEBLOSSOM = EnumHelper.getEnum(Material.class, "OPEN_EYEBLOSSOM");
    @Nullable
    private static final CreatureSpawnEvent.SpawnReason BUILD_COPPERGOLEM = EnumHelper.getEnum(CreatureSpawnEvent.SpawnReason.class, "BUILD_COPPERGOLEM");

    @WorldRecordFlags
    private static final int REGULAR_RECORD_FLAGS = WorldRecordFlags.SAVE_BLOCK_COUNT | WorldRecordFlags.DIRTY_CHUNKS;

    static {
        for (Material material : Material.values()) {
            if (material.name().contains("_BUCKET")) {
                BUCKET_TO_KEY_CACHE.put(material, getBlockKeyFromBucketMaterial(material));
            }
        }
    }

    private final Collection<Location> alreadySpongeAbosrbCalled = AutoRemovalCollection.newArrayList(5L * 50, TimeUnit.MILLISECONDS);
    private final Collection<Location> alreadyChorusFlowerTracked = AutoRemovalCollection.newArrayList(1L * 50, TimeUnit.MILLISECONDS);

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

        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(block.getWorld()))
            return;

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

        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(e.getArgs().clickedBlock.getWorld()))
            return;

        Key blockKey = BUCKET_TO_KEY_CACHE.computeIfAbsent(bucket, BlockChangesListener::getBlockKeyFromBucketMaterial);
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Block clickedBlock = e.getArgs().clickedBlock;
            this.worldRecordService.get().recordBlockPlace(blockKey,
                    clickedBlock.getLocation(wrapper.getHandle()), 1,
                    null, REGULAR_RECORD_FLAGS);
        }
    }

    private void onStructureGrow(GameEvent<GameEventArgs.StructureGrowEvent> e) {
        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(e.getArgs().location.getWorld()))
            return;

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

        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(block.getWorld()))
            return;

        BlockState newState = e.getArgs().newState;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            this.worldRecordService.get().recordBlockPlace(Keys.of(newState),
                    block.getLocation(wrapper.getHandle()),
                    1, block.getState(), REGULAR_RECORD_FLAGS);
        }
    }

    private void onBlockForm(GameEvent<GameEventArgs.BlockFormEvent> e) {
        Block block = e.getArgs().block;

        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(block.getWorld()))
            return;

        BlockState newState = e.getArgs().newState;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location location = block.getLocation(wrapper.getHandle());
            this.worldRecordService.get().recordBlockPlace(Keys.of(newState), location, 1, block.getState(), REGULAR_RECORD_FLAGS);
        }
    }

    private void onBlockSpread(GameEvent<GameEventArgs.BlockSpreadEvent> e) {
        Block block = e.getArgs().block;

        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(block.getWorld()))
            return;

        Block source = e.getArgs().source;
        BlockState newState = e.getArgs().newState;

        Material newStateType = newState.getType();
        Material sourceType = source.getType();

        Key spreadBlock = null;

        if (newStateType == CHORUS_FLOWER && sourceType == CHORUS_FLOWER) {
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                Location spreadBlockLocation = source.getLocation(wrapper.getHandle());
                // When a chorus flower grows, it is replaced with a flower plant and multiple new flowers can grow.
                // Therefore, we want to track a plant one time instead of a new flower.
                if (!alreadyChorusFlowerTracked.contains(spreadBlockLocation)) {
                    spreadBlock = ConstantKeys.CHORUS_PLANT;
                    alreadyChorusFlowerTracked.add(spreadBlockLocation.clone());
                }
            }
        }

        if (spreadBlock == null) {
            spreadBlock = Keys.of(newState);
        }

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            this.worldRecordService.get().recordBlockPlace(spreadBlock,
                    block.getLocation(wrapper.getHandle()),
                    1, block.getState(), REGULAR_RECORD_FLAGS);
        }
    }

    private void onBlockShapeUpdate(GameEvent<GameEventArgs.BlockUpdateShapeEvent> e) {
        Block block = e.getArgs().block;

        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(block.getWorld()))
            return;

        Key newStateKey = Keys.of(block);

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location blockLocation = block.getLocation(wrapper.getHandle());

            if (newStateKey.equals(ConstantKeys.AIR)) {
                // New state is AIR, so this is a block break
                Key oldStateKey = Keys.of(e.getArgs().oldState);
                this.worldRecordService.get().recordBlockBreak(oldStateKey, blockLocation,
                        1, REGULAR_RECORD_FLAGS);
            } else {
                this.worldRecordService.get().recordBlockPlace(newStateKey,
                        block.getLocation(wrapper.getHandle()),
                        1, e.getArgs().oldState, REGULAR_RECORD_FLAGS);
            }
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

        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(clickedBlock.getWorld()))
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
                    Location location = clickedBlock.getLocation(wrapper.getHandle());
                    int spawnerCount = plugin.getProviders().getSpawnersProvider().getSpawner(location).getKey();
                    this.worldRecordService.get().recordBlockBreak(oldSpawnerKey, location, spawnerCount, REGULAR_RECORD_FLAGS);
                    this.worldRecordService.get().recordBlockPlace(newSpawnerKey, location, spawnerCount, null, REGULAR_RECORD_FLAGS);
                }
            }
        }, 1L);
    }

    private void onEntityChangeBlock(GameEvent<GameEventArgs.EntityChangeBlockEvent> e) {
        if (e.getArgs().entity instanceof Player)
            return;

        Block block = e.getArgs().block;

        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(block.getWorld()))
            return;

        Key newBlockKey = e.getArgs().newType;
        Key oldBlockKey = Keys.of(block);

        if (newBlockKey.equals(oldBlockKey))
            return;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location blockLocation = block.getLocation(wrapper.getHandle());

            if (!oldBlockKey.equals(ConstantKeys.AIR)) {
                this.worldRecordService.get().recordBlockBreak(oldBlockKey, blockLocation,
                        plugin.getNMSWorld().getDefaultAmount(block),
                        REGULAR_RECORD_FLAGS);
            }

            if (!newBlockKey.equals(ConstantKeys.AIR)) {
                this.worldRecordService.get().recordBlockPlace(newBlockKey, blockLocation, 1,
                        null, REGULAR_RECORD_FLAGS);
            }
        }
    }

    /* BLOCK BREAKS */

    private void onBlockBreak(GameEvent<GameEventArgs.BlockBreakEvent> e) {
        Block block = e.getArgs().block;

        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(block.getWorld()))
            return;

        this.worldRecordService.get().recordBlockBreak(block, REGULAR_RECORD_FLAGS);
    }

    private void onBucketFill(GameEvent<GameEventArgs.PlayerFillBucketEvent> e) {
        Block clickedBlock = e.getArgs().clickedBlock;

        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(clickedBlock.getWorld()))
            return;

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

    private void onEntitySpawn(GameEvent<GameEventArgs.EntitySpawnEvent> e) {
        Entity entity = e.getArgs().entity;

        if (entity.isDead())
            return;

        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(entity.getWorld()))
            return;

        onMinecartPlace(e);
        onDragonEggDrop(e);
        onGolemStructure(e);
    }

    private void onDragonEggDrop(GameEvent<GameEventArgs.EntitySpawnEvent> e) {
        Entity entity = e.getArgs().entity;

        if (!(entity instanceof Item))
            return;

        Item item = (Item) entity;

        if (item.getItemStack().getType() != Material.DRAGON_EGG)
            return;

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

    private void onGolemStructure(GameEvent<GameEventArgs.EntitySpawnEvent> e) {
        CreatureSpawnEvent.SpawnReason spawnReason = e.getArgs().spawnReason;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location entityLocation = e.getArgs().entity.getLocation(wrapper.getHandle());

            if (spawnReason == CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM) {
                this.worldRecordService.get().recordBlockBreak(ConstantKeys.IRON_BLOCK, entityLocation, 4, 0);
                this.worldRecordService.get().recordBlockBreak(ConstantKeys.CARVED_PUMPKIN, entityLocation, 1, REGULAR_RECORD_FLAGS);
            } else if (spawnReason == CreatureSpawnEvent.SpawnReason.BUILD_SNOWMAN) {
                this.worldRecordService.get().recordBlockBreak(ConstantKeys.SNOW_BLOCK, entityLocation, 2, 0);
                this.worldRecordService.get().recordBlockBreak(ConstantKeys.CARVED_PUMPKIN, entityLocation, 1, REGULAR_RECORD_FLAGS);
            } else if (spawnReason == CreatureSpawnEvent.SpawnReason.BUILD_WITHER) {
                this.worldRecordService.get().recordBlockBreak(ConstantKeys.SOUL_SAND, entityLocation, 4, 0);
                this.worldRecordService.get().recordBlockBreak(ConstantKeys.WITHER_SKELETON_SKULL, entityLocation, 3, REGULAR_RECORD_FLAGS);
            } else if (spawnReason == BUILD_COPPERGOLEM) {
                Block copperOrChestBlock = entityLocation.getBlock().getRelative(BlockFace.DOWN);
                Key copperBlock = Keys.of(copperOrChestBlock);
                this.worldRecordService.get().recordBlockBreak(copperBlock, entityLocation, 1, 0);
                this.worldRecordService.get().recordBlockBreak(ConstantKeys.CARVED_PUMPKIN, entityLocation, 1, 0);
                BukkitExecutor.sync(() -> {
                    Key chestBlock = Keys.of(copperOrChestBlock);
                    this.worldRecordService.get().recordBlockPlace(chestBlock, entityLocation, 1, null, REGULAR_RECORD_FLAGS);
                }, 1L);
            }
        }

    }

    private void onPistonExtend(GameEvent<GameEventArgs.PistonExtendEvent> e) {
        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(e.getArgs().block.getWorld()))
            return;

        for (Block block : e.getArgs().blocks) {
            if (plugin.getNMSWorld().getPistonReaction(block) == PistonPushReaction.DESTROY) {
                this.worldRecordService.get().recordBlockBreak(block, 1, REGULAR_RECORD_FLAGS);
            }
        }
    }

    private void onLeavesDecay(GameEvent<GameEventArgs.LeavesDecayEvent> e) {
        Block block = e.getArgs().block;

        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(block.getWorld()))
            return;

        this.worldRecordService.get().recordBlockBreak(block, 1, REGULAR_RECORD_FLAGS);
    }

    private void onBlockFromTo(GameEvent<GameEventArgs.BlockFromToEvent> e) {
        Block block = e.getArgs().block;

        // Ignore dragon eggs, otherwise it will add +1 to the count of dragon eggs
        // when right-clicking them
        if (block.getType() == Material.DRAGON_EGG)
            return;

        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(block.getWorld()))
            return;

        Block toBlock = e.getArgs().toBlock;

        if (toBlock.getType() != Material.AIR) {
            // Do not save block counts
            this.worldRecordService.get().recordBlockBreak(toBlock, 1, WorldRecordFlags.DIRTY_CHUNKS);
        } else {
            BukkitExecutor.sync(() -> {
                // Ignore cobblestone blocks, otherwise it will add +1 to the count of cobblestone when generated
                // from cobblestone generator
                if (toBlock.getType() != Material.COBBLESTONE) {
                    // Do not save block counts
                    this.worldRecordService.get().recordBlockPlace(toBlock, 1, null, WorldRecordFlags.DIRTY_CHUNKS);
                }
            });
        }
    }

    private void onBlockFade(GameEvent<GameEventArgs.BlockFadeEvent> e) {
        Block block = e.getArgs().block;

        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(block.getWorld()))
            return;

        BlockState newState = e.getArgs().newState;
        if (newState.getType() == Material.AIR) {
            this.worldRecordService.get().recordBlockBreak(block, REGULAR_RECORD_FLAGS);
        } else {
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                this.worldRecordService.get().recordBlockPlace(Keys.of(newState),
                        newState.getLocation(wrapper.getHandle()),
                        1,
                        block.getState(),
                        REGULAR_RECORD_FLAGS);
            }
        }
    }

    private void onEntityExplode(GameEvent<GameEventArgs.EntityExplodeEvent> e) {
        if (e.getArgs().isSoftExplosion)
            return;

        Entity entity = e.getArgs().entity;

        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(entity.getWorld()))
            return;

        KeyMap<Integer> blockCounts = KeyMaps.createArrayMap(KeyIndicator.MATERIAL);
        e.getArgs().blocks.forEach(block -> {
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

    private void onSpongeAbsorb(GameEvent<GameEventArgs.SpongeAbsorbEvent> e) {
        Block block = e.getArgs().block;

        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(block.getWorld()))
            return;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location location = block.getLocation(wrapper.getHandle());

            if (alreadySpongeAbosrbCalled.contains(location))
                return;

            worldRecordService.get().recordBlockPlace(ConstantKeys.WET_SPONGE, location, 1,
                    block.getState(), WorldRecordFlags.SAVE_BLOCK_COUNT);

            alreadySpongeAbosrbCalled.add(location.clone());
        }
    }

    private void onGenericGame(GameEvent<GameEventArgs.GenericGameEvent> e) {
        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(e.getArgs().world))
            return;

        // We only care about block_change
        if (!e.getArgs().gameEvent.equals("block_change"))
            return;

        Location blockLocation = e.getArgs().location;
        Block block = blockLocation.getBlock();
        Material blockType = block.getType();

        Key newBlockKey;
        Key oldBlockKey;

        if (blockType == OPEN_EYEBLOSSOM) {
            // OPEN_EYEBLOSSOM was changed, we want to remove CLOSED_EYEBLOSSOM and replace it with OPEN_EYEBLOSSOM
            newBlockKey = ConstantKeys.OPEN_EYEBLOSSOM;
            oldBlockKey = ConstantKeys.CLOSED_EYEBLOSSOM;
        } else if (blockType == CLOSED_EYEBLOSSOM) {
            // CLOSED_EYEBLOSSOM was changed, we want to remove OPEN_EYEBLOSSOM and replace it with CLOSED_EYEBLOSSOM
            newBlockKey = ConstantKeys.CLOSED_EYEBLOSSOM;
            oldBlockKey = ConstantKeys.OPEN_EYEBLOSSOM;
        } else {
            return;
        }

        worldRecordService.get().recordBlockPlace(newBlockKey, blockLocation, 1, null, 0);
        worldRecordService.get().recordBlockBreak(oldBlockKey, blockLocation, 1, REGULAR_RECORD_FLAGS);
    }

    /* INTERNAL */

    private void registerListeners() {
        registerCallback(GameEventType.BLOCK_PLACE_EVENT, GameEventPriority.MONITOR, this::onBlockPlace);
        registerCallback(GameEventType.PLAYER_EMPTY_BUCKET_EVENT, GameEventPriority.MONITOR, this::onBucketEmpty);
        registerCallback(GameEventType.STRUCTURE_GROW_EVENT, GameEventPriority.MONITOR, this::onStructureGrow);
        registerCallback(GameEventType.BLOCK_GROW_EVENT, GameEventPriority.MONITOR, this::onBlockGrow);
        registerCallback(GameEventType.BLOCK_FORM_EVENT, GameEventPriority.MONITOR, this::onBlockForm);
        registerCallback(GameEventType.BLOCK_SPREAD_EVENT, GameEventPriority.MONITOR, this::onBlockSpread);
        registerCallback(GameEventType.BLOCK_UPDATE_SHAPE_EVENT, GameEventPriority.MONITOR, this::onBlockShapeUpdate);
        registerCallback(GameEventType.PLAYER_INTERACT_EVENT, GameEventPriority.MONITOR, this::onSpawnerChange);
        registerCallback(GameEventType.ENTITY_CHANGE_BLOCK_EVENT, GameEventPriority.MONITOR, this::onEntityChangeBlock);
        registerCallback(GameEventType.BLOCK_BREAK_EVENT, GameEventPriority.MONITOR, this::onBlockBreak);
        registerCallback(GameEventType.PLAYER_FILL_BUCKET_EVENT, GameEventPriority.MONITOR, this::onBucketFill);
        registerCallback(GameEventType.ENTITY_SPAWN_EVENT, GameEventPriority.MONITOR, this::onEntitySpawn);
        registerCallback(GameEventType.PISTON_EXTEND_EVENT, GameEventPriority.MONITOR, this::onPistonExtend);
        registerCallback(GameEventType.LEAVES_DECAY_EVENT, GameEventPriority.MONITOR, this::onLeavesDecay);
        registerCallback(GameEventType.BLOCK_FROM_TO_EVENT, GameEventPriority.MONITOR, this::onBlockFromTo);
        registerCallback(GameEventType.BLOCK_FADE_EVENT, GameEventPriority.MONITOR, this::onBlockFade);
        registerCallback(GameEventType.ENTITY_EXPLODE_EVENT, GameEventPriority.MONITOR, this::onEntityExplode);
        registerCallback(GameEventType.SPONGE_ABSORB_EVENT, GameEventPriority.MONITOR, this::onSpongeAbsorb);

        if (CLOSED_EYEBLOSSOM != null || OPEN_EYEBLOSSOM != null)
            registerCallback(GameEventType.GENERIC_GAME_EVENT, GameEventPriority.MONITOR, this::onGenericGame);
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

    private static Key getBlockKeyFromBucketMaterial(Material material) {
        return Keys.ofMaterialAndData(material.name().replace("_BUCKET", ""));
    }

}

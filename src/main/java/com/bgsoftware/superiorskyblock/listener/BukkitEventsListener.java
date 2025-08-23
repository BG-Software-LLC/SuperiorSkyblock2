package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.platform.IEventsDispatcher;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.PlayerHand;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.events.EventCallback;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import com.bgsoftware.superiorskyblock.platform.event.args.IEventArgs;
import com.bgsoftware.superiorskyblock.world.BukkitItems;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class BukkitEventsListener implements Listener {

    private static final ReflectMethod<Entity> PROJECTILE_HIT_TARGET_ENTITY = new ReflectMethod<>(
            ProjectileHitEvent.class, "getHitEntity");
    private static final ReflectMethod<Block> PROJECTILE_HIT_EVENT_TARGET_BLOCK = new ReflectMethod<>(
            ProjectileHitEvent.class, "getHitBlock");
    @Nullable
    private static final EntityType WIND_CHARGE = EnumHelper.getEnum(EntityType.class, "WIND_CHARGE");
    @Nullable
    private static final EntityType BREEZE_WIND_CHARGE = EnumHelper.getEnum(EntityType.class, "BREEZE_WIND_CHARGE");

    private final SuperiorSkyblockPlugin plugin;

    private boolean shouldFireAttemptEntitySpawnEvent = false;

    public BukkitEventsListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;

        // Block Events
        createEventListener(GameEventType.BLOCK_BREAK_EVENT, BlockBreakEvent.class, this::createGameEvent);
        createEventListener(GameEventType.BLOCK_BURN_EVENT, BlockBurnEvent.class, this::createGameEvent);
        createEventListener(GameEventType.BLOCK_DISPENSE_EVENT, BlockDispenseEvent.class, this::createGameEvent);
        createEventListener(GameEventType.BLOCK_FADE_EVENT, BlockFadeEvent.class, this::createGameEvent);
        createEventListener(GameEventType.BLOCK_FORM_EVENT, BlockFormEvent.class, this::createGameEvent);
        createEventListener(GameEventType.BLOCK_FROM_TO_EVENT, BlockFromToEvent.class, this::createGameEvent);
        createEventListener(GameEventType.BLOCK_GROW_EVENT, BlockGrowEvent.class, this::createGameEvent);
        createEventListener(GameEventType.BLOCK_IGNITE_EVENT, BlockIgniteEvent.class, this::createGameEvent);
        createEventListener(GameEventType.BLOCK_PHYSICS_EVENT, BlockPhysicsEvent.class, this::createGameEvent);
        createEventListener(GameEventType.BLOCK_PLACE_EVENT, BlockPlaceEvent.class, this::createGameEvent);
        createEventListener(GameEventType.BLOCK_REDSTONE_EVENT, BlockRedstoneEvent.class, this::createGameEvent, this::cancelBlockRedstoneEvent);
        createEventListener(GameEventType.BLOCK_SPREAD_EVENT, BlockSpreadEvent.class, this::createGameEvent);
        createEventListener(GameEventType.LEAVES_DECAY_EVENT, LeavesDecayEvent.class, this::createGameEvent);
        createEventListener(GameEventType.PISTON_EXTEND_EVENT, BlockPistonExtendEvent.class, this::createGameEvent);
        createEventListener(GameEventType.PISTON_RETRACT_EVENT, BlockPistonRetractEvent.class, this::createGameEvent);

        // World Events
        createEventListener(GameEventType.CHUNK_LOAD_EVENT, ChunkLoadEvent.class, this::createGameEvent);
        createEventListener(GameEventType.CHUNK_UNLOAD_EVENT, ChunkUnloadEvent.class, this::createGameEvent);
        createEventListener(GameEventType.SIGN_CHANGE_EVENT, SignChangeEvent.class, this::createGameEvent);
        createEventListener(GameEventType.STRUCTURE_GROW_EVENT, StructureGrowEvent.class, this::createGameEvent);
        createEventListener(GameEventType.WORLD_UNLOAD_EVENT, WorldUnloadEvent.class, this::createGameEvent);

        // Entity Events
        createEventListener(GameEventType.ENTITY_BLOCK_FORM_EVENT, EntityBlockFormEvent.class, this::createGameEvent);
        createEventListener(GameEventType.ENTITY_CHANGE_BLOCK_EVENT, EntityChangeBlockEvent.class, this::createGameEvent);
        createEventListener(GameEventType.ENTITY_COLLISION_EVENT, VehicleEntityCollisionEvent.class, this::createGameEvent);
        createEventListener(GameEventType.ENTITY_DAMAGE_EVENT, EntityDamageByEntityEvent.class, this::createGameEvent);
        createEventListener(GameEventType.ENTITY_DAMAGE_EVENT, VehicleDamageEvent.class, this::createGameEvent);
        createEventListener(GameEventType.ENTITY_ENTER_PORTAL_EVENT, EntityPortalEnterEvent.class, this::createGameEvent);
        createEventListener(GameEventType.ENTITY_EXPLODE_EVENT, EntityExplodeEvent.class, this::createGameEvent);
        createEventListener(GameEventType.ENTITY_MOVE_EVENT, PlayerMoveEvent.class, this::createGameEvent);
        createEventListener(GameEventType.ENTITY_MOVE_EVENT, VehicleMoveEvent.class, this::createGameEvent);
        createEventListener(GameEventType.ENTITY_PORTAL_EVENT, EntityPortalEvent.class, this::createGameEvent);
        createEventListener(GameEventType.ENTITY_PORTAL_EVENT, PlayerPortalEvent.class, this::createGameEvent);
        createEventListener(GameEventType.ENTITY_RIDE_EVENT, VehicleEnterEvent.class, this::createGameEvent);
        createEventListener(GameEventType.ENTITY_SPAWN_EVENT, CreatureSpawnEvent.class, this::createGameEvent);
        createEventListener(GameEventType.ENTITY_SPAWN_EVENT, HangingPlaceEvent.class, this::createGameEvent);
        createEventListener(GameEventType.ENTITY_SPAWN_EVENT, ItemSpawnEvent.class, this::createGameEvent);
        createEventListener(GameEventType.ENTITY_SPAWN_EVENT, VehicleCreateEvent.class, this::createGameEvent, this::cancelVehicleCreateEvent);
        createEventListener(GameEventType.ENTITY_TARGET_EVENT, EntityTargetEvent.class, this::createGameEvent);
        createEventListener(GameEventType.ENTITY_TELEPORT_EVENT, EntityTeleportEvent.class, this::createGameEvent);
        createEventListener(GameEventType.ENTITY_TELEPORT_EVENT, PlayerTeleportEvent.class, this::createGameEvent);
        createEventListener(GameEventType.HANGING_BREAK_EVENT, HangingBreakByEntityEvent.class, this::createGameEvent);
        createEventListener(GameEventType.HANGING_PLACE_EVENT, HangingPlaceEvent.class, this::createGameEvent2);
        createEventListener(GameEventType.PROJECTILE_HIT_EVENT, ProjectileHitEvent.class, this::createGameEvent);
        createEventListener(GameEventType.PROJECTILE_LAUNCH_EVENT, ProjectileLaunchEvent.class, this::createGameEvent);

        // Inventory Events
        createEventListener(GameEventType.INVENTORY_CLICK_EVENT, InventoryClickEvent.class, this::createGameEvent);
        createEventListener(GameEventType.INVENTORY_CLOSE_EVENT, InventoryCloseEvent.class, this::createGameEvent);
        createEventListener(GameEventType.INVENTORY_OPEN_EVENT, InventoryOpenEvent.class, this::createGameEvent);

        // Player Events
        createEventListener(GameEventType.PLAYER_CHANGED_WORLD_EVENT, PlayerChangedWorldEvent.class, this::createGameEvent);
        createEventListener(GameEventType.PLAYER_COMMAND_EVENT, PlayerCommandPreprocessEvent.class, this::createGameEvent);
        createEventListener(GameEventType.PLAYER_DROP_ITEM_EVENT, PlayerDropItemEvent.class, this::createGameEvent);
        createEventListener(GameEventType.PLAYER_EMPTY_BUCKET_EVENT, PlayerBucketEmptyEvent.class, this::createGameEvent);
        createEventListener(GameEventType.PLAYER_FILL_BUCKET_EVENT, PlayerBucketFillEvent.class, this::createGameEvent);
        createEventListener(GameEventType.PLAYER_GAMEMODE_CHANGE, PlayerGameModeChangeEvent.class, this::createGameEvent);
        createEventListener(GameEventType.PLAYER_INTERACT_EVENT, PlayerInteractAtEntityEvent.class, this::createGameEvent);
        createEventListener(GameEventType.PLAYER_INTERACT_EVENT, PlayerInteractEntityEvent.class, this::createGameEvent);
        createEventListener(GameEventType.PLAYER_INTERACT_EVENT, PlayerInteractEvent.class, this::createGameEvent);
        createEventListener(GameEventType.PLAYER_ITEM_CONSUME_EVENT, PlayerItemConsumeEvent.class, this::createGameEvent);
        createEventListener(GameEventType.PLAYER_JOIN_EVENT, PlayerJoinEvent.class, this::createGameEvent);
        createEventListener(GameEventType.PLAYER_LEASH_ENTITY_EVENT, PlayerLeashEntityEvent.class, this::createGameEvent);
        createEventListener(GameEventType.PLAYER_LOGIN_EVENT, PlayerLoginEvent.class, this::createGameEvent);
        createEventListener(GameEventType.PLAYER_PICKUP_ITEM_EVENT, PlayerPickupItemEvent.class, this::createGameEvent);
        createEventListener(GameEventType.PLAYER_QUIT_EVENT, PlayerQuitEvent.class, this::createGameEvent);
        createEventListener(GameEventType.PLAYER_RESPAWN_EVENT, PlayerRespawnEvent.class, this::createGameEvent);
        createEventListener(GameEventType.PLAYER_SHEAR_ENTITY_EVENT, PlayerShearEntityEvent.class, this::createGameEvent);
        createEventListener(GameEventType.PLAYER_UNLEASH_ENTITY_EVENT, PlayerUnleashEntityEvent.class, this::createGameEvent);

        // Special Events (per version)
        try {
            Class.forName("org.bukkit.event.block.SpongeAbsorbEvent");
            createEventListener(GameEventType.SPONGE_ABSORB_EVENT, org.bukkit.event.block.SpongeAbsorbEvent.class, new SpongeAbsorbEventFunction());
        } catch (ClassNotFoundException ignored) {
        }

        try {
            Class.forName("com.destroystokyo.paper.event.block.BlockDestroyEvent");
            createEventListener(GameEventType.BLOCK_DESTROY_EVENT, com.destroystokyo.paper.event.block.BlockDestroyEvent.class, new BlockDestroyEventFunction());
        } catch (ClassNotFoundException ignored) {
        }

        try {
            Class.forName("com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent");
            createEventListener(GameEventType.ENTITY_DEATH_EVENT, com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent.class, new EntityRemoveFromWorldEventFunction());
        } catch (ClassNotFoundException ignored) {
            createEventListener(GameEventType.ENTITY_DEATH_EVENT, EntityDeathEvent.class, this::createGameEvent);
        }

        try {
            Class.forName("com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent");
            createEventListener(GameEventType.ATTEMPT_ENTITY_SPAWN_EVENT, com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent.class, new AttemptEntitySpawnEventFunction());
        } catch (ClassNotFoundException ignored) {
            shouldFireAttemptEntitySpawnEvent = true;
        }

        boolean registeredChatListener = false;
        if (plugin.getSettings().getChatSigningSupport()) {
            try {
                Class.forName("io.papermc.paper.event.player.AsyncChatEvent");
                createEventListener(GameEventType.PLAYER_CHAT_EVENT, io.papermc.paper.event.player.AsyncChatEvent.class, new AsyncChatEventFunctions(), new AsyncChatEventFunctions());
                registeredChatListener = true;
            } catch (Exception ignored) {
            }
        }

        if (!registeredChatListener)
            createEventListener(GameEventType.PLAYER_CHAT_EVENT, AsyncPlayerChatEvent.class, this::createGameEvent);

        try {
            Class.forName("org.bukkit.event.player.PlayerPickupArrowEvent");
            createEventListener(GameEventType.PLAYER_PICKUP_ARROW_EVENT, org.bukkit.event.player.PlayerPickupArrowEvent.class, new PlayerPickupArrowEventFunctions());
        } catch (Exception ignored) {
        }

        try {
            Class.forName("org.bukkit.event.player.PlayerAttemptPickupItemEvent");
            createEventListener(GameEventType.PLAYER_PICKUP_ITEM_EVENT, org.bukkit.event.player.PlayerAttemptPickupItemEvent.class, new PlayerPickupItemEventFunctions());
        } catch (Exception ignored) {
        }

        try {
            Class.forName("org.bukkit.event.raid.RaidTriggerEvent");
            createEventListener(GameEventType.RAID_TRIGGER_EVENT, org.bukkit.event.raid.RaidTriggerEvent.class, new RaidTriggerEventFunctions());
        } catch (Exception ignored) {
        }
    }

    /*
     * BLOCK EVENTS
     */

    private GameEvent<GameEventArgs.BlockBreakEvent> createGameEvent(GameEventType<GameEventArgs.BlockBreakEvent> eventType, GameEventPriority priority, BlockBreakEvent e) {
        GameEventArgs.BlockBreakEvent blockBreakEvent = new GameEventArgs.BlockBreakEvent();
        blockBreakEvent.block = e.getBlock();
        blockBreakEvent.player = e.getPlayer();
        return eventType.createEvent(blockBreakEvent);
    }

    private GameEvent<GameEventArgs.BlockBurnEvent> createGameEvent(GameEventType<GameEventArgs.BlockBurnEvent> eventType, GameEventPriority priority, BlockBurnEvent e) {
        GameEventArgs.BlockBurnEvent blockBurnEvent = new GameEventArgs.BlockBurnEvent();
        blockBurnEvent.block = e.getBlock();
        return eventType.createEvent(blockBurnEvent);
    }

    private GameEvent<GameEventArgs.BlockDispenseEvent> createGameEvent(GameEventType<GameEventArgs.BlockDispenseEvent> eventType, GameEventPriority priority, BlockDispenseEvent e) {
        GameEventArgs.BlockDispenseEvent blockDispenseEvent = new GameEventArgs.BlockDispenseEvent();
        blockDispenseEvent.block = e.getBlock();
        blockDispenseEvent.dispensedItem = e.getItem();
        blockDispenseEvent.velocity = e.getVelocity();
        return eventType.createEvent(blockDispenseEvent);
    }

    private GameEvent<GameEventArgs.BlockFadeEvent> createGameEvent(GameEventType<GameEventArgs.BlockFadeEvent> eventType, GameEventPriority priority, BlockFadeEvent e) {
        GameEventArgs.BlockFadeEvent blockFadeEvent = new GameEventArgs.BlockFadeEvent();
        blockFadeEvent.block = e.getBlock();
        blockFadeEvent.newState = e.getNewState();
        return eventType.createEvent(blockFadeEvent);
    }

    private GameEvent<GameEventArgs.BlockFormEvent> createGameEvent(GameEventType<GameEventArgs.BlockFormEvent> eventType, GameEventPriority priority, BlockFormEvent e) {
        GameEventArgs.BlockFormEvent blockFormEvent = new GameEventArgs.BlockFormEvent();
        blockFormEvent.block = e.getBlock();
        blockFormEvent.newState = e.getNewState();
        return eventType.createEvent(blockFormEvent);
    }

    private GameEvent<GameEventArgs.BlockFromToEvent> createGameEvent(GameEventType<GameEventArgs.BlockFromToEvent> eventType, GameEventPriority priority, BlockFromToEvent e) {
        GameEventArgs.BlockFromToEvent blockFromToEvent = new GameEventArgs.BlockFromToEvent();
        blockFromToEvent.block = e.getBlock();
        blockFromToEvent.toBlock = e.getToBlock();
        return eventType.createEvent(blockFromToEvent);
    }

    private GameEvent<GameEventArgs.BlockGrowEvent> createGameEvent(GameEventType<GameEventArgs.BlockGrowEvent> eventType, GameEventPriority priority, BlockGrowEvent e) {
        GameEventArgs.BlockGrowEvent blockGrowEvent = new GameEventArgs.BlockGrowEvent();
        blockGrowEvent.block = e.getBlock();
        blockGrowEvent.newState = e.getNewState();
        return eventType.createEvent(blockGrowEvent);
    }

    private GameEvent<GameEventArgs.BlockIgniteEvent> createGameEvent(GameEventType<GameEventArgs.BlockIgniteEvent> eventType, GameEventPriority priority, BlockIgniteEvent e) {
        GameEventArgs.BlockIgniteEvent blockIgniteEvent = new GameEventArgs.BlockIgniteEvent();
        blockIgniteEvent.block = e.getBlock();
        blockIgniteEvent.igniteCause = e.getCause();
        return eventType.createEvent(blockIgniteEvent);
    }

    private GameEvent<GameEventArgs.BlockPhysicsEvent> createGameEvent(GameEventType<GameEventArgs.BlockPhysicsEvent> eventType, GameEventPriority priority, BlockPhysicsEvent e) {
        GameEventArgs.BlockPhysicsEvent blockPhysicsEvent = new GameEventArgs.BlockPhysicsEvent();
        blockPhysicsEvent.block = e.getBlock();
        return eventType.createEvent(blockPhysicsEvent);
    }

    private GameEvent<GameEventArgs.BlockPlaceEvent> createGameEvent(GameEventType<GameEventArgs.BlockPlaceEvent> eventType, GameEventPriority priority, BlockPlaceEvent e) {
        GameEventArgs.BlockPlaceEvent blockPlaceEvent = new GameEventArgs.BlockPlaceEvent();
        blockPlaceEvent.block = e.getBlock();
        blockPlaceEvent.player = e.getPlayer();
        blockPlaceEvent.againstBlock = e.getBlockAgainst();
        blockPlaceEvent.replacedState = e.getBlockReplacedState();
        blockPlaceEvent.usedHand = BukkitItems.getHand(e);
        blockPlaceEvent.usedItem = getHandItem(e.getPlayer(), blockPlaceEvent.usedHand, true, () -> e.getItemInHand());
        return eventType.createEvent(blockPlaceEvent);
    }

    private GameEvent<GameEventArgs.BlockRedstoneEvent> createGameEvent(GameEventType<GameEventArgs.BlockRedstoneEvent> eventType, GameEventPriority priority, BlockRedstoneEvent e) {
        GameEventArgs.BlockRedstoneEvent blockRedstoneEvent = new GameEventArgs.BlockRedstoneEvent();
        blockRedstoneEvent.block = e.getBlock();
        return eventType.createEvent(blockRedstoneEvent);
    }

    private void cancelBlockRedstoneEvent(BlockRedstoneEvent bukkitEvent, GameEvent<GameEventArgs.BlockRedstoneEvent> gameEvent) {
        if (gameEvent.isCancelled())
            bukkitEvent.setNewCurrent(0);
    }

    private GameEvent<GameEventArgs.BlockSpreadEvent> createGameEvent(GameEventType<GameEventArgs.BlockSpreadEvent> eventType, GameEventPriority priority, BlockSpreadEvent e) {
        GameEventArgs.BlockSpreadEvent blockSpreadEvent = new GameEventArgs.BlockSpreadEvent();
        blockSpreadEvent.block = e.getBlock();
        blockSpreadEvent.newState = e.getNewState();
        blockSpreadEvent.source = e.getSource();
        return eventType.createEvent(blockSpreadEvent);
    }

    private GameEvent<GameEventArgs.LeavesDecayEvent> createGameEvent(GameEventType<GameEventArgs.LeavesDecayEvent> eventType, GameEventPriority priority, LeavesDecayEvent e) {
        GameEventArgs.LeavesDecayEvent leavesDecayEvent = new GameEventArgs.LeavesDecayEvent();
        leavesDecayEvent.block = e.getBlock();
        return eventType.createEvent(leavesDecayEvent);
    }

    private GameEvent<GameEventArgs.PistonExtendEvent> createGameEvent(GameEventType<GameEventArgs.PistonExtendEvent> eventType, GameEventPriority priority, BlockPistonExtendEvent e) {
        GameEventArgs.PistonExtendEvent pistonExtendEvent = new GameEventArgs.PistonExtendEvent();
        pistonExtendEvent.block = e.getBlock();
        pistonExtendEvent.blocks = e.getBlocks();
        pistonExtendEvent.direction = e.getDirection();
        return eventType.createEvent(pistonExtendEvent);
    }

    private GameEvent<GameEventArgs.PistonRetractEvent> createGameEvent(GameEventType<GameEventArgs.PistonRetractEvent> eventType, GameEventPriority priority, BlockPistonRetractEvent e) {
        GameEventArgs.PistonRetractEvent pistonRetractEvent = new GameEventArgs.PistonRetractEvent();
        pistonRetractEvent.block = e.getBlock();
        pistonRetractEvent.blocks = e.getBlocks();
        pistonRetractEvent.direction = e.getDirection();
        return eventType.createEvent(pistonRetractEvent);
    }

    /*
     * WORLD EVENTS
     */

    private GameEvent<GameEventArgs.ChunkLoadEvent> createGameEvent(GameEventType<GameEventArgs.ChunkLoadEvent> eventType, GameEventPriority priority, ChunkLoadEvent e) {
        GameEventArgs.ChunkLoadEvent chunkLoadEvent = new GameEventArgs.ChunkLoadEvent();
        chunkLoadEvent.chunk = e.getChunk();
        chunkLoadEvent.isNewChunk = e.isNewChunk();
        return eventType.createEvent(chunkLoadEvent);
    }

    private GameEvent<GameEventArgs.ChunkUnloadEvent> createGameEvent(GameEventType<GameEventArgs.ChunkUnloadEvent> eventType, GameEventPriority priority, ChunkUnloadEvent e) {
        GameEventArgs.ChunkUnloadEvent chunkUnloadEvent = new GameEventArgs.ChunkUnloadEvent();
        chunkUnloadEvent.chunk = e.getChunk();
        return eventType.createEvent(chunkUnloadEvent);
    }

    private GameEvent<GameEventArgs.SignChangeEvent> createGameEvent(GameEventType<GameEventArgs.SignChangeEvent> eventType, GameEventPriority priority, SignChangeEvent e) {
        GameEventArgs.SignChangeEvent signChangeEvent = new GameEventArgs.SignChangeEvent();
        signChangeEvent.block = e.getBlock();
        signChangeEvent.player = e.getPlayer();
        signChangeEvent.lines = e.getLines();
        return eventType.createEvent(signChangeEvent);
    }

    private GameEvent<GameEventArgs.StructureGrowEvent> createGameEvent(GameEventType<GameEventArgs.StructureGrowEvent> eventType, GameEventPriority priority, StructureGrowEvent e) {
        GameEventArgs.StructureGrowEvent structureGrowEvent = new GameEventArgs.StructureGrowEvent();
        structureGrowEvent.world = e.getWorld();
        structureGrowEvent.location = e.getLocation();
        structureGrowEvent.blocks = e.getBlocks();
        return eventType.createEvent(structureGrowEvent);
    }

    private GameEvent<GameEventArgs.WorldUnloadEvent> createGameEvent(GameEventType<GameEventArgs.WorldUnloadEvent> eventType, GameEventPriority priority, WorldUnloadEvent e) {
        GameEventArgs.WorldUnloadEvent worldUnloadEvent = new GameEventArgs.WorldUnloadEvent();
        worldUnloadEvent.world = e.getWorld();
        return eventType.createEvent(worldUnloadEvent);
    }

    /*
     * ENTITY EVENTS
     */

    private GameEvent<GameEventArgs.EntityBlockFormEvent> createGameEvent(GameEventType<GameEventArgs.EntityBlockFormEvent> eventType, GameEventPriority priority, EntityBlockFormEvent e) {
        GameEventArgs.EntityBlockFormEvent entityBlockFormEvent = new GameEventArgs.EntityBlockFormEvent();
        entityBlockFormEvent.block = e.getBlock();
        entityBlockFormEvent.newState = e.getNewState();
        entityBlockFormEvent.entity = e.getEntity();
        return eventType.createEvent(entityBlockFormEvent);
    }

    private GameEvent<GameEventArgs.EntityChangeBlockEvent> createGameEvent(GameEventType<GameEventArgs.EntityChangeBlockEvent> eventType, GameEventPriority priority, EntityChangeBlockEvent e) {
        GameEventArgs.EntityChangeBlockEvent entityChangeBlockEvent = new GameEventArgs.EntityChangeBlockEvent();

        entityChangeBlockEvent.entity = e.getEntity();
        entityChangeBlockEvent.block = e.getBlock();
        if (ServerVersion.isLegacy()) {
            // noinspection deprecated
            entityChangeBlockEvent.newType = Keys.of(e.getTo(), e.getData());
        } else {
            entityChangeBlockEvent.newType = Keys.of(e.getTo(), (byte) 0);
        }

        return eventType.createEvent(entityChangeBlockEvent);
    }

    private GameEvent<GameEventArgs.EntityCollisionEvent> createGameEvent(GameEventType<GameEventArgs.EntityCollisionEvent> eventType, GameEventPriority priority, VehicleEntityCollisionEvent e) {
        GameEventArgs.EntityCollisionEvent entityCollisionEvent = new GameEventArgs.EntityCollisionEvent();
        entityCollisionEvent.entity = e.getEntity();
        entityCollisionEvent.target = e.getVehicle();
        return eventType.createEvent(entityCollisionEvent);
    }

    private GameEvent<GameEventArgs.EntityDamageEvent> createGameEvent(GameEventType<GameEventArgs.EntityDamageEvent> eventType, GameEventPriority priority, EntityDamageByEntityEvent e) {
        GameEventArgs.EntityDamageEvent entityDamageEvent = new GameEventArgs.EntityDamageEvent();
        entityDamageEvent.entity = e.getEntity();
        entityDamageEvent.damageCause = e.getCause();
        entityDamageEvent.damager = e.getDamager();
        return eventType.createEvent(entityDamageEvent);
    }

    private GameEvent<GameEventArgs.EntityDamageEvent> createGameEvent(GameEventType<GameEventArgs.EntityDamageEvent> eventType, GameEventPriority priority, VehicleDamageEvent e) {
        GameEventArgs.EntityDamageEvent entityDamageEvent = new GameEventArgs.EntityDamageEvent();
        entityDamageEvent.entity = e.getVehicle();
        entityDamageEvent.damager = e.getAttacker();
        entityDamageEvent.damageCause = EntityDamageEvent.DamageCause.ENTITY_ATTACK;
        return eventType.createEvent(entityDamageEvent);
    }

    private GameEvent<GameEventArgs.EntityEnterPortalEvent> createGameEvent(GameEventType<GameEventArgs.EntityEnterPortalEvent> eventType, GameEventPriority priority, EntityPortalEnterEvent e) {
        GameEventArgs.EntityEnterPortalEvent entityEnterPortalEvent = new GameEventArgs.EntityEnterPortalEvent();
        entityEnterPortalEvent.entity = e.getEntity();
        entityEnterPortalEvent.portalLocation = e.getLocation();
        return eventType.createEvent(entityEnterPortalEvent);
    }

    private GameEvent<GameEventArgs.EntityExplodeEvent> createGameEvent(GameEventType<GameEventArgs.EntityExplodeEvent> eventType, GameEventPriority priority, EntityExplodeEvent e) {
        GameEventArgs.EntityExplodeEvent entityExplodeEvent = new GameEventArgs.EntityExplodeEvent();
        entityExplodeEvent.entity = e.getEntity();
        entityExplodeEvent.blocks = e.blockList();
        entityExplodeEvent.isSoftExplosion = e.getEntityType() == WIND_CHARGE || e.getEntityType() == BREEZE_WIND_CHARGE;
        return eventType.createEvent(entityExplodeEvent);
    }

    private GameEvent<GameEventArgs.EntityMoveEvent> createGameEvent(GameEventType<GameEventArgs.EntityMoveEvent> eventType, GameEventPriority priority, PlayerMoveEvent e) {
        Location from = e.getFrom();
        Location to = e.getTo();

        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ() && from.getBlockY() == to.getBlockY())
            return null;

        GameEventArgs.EntityMoveEvent entityMoveEvent = new GameEventArgs.EntityMoveEvent();
        entityMoveEvent.entity = e.getPlayer();
        entityMoveEvent.from = e.getFrom();
        entityMoveEvent.to = e.getTo();
        return eventType.createEvent(entityMoveEvent);
    }

    private GameEvent<GameEventArgs.EntityMoveEvent> createGameEvent(GameEventType<GameEventArgs.EntityMoveEvent> eventType, GameEventPriority priority, VehicleMoveEvent e) {
        Location from = e.getFrom();
        Location to = e.getTo();

        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ() && from.getBlockY() == to.getBlockY())
            return null;

        GameEventArgs.EntityMoveEvent entityMoveEvent = new GameEventArgs.EntityMoveEvent();
        entityMoveEvent.entity = e.getVehicle();
        entityMoveEvent.from = e.getFrom();
        entityMoveEvent.to = e.getTo();
        return eventType.createEvent(entityMoveEvent);
    }

    private GameEvent<GameEventArgs.EntityPortalEvent> createGameEvent(GameEventType<GameEventArgs.EntityPortalEvent> eventType, GameEventPriority priority, EntityPortalEvent e) {
        GameEventArgs.EntityPortalEvent entityPortalEvent = new GameEventArgs.EntityPortalEvent();
        entityPortalEvent.entity = e.getEntity();
        entityPortalEvent.cause = e.getTo().getWorld().getEnvironment() == World.Environment.THE_END ||
                e.getFrom().getWorld().getEnvironment() == World.Environment.THE_END ?
                PlayerTeleportEvent.TeleportCause.END_PORTAL : PlayerTeleportEvent.TeleportCause.NETHER_PORTAL;
        entityPortalEvent.from = e.getFrom();
        entityPortalEvent.to = e.getTo();
        return eventType.createEvent(entityPortalEvent);
    }

    private GameEvent<GameEventArgs.EntityPortalEvent> createGameEvent(GameEventType<GameEventArgs.EntityPortalEvent> eventType, GameEventPriority priority, PlayerPortalEvent e) {
        GameEventArgs.EntityPortalEvent entityPortalEvent = new GameEventArgs.EntityPortalEvent();
        entityPortalEvent.entity = e.getPlayer();
        entityPortalEvent.cause = e.getTo() == null ? e.getCause() :
                e.getTo().getWorld().getEnvironment() == World.Environment.THE_END ||
                        e.getFrom().getWorld().getEnvironment() == World.Environment.THE_END ?
                        PlayerTeleportEvent.TeleportCause.END_PORTAL : PlayerTeleportEvent.TeleportCause.NETHER_PORTAL;
        entityPortalEvent.from = e.getFrom();
        entityPortalEvent.to = e.getTo();
        return eventType.createEvent(entityPortalEvent);
    }

    private GameEvent<GameEventArgs.EntityRideEvent> createGameEvent(GameEventType<GameEventArgs.EntityRideEvent> eventType, GameEventPriority priority, VehicleEnterEvent e) {
        GameEventArgs.EntityRideEvent entityRideEvent = new GameEventArgs.EntityRideEvent();
        entityRideEvent.entity = e.getEntered();
        entityRideEvent.vehicle = e.getVehicle();
        return eventType.createEvent(entityRideEvent);
    }

    private GameEvent<GameEventArgs.EntitySpawnEvent> createGameEvent(GameEventType<GameEventArgs.EntitySpawnEvent> eventType, GameEventPriority priority, CreatureSpawnEvent e) {
        if (this.shouldFireAttemptEntitySpawnEvent) {
            GameEventArgs.AttemptEntitySpawnEvent attemptEntitySpawnEvent = new GameEventArgs.AttemptEntitySpawnEvent();
            attemptEntitySpawnEvent.entityType = e.getEntityType();
            attemptEntitySpawnEvent.spawnLocation = e.getLocation();
            attemptEntitySpawnEvent.spawnReason = e.getSpawnReason();
            GameEvent<GameEventArgs.AttemptEntitySpawnEvent> gameEvent = GameEventType.ATTEMPT_ENTITY_SPAWN_EVENT
                    .createEvent(attemptEntitySpawnEvent);
            plugin.getGameEventsDispatcher().onGameEvent(gameEvent, priority);
            if (gameEvent.isCancelled()) {
                e.getEntity().remove();
                return null;
            }
        }

        GameEventArgs.EntitySpawnEvent entitySpawnEvent = new GameEventArgs.EntitySpawnEvent();
        entitySpawnEvent.entity = e.getEntity();
        entitySpawnEvent.spawnReason = e.getSpawnReason();
        return eventType.createEvent(entitySpawnEvent);
    }

    private GameEvent<GameEventArgs.EntitySpawnEvent> createGameEvent(GameEventType<GameEventArgs.EntitySpawnEvent> eventType, GameEventPriority priority, HangingPlaceEvent e) {
        GameEventArgs.EntitySpawnEvent entitySpawnEvent = new GameEventArgs.EntitySpawnEvent();
        entitySpawnEvent.entity = e.getEntity();
        entitySpawnEvent.spawnReason = CreatureSpawnEvent.SpawnReason.DEFAULT;
        return eventType.createEvent(entitySpawnEvent);
    }

    private GameEvent<GameEventArgs.EntitySpawnEvent> createGameEvent(GameEventType<GameEventArgs.EntitySpawnEvent> eventType, GameEventPriority priority, ItemSpawnEvent e) {
        GameEventArgs.EntitySpawnEvent entitySpawnEvent = new GameEventArgs.EntitySpawnEvent();
        entitySpawnEvent.entity = e.getEntity();
        entitySpawnEvent.spawnReason = CreatureSpawnEvent.SpawnReason.DEFAULT;
        return eventType.createEvent(entitySpawnEvent);
    }

    private GameEvent<GameEventArgs.EntitySpawnEvent> createGameEvent(GameEventType<GameEventArgs.EntitySpawnEvent> eventType, GameEventPriority priority, VehicleCreateEvent e) {
        GameEventArgs.EntitySpawnEvent entitySpawnEvent = new GameEventArgs.EntitySpawnEvent();
        entitySpawnEvent.entity = e.getVehicle();
        entitySpawnEvent.spawnReason = CreatureSpawnEvent.SpawnReason.DEFAULT;
        return eventType.createEvent(entitySpawnEvent);
    }

    private void cancelVehicleCreateEvent(VehicleCreateEvent bukkitEvent, GameEvent<GameEventArgs.EntitySpawnEvent> gameEvent) {
        if (gameEvent.isCancelled())
            bukkitEvent.getVehicle().remove();
    }

    private GameEvent<GameEventArgs.EntityTargetEvent> createGameEvent(GameEventType<GameEventArgs.EntityTargetEvent> eventType, GameEventPriority priority, EntityTargetEvent e) {
        GameEventArgs.EntityTargetEvent entityTargetEvent = new GameEventArgs.EntityTargetEvent();
        entityTargetEvent.entity = e.getEntity();
        entityTargetEvent.target = e.getTarget();
        return eventType.createEvent(entityTargetEvent);
    }

    private GameEvent<GameEventArgs.EntityTeleportEvent> createGameEvent(GameEventType<GameEventArgs.EntityTeleportEvent> eventType, GameEventPriority priority, EntityTeleportEvent e) {
        GameEventArgs.EntityTeleportEvent entityTeleportEvent = new GameEventArgs.EntityTeleportEvent();
        entityTeleportEvent.entity = e.getEntity();
        entityTeleportEvent.from = e.getFrom();
        entityTeleportEvent.to = e.getTo();
        entityTeleportEvent.cause = PlayerTeleportEvent.TeleportCause.UNKNOWN;
        return eventType.createEvent(entityTeleportEvent);
    }

    private GameEvent<GameEventArgs.EntityTeleportEvent> createGameEvent(GameEventType<GameEventArgs.EntityTeleportEvent> eventType, GameEventPriority priority, PlayerTeleportEvent e) {
        GameEventArgs.EntityTeleportEvent entityTeleportEvent = new GameEventArgs.EntityTeleportEvent();
        entityTeleportEvent.entity = e.getPlayer();
        entityTeleportEvent.from = e.getFrom();
        entityTeleportEvent.to = e.getTo();
        entityTeleportEvent.cause = e.getCause();
        return eventType.createEvent(entityTeleportEvent);
    }

    private GameEvent<GameEventArgs.HangingBreakEvent> createGameEvent(GameEventType<GameEventArgs.HangingBreakEvent> eventType, GameEventPriority priority, HangingBreakByEntityEvent e) {
        GameEventArgs.HangingBreakEvent hangingBreakEvent = new GameEventArgs.HangingBreakEvent();
        hangingBreakEvent.entity = e.getEntity();
        hangingBreakEvent.remover = e.getRemover();
        hangingBreakEvent.removeCause = e.getCause();
        return eventType.createEvent(hangingBreakEvent);
    }

    private GameEvent<GameEventArgs.HangingPlaceEvent> createGameEvent2(GameEventType<GameEventArgs.HangingPlaceEvent> eventType, GameEventPriority priority, HangingPlaceEvent e) {
        GameEventArgs.HangingPlaceEvent hangingPlaceEvent = new GameEventArgs.HangingPlaceEvent();
        hangingPlaceEvent.entity = e.getEntity();
        hangingPlaceEvent.player = e.getPlayer();
        return eventType.createEvent(hangingPlaceEvent);
    }

    private GameEvent<GameEventArgs.ProjectileHitEvent> createGameEvent(GameEventType<GameEventArgs.ProjectileHitEvent> eventType, GameEventPriority priority, ProjectileHitEvent e) {
        GameEventArgs.ProjectileHitEvent projectileHitEvent = new GameEventArgs.ProjectileHitEvent();
        projectileHitEvent.entity = e.getEntity();
        if (PROJECTILE_HIT_TARGET_ENTITY.isValid())
            projectileHitEvent.hitEntity = PROJECTILE_HIT_TARGET_ENTITY.invoke(e);
        if (PROJECTILE_HIT_EVENT_TARGET_BLOCK.isValid())
            projectileHitEvent.hitBlock = PROJECTILE_HIT_EVENT_TARGET_BLOCK.invoke(e);
        return eventType.createEvent(projectileHitEvent);
    }

    private GameEvent<GameEventArgs.ProjectileLaunchEvent> createGameEvent(GameEventType<GameEventArgs.ProjectileLaunchEvent> eventType, GameEventPriority priority, ProjectileLaunchEvent e) {
        GameEventArgs.ProjectileLaunchEvent projectileLaunchEvent = new GameEventArgs.ProjectileLaunchEvent();
        projectileLaunchEvent.entity = e.getEntity();
        return eventType.createEvent(projectileLaunchEvent);
    }

    private GameEvent<GameEventArgs.EntityDeathEvent> createGameEvent(GameEventType<GameEventArgs.EntityDeathEvent> eventType, GameEventPriority priority, EntityDeathEvent e) {
        GameEventArgs.EntityDeathEvent entityDeathEvent = new GameEventArgs.EntityDeathEvent();
        entityDeathEvent.entity = e.getEntity();
        return eventType.createEvent(entityDeathEvent);
    }

    /*
     * INVENTORY EVENTS
     */

    private GameEvent<GameEventArgs.InventoryClickEvent> createGameEvent(GameEventType<GameEventArgs.InventoryClickEvent> eventType, GameEventPriority priority, InventoryClickEvent e) {
        GameEventArgs.InventoryClickEvent inventoryClickEvent = new GameEventArgs.InventoryClickEvent();
        inventoryClickEvent.bukkitEvent = e;
        return eventType.createEvent(inventoryClickEvent);
    }

    private GameEvent<GameEventArgs.InventoryCloseEvent> createGameEvent(GameEventType<GameEventArgs.InventoryCloseEvent> eventType, GameEventPriority priority, InventoryCloseEvent e) {
        GameEventArgs.InventoryCloseEvent inventoryCloseEvent = new GameEventArgs.InventoryCloseEvent();
        inventoryCloseEvent.bukkitEvent = e;
        return eventType.createEvent(inventoryCloseEvent);
    }

    private GameEvent<GameEventArgs.InventoryOpenEvent> createGameEvent(GameEventType<GameEventArgs.InventoryOpenEvent> eventType, GameEventPriority priority, InventoryOpenEvent e) {
        GameEventArgs.InventoryOpenEvent inventoryOpenEvent = new GameEventArgs.InventoryOpenEvent();
        inventoryOpenEvent.bukkitEvent = e;
        return eventType.createEvent(inventoryOpenEvent);
    }

    /*
     * PLAYER EVENTS
     */

    private GameEvent<GameEventArgs.PlayerChangedWorldEvent> createGameEvent(GameEventType<GameEventArgs.PlayerChangedWorldEvent> eventType, GameEventPriority priority, PlayerChangedWorldEvent e) {
        GameEventArgs.PlayerChangedWorldEvent playerChangedWorldEvent = new GameEventArgs.PlayerChangedWorldEvent();
        playerChangedWorldEvent.player = e.getPlayer();
        playerChangedWorldEvent.from = e.getFrom();
        return eventType.createEvent(playerChangedWorldEvent);
    }

    private GameEvent<GameEventArgs.PlayerCommandEvent> createGameEvent(GameEventType<GameEventArgs.PlayerCommandEvent> eventType, GameEventPriority priority, PlayerCommandPreprocessEvent e) {
        GameEventArgs.PlayerCommandEvent playerCommandEvent = new GameEventArgs.PlayerCommandEvent();
        playerCommandEvent.player = e.getPlayer();
        playerCommandEvent.command = e.getMessage();
        return eventType.createEvent(playerCommandEvent);
    }

    private GameEvent<GameEventArgs.PlayerDropItemEvent> createGameEvent(GameEventType<GameEventArgs.PlayerDropItemEvent> eventType, GameEventPriority priority, PlayerDropItemEvent e) {
        GameEventArgs.PlayerDropItemEvent playerDropItemEvent = new GameEventArgs.PlayerDropItemEvent();
        playerDropItemEvent.player = e.getPlayer();
        playerDropItemEvent.droppedItem = e.getItemDrop();
        return eventType.createEvent(playerDropItemEvent);
    }

    private GameEvent<GameEventArgs.PlayerEmptyBucketEvent> createGameEvent(GameEventType<GameEventArgs.PlayerEmptyBucketEvent> eventType, GameEventPriority priority, PlayerBucketEmptyEvent e) {
        GameEventArgs.PlayerEmptyBucketEvent playerEmptyBucketEvent = new GameEventArgs.PlayerEmptyBucketEvent();
        playerEmptyBucketEvent.player = e.getPlayer();
        playerEmptyBucketEvent.bucket = e.getBucket();
        playerEmptyBucketEvent.clickedBlock = e.getBlockClicked();
        return eventType.createEvent(playerEmptyBucketEvent);
    }

    private GameEvent<GameEventArgs.PlayerFillBucketEvent> createGameEvent(GameEventType<GameEventArgs.PlayerFillBucketEvent> eventType, GameEventPriority priority, PlayerBucketFillEvent e) {
        GameEventArgs.PlayerFillBucketEvent playerFillBucketEvent = new GameEventArgs.PlayerFillBucketEvent();
        playerFillBucketEvent.player = e.getPlayer();
        playerFillBucketEvent.bucket = e.getBucket();
        playerFillBucketEvent.clickedBlock = e.getBlockClicked();
        return eventType.createEvent(playerFillBucketEvent);
    }

    private GameEvent<GameEventArgs.PlayerGamemodeChangeEvent> createGameEvent(GameEventType<GameEventArgs.PlayerGamemodeChangeEvent> eventType, GameEventPriority priority, PlayerGameModeChangeEvent e) {
        GameEventArgs.PlayerGamemodeChangeEvent playerGamemodeChangeEvent = new GameEventArgs.PlayerGamemodeChangeEvent();
        playerGamemodeChangeEvent.player = e.getPlayer();
        playerGamemodeChangeEvent.newGamemode = e.getNewGameMode();
        return eventType.createEvent(playerGamemodeChangeEvent);
    }

    private GameEvent<GameEventArgs.PlayerInteractEvent> createGameEvent(GameEventType<GameEventArgs.PlayerInteractEvent> eventType, GameEventPriority priority, PlayerInteractAtEntityEvent e) {
        GameEventArgs.PlayerInteractEvent playerInteractEvent = new GameEventArgs.PlayerInteractEvent();
        playerInteractEvent.player = e.getPlayer();
        playerInteractEvent.action = Action.RIGHT_CLICK_AIR;
        playerInteractEvent.usedHand = BukkitItems.getHand(e);
        playerInteractEvent.usedItem = getHandItem(e.getPlayer(), playerInteractEvent.usedHand, true, null);
        playerInteractEvent.clickedEntity = e.getRightClicked();
        return eventType.createEvent(playerInteractEvent);
    }

    private GameEvent<GameEventArgs.PlayerInteractEvent> createGameEvent(GameEventType<GameEventArgs.PlayerInteractEvent> eventType, GameEventPriority priority, PlayerInteractEntityEvent e) {
        GameEventArgs.PlayerInteractEvent playerInteractEvent = new GameEventArgs.PlayerInteractEvent();
        playerInteractEvent.player = e.getPlayer();
        playerInteractEvent.action = Action.RIGHT_CLICK_AIR;
        playerInteractEvent.usedHand = BukkitItems.getHand(e);
        playerInteractEvent.usedItem = getHandItem(e.getPlayer(), playerInteractEvent.usedHand, true, null);
        playerInteractEvent.clickedEntity = e.getRightClicked();
        return eventType.createEvent(playerInteractEvent);
    }

    private GameEvent<GameEventArgs.PlayerInteractEvent> createGameEvent(GameEventType<GameEventArgs.PlayerInteractEvent> eventType, GameEventPriority priority, PlayerInteractEvent e) {
        GameEventArgs.PlayerInteractEvent playerInteractEvent = new GameEventArgs.PlayerInteractEvent();
        playerInteractEvent.player = e.getPlayer();
        playerInteractEvent.action = e.getAction();
        playerInteractEvent.usedHand = BukkitItems.getHand(e);
        playerInteractEvent.usedItem = getHandItem(e.getPlayer(), playerInteractEvent.usedHand, true, () -> e.getItem());
        playerInteractEvent.clickedBlock = e.getClickedBlock();
        return eventType.createEvent(playerInteractEvent);
    }

    private GameEvent<GameEventArgs.PlayerItemConsumeEvent> createGameEvent(GameEventType<GameEventArgs.PlayerItemConsumeEvent> eventType, GameEventPriority priority, PlayerItemConsumeEvent e) {
        GameEventArgs.PlayerItemConsumeEvent playerItemConsumeEvent = new GameEventArgs.PlayerItemConsumeEvent();
        playerItemConsumeEvent.player = e.getPlayer();
        playerItemConsumeEvent.consumedItem = e.getItem();
        return eventType.createEvent(playerItemConsumeEvent);
    }

    private GameEvent<GameEventArgs.PlayerJoinEvent> createGameEvent(GameEventType<GameEventArgs.PlayerJoinEvent> eventType, GameEventPriority priority, PlayerJoinEvent e) {
        GameEventArgs.PlayerJoinEvent playerJoinEvent = new GameEventArgs.PlayerJoinEvent();
        playerJoinEvent.player = e.getPlayer();
        return eventType.createEvent(playerJoinEvent);
    }

    private GameEvent<GameEventArgs.PlayerLeashEntityEvent> createGameEvent(GameEventType<GameEventArgs.PlayerLeashEntityEvent> eventType, GameEventPriority priority, PlayerLeashEntityEvent e) {
        GameEventArgs.PlayerLeashEntityEvent playerLeashEntityEvent = new GameEventArgs.PlayerLeashEntityEvent();
        playerLeashEntityEvent.player = e.getPlayer();
        playerLeashEntityEvent.entity = e.getEntity();
        return eventType.createEvent(playerLeashEntityEvent);
    }

    private GameEvent<GameEventArgs.PlayerLoginEvent> createGameEvent(GameEventType<GameEventArgs.PlayerLoginEvent> eventType, GameEventPriority priority, PlayerLoginEvent e) {
        GameEventArgs.PlayerLoginEvent playerLoginEvent = new GameEventArgs.PlayerLoginEvent();
        playerLoginEvent.player = e.getPlayer();
        return eventType.createEvent(playerLoginEvent);
    }

    private GameEvent<GameEventArgs.PlayerPickupItemEvent> createGameEvent(GameEventType<GameEventArgs.PlayerPickupItemEvent> eventType, GameEventPriority priority, PlayerPickupItemEvent e) {
        GameEventArgs.PlayerPickupItemEvent playerPickupItemEvent = new GameEventArgs.PlayerPickupItemEvent();
        playerPickupItemEvent.player = e.getPlayer();
        playerPickupItemEvent.pickedUpItem = e.getItem();
        return eventType.createEvent(playerPickupItemEvent);
    }

    private GameEvent<GameEventArgs.PlayerQuitEvent> createGameEvent(GameEventType<GameEventArgs.PlayerQuitEvent> eventType, GameEventPriority priority, PlayerQuitEvent e) {
        GameEventArgs.PlayerQuitEvent playerQuitEvent = new GameEventArgs.PlayerQuitEvent();
        playerQuitEvent.player = e.getPlayer();
        return eventType.createEvent(playerQuitEvent);
    }

    private GameEvent<GameEventArgs.PlayerRespawnEvent> createGameEvent(GameEventType<GameEventArgs.PlayerRespawnEvent> eventType, GameEventPriority priority, PlayerRespawnEvent e) {
        GameEventArgs.PlayerRespawnEvent playerRespawnEvent = new GameEventArgs.PlayerRespawnEvent();
        playerRespawnEvent.player = e.getPlayer();
        playerRespawnEvent.bukkitEvent = e;
        return eventType.createEvent(playerRespawnEvent);
    }

    private GameEvent<GameEventArgs.PlayerShearEntityEvent> createGameEvent(GameEventType<GameEventArgs.PlayerShearEntityEvent> eventType, GameEventPriority priority, PlayerShearEntityEvent e) {
        GameEventArgs.PlayerShearEntityEvent playerShearEntityEvent = new GameEventArgs.PlayerShearEntityEvent();
        playerShearEntityEvent.player = e.getPlayer();
        playerShearEntityEvent.entity = e.getEntity();
        return eventType.createEvent(playerShearEntityEvent);
    }

    private GameEvent<GameEventArgs.PlayerUnleashEntityEvent> createGameEvent(GameEventType<GameEventArgs.PlayerUnleashEntityEvent> eventType, GameEventPriority priority, PlayerUnleashEntityEvent e) {
        GameEventArgs.PlayerUnleashEntityEvent playerUnleashEntityEvent = new GameEventArgs.PlayerUnleashEntityEvent();
        playerUnleashEntityEvent.player = e.getPlayer();
        playerUnleashEntityEvent.entity = e.getEntity();
        return eventType.createEvent(playerUnleashEntityEvent);
    }

    private GameEvent<GameEventArgs.PlayerChatEvent> createGameEvent(GameEventType<GameEventArgs.PlayerChatEvent> eventType, GameEventPriority priority, AsyncPlayerChatEvent e) {
        GameEventArgs.PlayerChatEvent playerChatEvent = new GameEventArgs.PlayerChatEvent();
        playerChatEvent.player = e.getPlayer();
        playerChatEvent.message = e.getMessage();
        playerChatEvent.format = e.getFormat();
        return eventType.createEvent(playerChatEvent);
    }

    /*
     * INTERNALS
     */

    private <E extends Event, Args extends IEventArgs> void createEventListener(GameEventType<Args> eventType,
                                                                                Class<E> bukkitEventClass,
                                                                                GameEventCreator<Args, E> function) {
        createEventListener(eventType, bukkitEventClass, function, null);
    }

    private <E extends Event, Args extends IEventArgs> void createEventListener(GameEventType<Args> eventType,
                                                                                Class<E> bukkitEventClass,
                                                                                GameEventCreator<Args, E> function,
                                                                                @Nullable ApplyBukkitEventFunction<E, Args> applyBukkitEventFunction) {
        Map<GameEventPriority, List<EventCallback>> callbacks = plugin.getGameEventsDispatcher().getCallbacks(eventType);
        if (!callbacks.isEmpty()) {
            callbacks.keySet().forEach(priority ->
                    createEventListenerForPriority(eventType, bukkitEventClass, priority, function, applyBukkitEventFunction));
        }
    }

    private <E extends Event, Args extends IEventArgs> void createEventListenerForPriority(GameEventType<Args> eventType,
                                                                                           Class<E> bukkitEventClass,
                                                                                           GameEventPriority gameEventPriority,
                                                                                           GameEventCreator<Args, E> function,
                                                                                           @Nullable ApplyBukkitEventFunction<E, Args> applyBukkitEventFunction) {
        EventPriority bukkitEventPriority = EventPriority.valueOf(gameEventPriority.name());
        plugin.getServer().getPluginManager().registerEvent(bukkitEventClass, this, bukkitEventPriority, (listener, event) -> {
            if (!bukkitEventClass.isAssignableFrom(event.getClass()))
                return;

            IEventsDispatcher customEventsDispatcher = plugin.getEventsDispatcher();
            if (customEventsDispatcher != null) {
                if (customEventsDispatcher.notifyEvent(event, bukkitEventPriority))
                    return;
                if (!customEventsDispatcher.shouldFallbackToDefaultExecutorOnFailure())
                    return;
            }

            GameEvent<Args> gameEvent = function.execute(eventType, gameEventPriority, (E) event);
            if (gameEvent == null)
                return;

            boolean cancelledBeforeDispatch = false;

            if (event instanceof Cancellable && ((Cancellable) event).isCancelled()) {
                gameEvent.setCancelled();
                cancelledBeforeDispatch = true;
            }

            plugin.getGameEventsDispatcher().onGameEvent(gameEvent, gameEventPriority);
            if (!cancelledBeforeDispatch && gameEvent.isCancelled()) {
                if (event instanceof Cancellable)
                    ((Cancellable) event).setCancelled(true);
            }
            if (applyBukkitEventFunction != null)
                applyBukkitEventFunction.apply((E) event, gameEvent);
        }, plugin, false);
    }

    private static ItemStack getHandItem(Player player, PlayerHand usedHand, boolean clone, @Nullable Supplier<ItemStack> defItem) {
        ItemStack itemStack = BukkitItems.getHandItem(player, usedHand);
        if (clone && itemStack != null) {
            itemStack = itemStack.clone();
        }
        if (defItem != null && (itemStack == null || itemStack.getType() == Material.AIR))
            itemStack = defItem.get();
        return itemStack;
    }

    private interface GameEventCreator<Args extends IEventArgs, E extends Event> {

        @Nullable
        GameEvent<Args> execute(GameEventType<Args> eventType, GameEventPriority priority, E e);

    }

    private interface ApplyBukkitEventFunction<E, Args extends IEventArgs> {

        void apply(E bukkitEvent, GameEvent<Args> event);

    }

    /*
     * SPECIAL EVENTS
     */

    private static class SpongeAbsorbEventFunction implements GameEventCreator<GameEventArgs.SpongeAbsorbEvent, org.bukkit.event.block.SpongeAbsorbEvent> {

        @Override
        public GameEvent<GameEventArgs.SpongeAbsorbEvent> execute(GameEventType<GameEventArgs.SpongeAbsorbEvent> eventType, GameEventPriority priority, org.bukkit.event.block.SpongeAbsorbEvent e) {
            GameEventArgs.SpongeAbsorbEvent spongeAbsorbEvent = new GameEventArgs.SpongeAbsorbEvent();
            spongeAbsorbEvent.block = e.getBlock();
            spongeAbsorbEvent.blocks = e.getBlocks();
            return eventType.createEvent(spongeAbsorbEvent);
        }
    }

    private static class BlockDestroyEventFunction implements GameEventCreator<GameEventArgs.BlockDestroyEvent, com.destroystokyo.paper.event.block.BlockDestroyEvent> {

        @Override
        public GameEvent<GameEventArgs.BlockDestroyEvent> execute(GameEventType<GameEventArgs.BlockDestroyEvent> eventType, GameEventPriority priority, com.destroystokyo.paper.event.block.BlockDestroyEvent e) {
            if (e.getNewState().getMaterial() != Material.AIR)
                return null;

            GameEventArgs.BlockDestroyEvent blockDestroyEvent = new GameEventArgs.BlockDestroyEvent();
            blockDestroyEvent.block = e.getBlock();
            return eventType.createEvent(blockDestroyEvent);
        }
    }

    private class EntityRemoveFromWorldEventFunction implements GameEventCreator<GameEventArgs.EntityDeathEvent, com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent> {

        @Override
        public GameEvent<GameEventArgs.EntityDeathEvent> execute(GameEventType<GameEventArgs.EntityDeathEvent> eventType, GameEventPriority priority, com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent e) {
            Location entityLocation = e.getEntity().getLocation();

            BukkitExecutor.sync(() -> {
                if (e.getEntity().isValid() && !e.getEntity().isDead())
                    return;

                World world = entityLocation.getWorld();
                int chunkX = entityLocation.getBlockX() >> 4;
                int chunkZ = entityLocation.getBlockZ() >> 4;
                // We don't want call this event if
                if (world.isChunkLoaded(chunkX, chunkZ)) {
                    GameEventArgs.EntityDeathEvent entityDeathEvent = new GameEventArgs.EntityDeathEvent();
                    entityDeathEvent.entity = e.getEntity();
                    GameEvent<GameEventArgs.EntityDeathEvent> gameEvent = eventType.createEvent(entityDeathEvent);
                    plugin.getGameEventsDispatcher().onGameEvent(gameEvent, priority);
                }
            }, 1L);

            return null;
        }
    }

    private static class AttemptEntitySpawnEventFunction implements GameEventCreator<GameEventArgs.AttemptEntitySpawnEvent, com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent> {

        @Override
        public GameEvent<GameEventArgs.AttemptEntitySpawnEvent> execute(GameEventType<GameEventArgs.AttemptEntitySpawnEvent> eventType, GameEventPriority priority, com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent e) {
            GameEventArgs.AttemptEntitySpawnEvent attemptEntitySpawnEvent = new GameEventArgs.AttemptEntitySpawnEvent();
            attemptEntitySpawnEvent.entityType = e.getType();
            attemptEntitySpawnEvent.spawnLocation = e.getSpawnLocation();
            attemptEntitySpawnEvent.spawnReason = e.getReason();
            return eventType.createEvent(attemptEntitySpawnEvent);
        }

    }

    private class AsyncChatEventFunctions implements
            GameEventCreator<GameEventArgs.PlayerChatEvent, io.papermc.paper.event.player.AsyncChatEvent>,
            ApplyBukkitEventFunction<io.papermc.paper.event.player.AsyncChatEvent, GameEventArgs.PlayerChatEvent> {

        @Override
        public GameEvent<GameEventArgs.PlayerChatEvent> execute(GameEventType<GameEventArgs.PlayerChatEvent> eventType, GameEventPriority priority, io.papermc.paper.event.player.AsyncChatEvent e) {
            GameEventArgs.PlayerChatEvent playerChatEvent = new GameEventArgs.PlayerChatEvent();
            playerChatEvent.player = e.getPlayer();
            playerChatEvent.message = LegacyComponentSerializer.legacyAmpersand().serialize(e.message());
            return eventType.createEvent(playerChatEvent);
        }

        @Override
        public void apply(io.papermc.paper.event.player.AsyncChatEvent bukkitEvent, GameEvent<GameEventArgs.PlayerChatEvent> event) {
            plugin.getNMSAlgorithms().handlePaperChatRenderer(bukkitEvent);
        }

    }

    private static class PlayerPickupArrowEventFunctions implements GameEventCreator<GameEventArgs.PlayerPickupArrowEvent, org.bukkit.event.player.PlayerPickupArrowEvent> {

        @Override
        public GameEvent<GameEventArgs.PlayerPickupArrowEvent> execute(GameEventType<GameEventArgs.PlayerPickupArrowEvent> eventType, GameEventPriority priority, org.bukkit.event.player.PlayerPickupArrowEvent e) {
            GameEventArgs.PlayerPickupArrowEvent playerPickupArrowEvent = new GameEventArgs.PlayerPickupArrowEvent();
            playerPickupArrowEvent.player = e.getPlayer();
            playerPickupArrowEvent.pickedUpItem = e.getItem();
            return eventType.createEvent(playerPickupArrowEvent);
        }

    }

    private static class PlayerPickupItemEventFunctions implements GameEventCreator<GameEventArgs.PlayerPickupItemEvent, org.bukkit.event.player.PlayerAttemptPickupItemEvent> {

        @Override
        public GameEvent<GameEventArgs.PlayerPickupItemEvent> execute(GameEventType<GameEventArgs.PlayerPickupItemEvent> eventType, GameEventPriority priority, org.bukkit.event.player.PlayerAttemptPickupItemEvent e) {
            GameEventArgs.PlayerPickupItemEvent playerPickupItemEvent = new GameEventArgs.PlayerPickupItemEvent();
            playerPickupItemEvent.player = e.getPlayer();
            playerPickupItemEvent.pickedUpItem = e.getItem();
            return eventType.createEvent(playerPickupItemEvent);
        }

    }

    private static class RaidTriggerEventFunctions implements GameEventCreator<GameEventArgs.RaidTriggerEvent, org.bukkit.event.raid.RaidTriggerEvent> {

        @Override
        public GameEvent<GameEventArgs.RaidTriggerEvent> execute(GameEventType<GameEventArgs.RaidTriggerEvent> eventType, GameEventPriority priority, org.bukkit.event.raid.RaidTriggerEvent e) {
            GameEventArgs.RaidTriggerEvent raidTriggerEvent = new GameEventArgs.RaidTriggerEvent();
            raidTriggerEvent.world = e.getWorld();
            raidTriggerEvent.player = e.getPlayer();
            raidTriggerEvent.raidLocation = e.getRaid().getLocation();
            return eventType.createEvent(raidTriggerEvent);
        }

    }

}

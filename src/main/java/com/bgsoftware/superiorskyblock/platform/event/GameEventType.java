package com.bgsoftware.superiorskyblock.platform.event;

import com.bgsoftware.superiorskyblock.core.events.EventType;
import com.bgsoftware.superiorskyblock.platform.event.args.IEventArgs;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.BlockBreakEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.BlockBurnEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.BlockDispenseEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.BlockFadeEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.BlockFormEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.BlockFromToEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.BlockGrowEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.BlockIgniteEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.BlockPhysicsEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.BlockPlaceEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.BlockRedstoneEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.BlockSpreadEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.BlockUpdateShapeEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.ChunkLoadEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.ChunkUnloadEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.EntityBlockFormEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.EntityChangeBlockEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.EntityCollisionEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.EntityDamageEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.EntityDeathEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.EntityEnterPortalEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.EntityExplodeEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.EntityInteractEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.EntityMoveEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.EntityPortalEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.EntityRideEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.EntitySpawnEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.EntityTargetEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.EntityTeleportEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.GenericGameEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.HangingBreakEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.HangingPlaceEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.InventoryClickEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.InventoryCloseEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.InventoryOpenEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.LeavesDecayEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.PistonExtendEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.PistonRetractEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.PlayerChangedWorldEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.PlayerChatEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.PlayerCommandEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.PlayerDropItemEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.PlayerEmptyBucketEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.PlayerFillBucketEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.PlayerGamemodeChangeEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.PlayerInteractEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.PlayerItemConsumeEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.PlayerJoinEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.PlayerLeashEntityEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.PlayerLoginEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.PlayerPickupArrowEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.PlayerPickupItemEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.PlayerQuitEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.PlayerRespawnEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.PlayerShearEntityEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.PlayerUnleashEntityEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.ProjectileHitEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.ProjectileLaunchEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.RaidTriggerEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.SignChangeEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.SpongeAbsorbEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.StructureGrowEvent;
import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.WorldUnloadEvent;

public class GameEventType<Args extends IEventArgs> extends EventType<Args, GameEvent<Args>> {

    private static final List<GameEventType<?>> ALL_TYPES = new LinkedList<>();

    // Block Events
    public static final GameEventType<BlockBreakEvent> BLOCK_BREAK_EVENT = register(BlockBreakEvent.class, GameEventFlags.BLOCK_EVENT | GameEventFlags.PLAYER_EVENT);
    public static final GameEventType<BlockBurnEvent> BLOCK_BURN_EVENT = register(BlockBurnEvent.class, GameEventFlags.BLOCK_EVENT);
    public static final GameEventType<BlockDispenseEvent> BLOCK_DISPENSE_EVENT = register(BlockDispenseEvent.class, GameEventFlags.BLOCK_EVENT);
    public static final GameEventType<BlockFadeEvent> BLOCK_FADE_EVENT = register(BlockFadeEvent.class, GameEventFlags.BLOCK_EVENT);
    public static final GameEventType<BlockFormEvent> BLOCK_FORM_EVENT = register(BlockFormEvent.class, GameEventFlags.BLOCK_EVENT);
    public static final GameEventType<BlockFromToEvent> BLOCK_FROM_TO_EVENT = register(BlockFromToEvent.class, GameEventFlags.BLOCK_EVENT);
    public static final GameEventType<BlockGrowEvent> BLOCK_GROW_EVENT = register(BlockGrowEvent.class, GameEventFlags.BLOCK_EVENT);
    public static final GameEventType<BlockIgniteEvent> BLOCK_IGNITE_EVENT = register(BlockIgniteEvent.class, GameEventFlags.BLOCK_EVENT);
    public static final GameEventType<BlockPhysicsEvent> BLOCK_PHYSICS_EVENT = register(BlockPhysicsEvent.class, GameEventFlags.BLOCK_EVENT);
    public static final GameEventType<BlockPlaceEvent> BLOCK_PLACE_EVENT = register(BlockPlaceEvent.class, GameEventFlags.BLOCK_EVENT | GameEventFlags.PLAYER_EVENT);
    public static final GameEventType<BlockRedstoneEvent> BLOCK_REDSTONE_EVENT = register(BlockRedstoneEvent.class, GameEventFlags.BLOCK_EVENT);
    public static final GameEventType<BlockSpreadEvent> BLOCK_SPREAD_EVENT = register(BlockSpreadEvent.class, GameEventFlags.BLOCK_EVENT);
    public static final GameEventType<BlockUpdateShapeEvent> BLOCK_UPDATE_SHAPE_EVENT = register(BlockUpdateShapeEvent.class, GameEventFlags.BLOCK_EVENT);
    public static final GameEventType<LeavesDecayEvent> LEAVES_DECAY_EVENT = register(LeavesDecayEvent.class, GameEventFlags.BLOCK_EVENT);
    public static final GameEventType<PistonExtendEvent> PISTON_EXTEND_EVENT = register(PistonExtendEvent.class, GameEventFlags.BLOCK_EVENT);
    public static final GameEventType<PistonRetractEvent> PISTON_RETRACT_EVENT = register(PistonRetractEvent.class, GameEventFlags.BLOCK_EVENT);
    public static final GameEventType<SignChangeEvent> SIGN_CHANGE_EVENT = register(SignChangeEvent.class, GameEventFlags.BLOCK_EVENT | GameEventFlags.PLAYER_EVENT);
    public static final GameEventType<SpongeAbsorbEvent> SPONGE_ABSORB_EVENT = register(SpongeAbsorbEvent.class, GameEventFlags.BLOCK_EVENT);
    public static final GameEventType<StructureGrowEvent> STRUCTURE_GROW_EVENT = register(StructureGrowEvent.class, GameEventFlags.BLOCK_EVENT);

    // World Events
    public static final GameEventType<ChunkLoadEvent> CHUNK_LOAD_EVENT = register(ChunkLoadEvent.class, GameEventFlags.GENERIC_WORLD_EVENT);
    public static final GameEventType<ChunkUnloadEvent> CHUNK_UNLOAD_EVENT = register(ChunkUnloadEvent.class, GameEventFlags.GENERIC_WORLD_EVENT);
    public static final GameEventType<WorldUnloadEvent> WORLD_UNLOAD_EVENT = register(WorldUnloadEvent.class, GameEventFlags.GENERIC_WORLD_EVENT);
    public static final GameEventType<RaidTriggerEvent> RAID_TRIGGER_EVENT = register(RaidTriggerEvent.class, GameEventFlags.GENERIC_WORLD_EVENT | GameEventFlags.PLAYER_EVENT);
    public static final GameEventType<GenericGameEvent> GENERIC_GAME_EVENT = register(GenericGameEvent.class, GameEventFlags.GENERIC_WORLD_EVENT | GameEventFlags.MAYBE_BLOCK_EVENT | GameEventFlags.MAYBE_ENTITY_EVENT);

    // Entity Events
    public static final GameEventType<EntityBlockFormEvent> ENTITY_BLOCK_FORM_EVENT = register(EntityBlockFormEvent.class, GameEventFlags.BLOCK_EVENT | GameEventFlags.ENTITY_EVENT);
    public static final GameEventType<EntityChangeBlockEvent> ENTITY_CHANGE_BLOCK_EVENT = register(EntityChangeBlockEvent.class, GameEventFlags.BLOCK_EVENT | GameEventFlags.ENTITY_EVENT);
    public static final GameEventType<EntityCollisionEvent> ENTITY_COLLISION_EVENT = register(EntityCollisionEvent.class, GameEventFlags.ENTITY_EVENT);
    public static final GameEventType<EntityDamageEvent> ENTITY_DAMAGE_EVENT = register(EntityDamageEvent.class, GameEventFlags.ENTITY_EVENT);
    public static final GameEventType<EntityDeathEvent> ENTITY_DEATH_EVENT = register(EntityDeathEvent.class, GameEventFlags.ENTITY_EVENT);
    public static final GameEventType<EntityEnterPortalEvent> ENTITY_ENTER_PORTAL_EVENT = register(EntityEnterPortalEvent.class, GameEventFlags.ENTITY_EVENT);
    public static final GameEventType<EntityExplodeEvent> ENTITY_EXPLODE_EVENT = register(EntityExplodeEvent.class, GameEventFlags.BLOCK_EVENT | GameEventFlags.ENTITY_EVENT);
    public static final GameEventType<EntityInteractEvent> ENTITY_INTERACT_EVENT = register(EntityInteractEvent.class, GameEventFlags.ENTITY_EVENT | GameEventFlags.MAYBE_BLOCK_EVENT);
    public static final GameEventType<EntityMoveEvent> ENTITY_MOVE_EVENT = register(EntityMoveEvent.class, GameEventFlags.ENTITY_EVENT);
    public static final GameEventType<EntityPortalEvent> ENTITY_PORTAL_EVENT = register(EntityPortalEvent.class, GameEventFlags.ENTITY_EVENT);
    public static final GameEventType<EntityRideEvent> ENTITY_RIDE_EVENT = register(EntityRideEvent.class, GameEventFlags.ENTITY_EVENT);
    public static final GameEventType<EntitySpawnEvent> ENTITY_SPAWN_EVENT = register(EntitySpawnEvent.class, GameEventFlags.ENTITY_EVENT);
    public static final GameEventType<EntityTargetEvent> ENTITY_TARGET_EVENT = register(EntityTargetEvent.class, GameEventFlags.ENTITY_EVENT);
    public static final GameEventType<EntityTeleportEvent> ENTITY_TELEPORT_EVENT = register(EntityTeleportEvent.class, GameEventFlags.ENTITY_EVENT);
    public static final GameEventType<HangingBreakEvent> HANGING_BREAK_EVENT = register(HangingBreakEvent.class, GameEventFlags.ENTITY_EVENT);
    public static final GameEventType<HangingPlaceEvent> HANGING_PLACE_EVENT = register(HangingPlaceEvent.class, GameEventFlags.ENTITY_EVENT);
    public static final GameEventType<ProjectileHitEvent> PROJECTILE_HIT_EVENT = register(ProjectileHitEvent.class, GameEventFlags.ENTITY_EVENT | GameEventFlags.MAYBE_BLOCK_EVENT);
    public static final GameEventType<ProjectileLaunchEvent> PROJECTILE_LAUNCH_EVENT = register(ProjectileLaunchEvent.class, GameEventFlags.ENTITY_EVENT);

    // Inventory Events
    public static final GameEventType<InventoryClickEvent> INVENTORY_CLICK_EVENT = register(InventoryClickEvent.class, GameEventFlags.INVENTORY_EVENT | GameEventFlags.PLAYER_EVENT);
    public static final GameEventType<InventoryCloseEvent> INVENTORY_CLOSE_EVENT = register(InventoryCloseEvent.class, GameEventFlags.INVENTORY_EVENT | GameEventFlags.PLAYER_EVENT);
    public static final GameEventType<InventoryOpenEvent> INVENTORY_OPEN_EVENT = register(InventoryOpenEvent.class, GameEventFlags.INVENTORY_EVENT | GameEventFlags.PLAYER_EVENT);

    // Player Events
    public static final GameEventType<PlayerChangedWorldEvent> PLAYER_CHANGED_WORLD_EVENT = register(PlayerChangedWorldEvent.class, GameEventFlags.PLAYER_EVENT);
    public static final GameEventType<PlayerChatEvent> PLAYER_CHAT_EVENT = register(PlayerChatEvent.class, GameEventFlags.PLAYER_EVENT);
    public static final GameEventType<PlayerCommandEvent> PLAYER_COMMAND_EVENT = register(PlayerCommandEvent.class, GameEventFlags.PLAYER_EVENT);
    public static final GameEventType<PlayerDropItemEvent> PLAYER_DROP_ITEM_EVENT = register(PlayerDropItemEvent.class, GameEventFlags.PLAYER_EVENT | GameEventFlags.ENTITY_EVENT);
    public static final GameEventType<PlayerEmptyBucketEvent> PLAYER_EMPTY_BUCKET_EVENT = register(PlayerEmptyBucketEvent.class, GameEventFlags.PLAYER_EVENT | GameEventFlags.BLOCK_EVENT);
    public static final GameEventType<PlayerFillBucketEvent> PLAYER_FILL_BUCKET_EVENT = register(PlayerFillBucketEvent.class, GameEventFlags.PLAYER_EVENT | GameEventFlags.BLOCK_EVENT);
    public static final GameEventType<PlayerGamemodeChangeEvent> PLAYER_GAMEMODE_CHANGE = register(PlayerGamemodeChangeEvent.class, GameEventFlags.PLAYER_EVENT);
    public static final GameEventType<PlayerInteractEvent> PLAYER_INTERACT_EVENT = register(PlayerInteractEvent.class, GameEventFlags.PLAYER_EVENT | GameEventFlags.MAYBE_BLOCK_EVENT | GameEventFlags.MAYBE_ENTITY_EVENT);
    public static final GameEventType<PlayerItemConsumeEvent> PLAYER_ITEM_CONSUME_EVENT = register(PlayerItemConsumeEvent.class, GameEventFlags.PLAYER_EVENT);
    public static final GameEventType<PlayerJoinEvent> PLAYER_JOIN_EVENT = register(PlayerJoinEvent.class, GameEventFlags.PLAYER_EVENT);
    public static final GameEventType<PlayerLeashEntityEvent> PLAYER_LEASH_ENTITY_EVENT = register(PlayerLeashEntityEvent.class, GameEventFlags.PLAYER_EVENT | GameEventFlags.ENTITY_EVENT);
    public static final GameEventType<PlayerLoginEvent> PLAYER_LOGIN_EVENT = register(PlayerLoginEvent.class, GameEventFlags.PLAYER_EVENT);
    public static final GameEventType<PlayerPickupArrowEvent> PLAYER_PICKUP_ARROW_EVENT = register(PlayerPickupArrowEvent.class, GameEventFlags.PLAYER_EVENT | GameEventFlags.ENTITY_EVENT);
    public static final GameEventType<PlayerPickupItemEvent> PLAYER_PICKUP_ITEM_EVENT = register(PlayerPickupItemEvent.class, GameEventFlags.PLAYER_EVENT | GameEventFlags.ENTITY_EVENT);
    public static final GameEventType<PlayerQuitEvent> PLAYER_QUIT_EVENT = register(PlayerQuitEvent.class, GameEventFlags.PLAYER_EVENT);
    public static final GameEventType<PlayerRespawnEvent> PLAYER_RESPAWN_EVENT = register(PlayerRespawnEvent.class, GameEventFlags.PLAYER_EVENT);
    public static final GameEventType<PlayerShearEntityEvent> PLAYER_SHEAR_ENTITY_EVENT = register(PlayerShearEntityEvent.class, GameEventFlags.PLAYER_EVENT | GameEventFlags.ENTITY_EVENT);
    public static final GameEventType<PlayerUnleashEntityEvent> PLAYER_UNLEASH_ENTITY_EVENT = register(PlayerUnleashEntityEvent.class, GameEventFlags.PLAYER_EVENT | GameEventFlags.ENTITY_EVENT);

    @GameEventFlags
    private final int flags;

    private GameEventType(@GameEventFlags int flags) {
        this.flags = flags;
    }

    @GameEventFlags
    public int getFlags() {
        return this.flags;
    }

    public GameEvent<Args> createEvent(Args args) {
        return new GameEvent<>(this, args);
    }

    private static <Args extends IEventArgs> GameEventType<Args> register(Class<Args> eventArgsType, @GameEventFlags int flags) {
        GameEventType<Args> eventType = new GameEventType<>(flags);
        ALL_TYPES.add(eventType);
        return eventType;
    }

    public static Collection<GameEventType<?>> values() {
        return Collections.unmodifiableList(ALL_TYPES);
    }

}

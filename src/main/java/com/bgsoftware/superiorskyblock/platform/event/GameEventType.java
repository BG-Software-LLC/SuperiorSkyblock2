package com.bgsoftware.superiorskyblock.platform.event;

import com.bgsoftware.superiorskyblock.core.events.EventType;
import com.bgsoftware.superiorskyblock.platform.event.args.IEventArgs;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.*;

public class GameEventType<Args extends IEventArgs> extends EventType<Args, GameEvent<Args>> {

    private static final List<GameEventType<?>> ALL_TYPES = new LinkedList<>();

    // Block Events
    public static final GameEventType<BlockBreakEvent> BLOCK_BREAK_EVENT = register(BlockBreakEvent.class);
    public static final GameEventType<BlockBurnEvent> BLOCK_BURN_EVENT = register(BlockBurnEvent.class);
    public static final GameEventType<BlockDestroyEvent> BLOCK_DESTROY_EVENT = register(BlockDestroyEvent.class);
    public static final GameEventType<BlockDispenseEvent> BLOCK_DISPENSE_EVENT = register(BlockDispenseEvent.class);
    public static final GameEventType<BlockFadeEvent> BLOCK_FADE_EVENT = register(BlockFadeEvent.class);
    public static final GameEventType<BlockFormEvent> BLOCK_FORM_EVENT = register(BlockFormEvent.class);
    public static final GameEventType<BlockFromToEvent> BLOCK_FROM_TO_EVENT = register(BlockFromToEvent.class);
    public static final GameEventType<BlockGrowEvent> BLOCK_GROW_EVENT = register(BlockGrowEvent.class);
    public static final GameEventType<BlockIgniteEvent> BLOCK_IGNITE_EVENT = register(BlockIgniteEvent.class);
    public static final GameEventType<BlockPhysicsEvent> BLOCK_PHYSICS_EVENT = register(BlockPhysicsEvent.class);
    public static final GameEventType<BlockPlaceEvent> BLOCK_PLACE_EVENT = register(BlockPlaceEvent.class);
    public static final GameEventType<BlockRedstoneEvent> BLOCK_REDSTONE_EVENT = register(BlockRedstoneEvent.class);
    public static final GameEventType<BlockSpreadEvent> BLOCK_SPREAD_EVENT = register(BlockSpreadEvent.class);
    public static final GameEventType<LeavesDecayEvent> LEAVES_DECAY_EVENT = register(LeavesDecayEvent.class);
    public static final GameEventType<PistonExtendEvent> PISTON_EXTEND_EVENT = register(PistonExtendEvent.class);
    public static final GameEventType<PistonRetractEvent> PISTON_RETRACT_EVENT = register(PistonRetractEvent.class);

    // World Events
    public static final GameEventType<ChunkLoadEvent> CHUNK_LOAD_EVENT = register(ChunkLoadEvent.class);
    public static final GameEventType<ChunkUnloadEvent> CHUNK_UNLOAD_EVENT = register(ChunkUnloadEvent.class);
    public static final GameEventType<SignChangeEvent> SIGN_CHANGE_EVENT = register(SignChangeEvent.class);
    public static final GameEventType<SpongeAbsorbEvent> SPONGE_ABSORB_EVENT = register(SpongeAbsorbEvent.class);
    public static final GameEventType<StructureGrowEvent> STRUCTURE_GROW_EVENT = register(StructureGrowEvent.class);
    public static final GameEventType<WorldUnloadEvent> WORLD_UNLOAD_EVENT = register(WorldUnloadEvent.class);

    // Entity Events
    public static final GameEventType<EntityBlockFormEvent> ENTITY_BLOCK_FORM_EVENT = register(EntityBlockFormEvent.class);
    public static final GameEventType<EntityChangeBlockEvent> ENTITY_CHANGE_BLOCK_EVENT = register(EntityChangeBlockEvent.class);
    public static final GameEventType<EntityCollisionEvent> ENTITY_COLLISION_EVENT = register(EntityCollisionEvent.class);
    public static final GameEventType<EntityDamageEvent> ENTITY_DAMAGE_EVENT = register(EntityDamageEvent.class);
    public static final GameEventType<EntityDeathEvent> ENTITY_DEATH_EVENT = register(EntityDeathEvent.class);
    public static final GameEventType<EntityEnterPortalEvent> ENTITY_ENTER_PORTAL_EVENT = register(EntityEnterPortalEvent.class);
    public static final GameEventType<EntityExplodeEvent> ENTITY_EXPLODE_EVENT = register(EntityExplodeEvent.class);
    public static final GameEventType<EntityMoveEvent> ENTITY_MOVE_EVENT = register(EntityMoveEvent.class);
    public static final GameEventType<EntityPortalEvent> ENTITY_PORTAL_EVENT = register(EntityPortalEvent.class);
    public static final GameEventType<EntityRideEvent> ENTITY_RIDE_EVENT = register(EntityRideEvent.class);
    public static final GameEventType<EntitySpawnEvent> ENTITY_SPAWN_EVENT = register(EntitySpawnEvent.class);
    public static final GameEventType<EntityTargetEvent> ENTITY_TARGET_EVENT = register(EntityTargetEvent.class);
    public static final GameEventType<EntityTeleportEvent> ENTITY_TELEPORT_EVENT = register(EntityTeleportEvent.class);
    public static final GameEventType<HangingBreakEvent> HANGING_BREAK_EVENT = register(HangingBreakEvent.class);
    public static final GameEventType<HangingPlaceEvent> HANGING_PLACE_EVENT = register(HangingPlaceEvent.class);
    public static final GameEventType<ProjectileHitEvent> PROJECTILE_HIT_EVENT = register(ProjectileHitEvent.class);
    public static final GameEventType<ProjectileLaunchEvent> PROJECTILE_LAUNCH_EVENT = register(ProjectileLaunchEvent.class);
    public static final GameEventType<RaidTriggerEvent> RAID_TRIGGER_EVENT = register(RaidTriggerEvent.class);

    // Inventory Events
    public static final GameEventType<InventoryClickEvent> INVENTORY_CLICK_EVENT = register(InventoryClickEvent.class);
    public static final GameEventType<InventoryCloseEvent> INVENTORY_CLOSE_EVENT = register(InventoryCloseEvent.class);
    public static final GameEventType<InventoryOpenEvent> INVENTORY_OPEN_EVENT = register(InventoryOpenEvent.class);

    // Player Events
    public static final GameEventType<PlayerChangedWorldEvent> PLAYER_CHANGED_WORLD_EVENT = register(PlayerChangedWorldEvent.class);
    public static final GameEventType<PlayerChatEvent> PLAYER_CHAT_EVENT = register(PlayerChatEvent.class);
    public static final GameEventType<PlayerCommandEvent> PLAYER_COMMAND_EVENT = register(PlayerCommandEvent.class);
    public static final GameEventType<PlayerDropItemEvent> PLAYER_DROP_ITEM_EVENT = register(PlayerDropItemEvent.class);
    public static final GameEventType<PlayerEmptyBucketEvent> PLAYER_EMPTY_BUCKET_EVENT = register(PlayerEmptyBucketEvent.class);
    public static final GameEventType<PlayerFillBucketEvent> PLAYER_FILL_BUCKET_EVENT = register(PlayerFillBucketEvent.class);
    public static final GameEventType<PlayerGamemodeChangeEvent> PLAYER_GAMEMODE_CHANGE = register(PlayerGamemodeChangeEvent.class);
    public static final GameEventType<PlayerInteractEvent> PLAYER_INTERACT_EVENT = register(PlayerInteractEvent.class);
    public static final GameEventType<PlayerItemConsumeEvent> PLAYER_ITEM_CONSUME_EVENT = register(PlayerItemConsumeEvent.class);
    public static final GameEventType<PlayerJoinEvent> PLAYER_JOIN_EVENT = register(PlayerJoinEvent.class);
    public static final GameEventType<PlayerLeashEntityEvent> PLAYER_LEASH_ENTITY_EVENT = register(PlayerLeashEntityEvent.class);
    public static final GameEventType<PlayerLoginEvent> PLAYER_LOGIN_EVENT = register(PlayerLoginEvent.class);
    public static final GameEventType<PlayerPickupArrowEvent> PLAYER_PICKUP_ARROW_EVENT = register(PlayerPickupArrowEvent.class);
    public static final GameEventType<PlayerPickupItemEvent> PLAYER_PICKUP_ITEM_EVENT = register(PlayerPickupItemEvent.class);
    public static final GameEventType<PlayerQuitEvent> PLAYER_QUIT_EVENT = register(PlayerQuitEvent.class);
    public static final GameEventType<PlayerRespawnEvent> PLAYER_RESPAWN_EVENT = register(PlayerRespawnEvent.class);
    public static final GameEventType<PlayerShearEntityEvent> PLAYER_SHEAR_ENTITY_EVENT = register(PlayerShearEntityEvent.class);
    public static final GameEventType<PlayerUnleashEntityEvent> PLAYER_UNLEASH_ENTITY_EVENT = register(PlayerUnleashEntityEvent.class);

    private GameEventType() {
    }

    public GameEvent<Args> createEvent(Args args) {
        return new GameEvent<>(this, args);
    }

    private static <Args extends IEventArgs> GameEventType<Args> register(Class<Args> eventArgsType) {
        GameEventType<Args> eventType = new GameEventType<>();
        ALL_TYPES.add(eventType);
        return eventType;
    }

    public static Collection<GameEventType<?>> values() {
        return Collections.unmodifiableList(ALL_TYPES);
    }

}

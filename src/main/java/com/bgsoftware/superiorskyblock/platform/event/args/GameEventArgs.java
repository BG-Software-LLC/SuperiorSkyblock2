package com.bgsoftware.superiorskyblock.platform.event.args;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.core.PlayerHand;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;

public class GameEventArgs implements IEventArgs {

    private GameEventArgs() {

    }

    public static class AttemptEntitySpawnEvent extends GameEventArgs {

        public EntityType entityType;
        public Location spawnLocation;
        public CreatureSpawnEvent.SpawnReason spawnReason;

    }

    public static class BlockFromToEvent extends BlockEvent {

        public Block toBlock;

    }

    public static class BlockIgniteEvent extends BlockEvent {

        public org.bukkit.event.block.BlockIgniteEvent.IgniteCause igniteCause;

    }

    public static class PistonExtendEvent extends PistonEvent {

    }

    public static class PistonRetractEvent extends PistonEvent {

    }

    public static class BlockPlaceEvent extends BlockEvent {

        public Player player;
        public Block againstBlock;
        public BlockState replacedState;
        public PlayerHand usedHand;
        public ItemStack usedItem;

    }

    public static class ChunkLoadEvent extends ChunkEvent {

        public boolean isNewChunk;

    }

    public static class EntityChangeBlockEvent extends EntityEvent {

        public Block block;
        public Key newType;

    }

    public static class EntityDamageEvent extends EntityEvent {

        public org.bukkit.event.entity.EntityDamageEvent.DamageCause damageCause;
        @Nullable
        public Entity damager;

    }

    public static class EntityMoveEvent extends EntityEvent {

        public Location from;
        public Location to;

    }

    public static class EntityRideEvent extends EntityEvent {

        public Vehicle vehicle;

    }

    public static class HangingBreakEvent extends EntityEvent {

        public Entity remover;
        public org.bukkit.event.hanging.HangingBreakEvent.RemoveCause removeCause;

    }

    public static class HangingPlaceEvent extends EntitySpawnEvent {

        public Player player;

    }

    public static class EntityTargetEvent extends EntityEvent {

        public Entity target;

    }

    public static class InventoryClickEvent extends GameEventArgs {

        public org.bukkit.event.inventory.InventoryClickEvent bukkitEvent;

    }

    public static class PlayerChangedWorldEvent extends PlayerEvent {

        public World from;

    }

    public static class PlayerChatEvent extends PlayerEvent {

        public String message;
        @Nullable
        public String format;

    }

    public static class PlayerCommandEvent extends PlayerEvent {

        public String command;

    }

    public static class PlayerGamemodeChangeEvent extends PlayerEvent {

        public GameMode newGamemode;

    }

    public static class PlayerInteractEvent extends PlayerEvent {

        public Action action;
        @Nullable
        public PlayerHand usedHand;
        @Nullable
        public ItemStack usedItem;
        @Nullable
        public Block clickedBlock;
        @Nullable
        public Entity clickedEntity;

    }

    public static class PlayerRespawnEvent extends PlayerEvent {

        public org.bukkit.event.player.PlayerRespawnEvent bukkitEvent;

    }

    public static class SpongeAbsorbEvent extends BlockEvent {

        public List<BlockState> blocks;

    }

    public static class StructureGrowEvent extends WorldEvent {

        public Location location;
        public List<BlockState> blocks;

    }

    public static class PlayerJoinEvent extends PlayerEvent {

    }

    public static class PlayerQuitEvent extends PlayerEvent {

    }

    public static class PlayerEmptyBucketEvent extends PlayerBucketEvent {

    }

    public static class PlayerFillBucketEvent extends PlayerBucketEvent {

    }

    public static class BlockGrowEvent extends BlockTransformEvent {

    }

    public static class BlockFormEvent extends BlockTransformEvent {

    }

    public static class BlockSpreadEvent extends BlockTransformEvent {

        public Block source;

    }

    public static class BlockBreakEvent extends BlockEvent {

        public Player player;

    }

    public static class BlockDestroyEvent extends BlockEvent {

    }

    public static class EntityDeathEvent extends EntityEvent {

    }

    public static class LeavesDecayEvent extends BlockEvent {

    }

    public static class EntityExplodeEvent extends EntityEvent {

        public List<Block> blocks;
        public boolean isSoftExplosion;

    }

    public static class ProjectileHitEvent extends EntityEvent {

        @Nullable
        public Block hitBlock;
        @Nullable
        public Entity hitEntity;

    }

    public static class ProjectileLaunchEvent extends EntityEvent {

    }

    public static class ChunkUnloadEvent extends ChunkEvent {

    }

    public static class WorldUnloadEvent extends WorldEvent {

    }

    public static class EntitySpawnEvent extends EntityEvent {

        public CreatureSpawnEvent.SpawnReason spawnReason;

    }

    public static class BlockBurnEvent extends BlockEvent {

    }

    public static class EntityTeleportEvent extends EntityMoveEvent {

        public PlayerTeleportEvent.TeleportCause cause;

    }

    public static class BlockRedstoneEvent extends BlockEvent {

    }

    public static class InventoryCloseEvent extends GameEventArgs {

        public org.bukkit.event.inventory.InventoryCloseEvent bukkitEvent;

    }

    public static class InventoryOpenEvent extends GameEventArgs {

        public org.bukkit.event.inventory.InventoryOpenEvent bukkitEvent;

    }

    public static class PlayerLoginEvent extends PlayerEvent {

    }

    public static class EntityPortalEvent extends EntityTeleportEvent {

    }

    public static class EntityEnterPortalEvent extends EntityEvent {

        public Location portalLocation;

    }

    public static class EntityBlockFormEvent extends BlockFormEvent {

        public Entity entity;

    }

    public static class BlockDispenseEvent extends BlockEvent {

        public ItemStack dispensedItem;
        public Vector velocity;

    }

    public static class BlockFadeEvent extends BlockEvent {

        public BlockState newState;

    }


    public static class PlayerItemConsumeEvent extends PlayerEvent {

        public ItemStack consumedItem;

    }


    public static class PlayerShearEntityEvent extends PlayerEvent {

        public Entity entity;

    }


    public static class PlayerLeashEntityEvent extends PlayerEvent {

        public Entity entity;

    }


    public static class PlayerUnleashEntityEvent extends PlayerEvent {

        public Entity entity;

    }


    public static class EntityCollisionEvent extends EntityEvent {

        public Entity target;

    }


    public static class PlayerDropItemEvent extends PlayerEvent {

        public Item droppedItem;

    }


    public static class PlayerPickupItemEvent extends PlayerEvent {

        public Item pickedUpItem;

    }

    public static class PlayerPickupArrowEvent extends PlayerPickupItemEvent {

    }

    public static class RaidTriggerEvent extends WorldEvent {

        public Player player;
        public Location raidLocation;

    }

    public static class SignChangeEvent extends BlockEvent {

        public Player player;
        public String[] lines;

    }

    public static class BlockPhysicsEvent extends BlockEvent {

    }

    private static class BlockEvent extends GameEventArgs {

        public Block block;

    }

    private static class PistonEvent extends BlockEvent {

        public List<Block> blocks;
        public BlockFace direction;

    }

    private static class ChunkEvent extends GameEventArgs {

        public Chunk chunk;

    }

    private static class EntityEvent extends GameEventArgs {

        public Entity entity;

    }

    private static class PlayerEvent extends GameEventArgs {

        public Player player;

    }

    private static class WorldEvent extends GameEventArgs {

        public World world;

    }

    private static class BlockTransformEvent extends BlockEvent {

        public BlockState newState;

    }

    private static class PlayerBucketEvent extends PlayerEvent {

        public Material bucket;
        public Block clickedBlock;

    }

}

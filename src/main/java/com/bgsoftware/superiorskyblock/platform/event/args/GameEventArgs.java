package com.bgsoftware.superiorskyblock.platform.event.args;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.LazyNullableReference;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.PlayerHand;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.menu.dialog.DialogWrapper;
import com.bgsoftware.superiorskyblock.world.WorldBlocks;
import org.bukkit.Chunk;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Objects;

public class GameEventArgs implements IEventArgs {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private GameEventArgs() {

    }

    public static class BlockFromToEvent extends BlockEvent {

        public final LazyNullableReference<Island, Boolean> toIslandResolver = new LazyNullableReference<Island, Boolean>() {
            @Override
            protected Island create(Boolean ignoreWorld) {
                return WorldBlocks.findIslandWithHint(toBlock, islandResolver.get(ignoreWorld));
            }
        };
        public Block toBlock;

        @Override
        public void reuseLocationCaches(IEventArgs previous) {
            super.reuseLocationCaches(previous);
            BlockFromToEvent other = (BlockFromToEvent) previous;
            if (Objects.equals(other.toBlock, this.toBlock))
                this.toIslandResolver.copyComputedFrom(other.toIslandResolver);
        }

    }

    public static class BlockIgniteEvent extends BlockEvent {

        public org.bukkit.event.block.BlockIgniteEvent.IgniteCause igniteCause;

    }

    public static class PistonExtendEvent extends PistonEvent {

    }

    public static class PistonRetractEvent extends PistonEvent {

    }

    public static class BlockPlaceEvent extends BlockEvent {

        public final LazyNullableReference<SuperiorPlayer, Void> superiorPlayerResolver = new LazyNullableReference<SuperiorPlayer, Void>() {
            @Override
            protected SuperiorPlayer create(Void unused) {
                return plugin.getPlayers().getSuperiorPlayer(player);
            }
        };
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

        public final LazyNullableReference<Island, Boolean> blockIslandResolver = new LazyNullableReference<Island, Boolean>() {
            @Override
            protected Island create(Boolean ignoreWorld) {
                try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                    return plugin.getGrid().getIslandAt(block.getLocation(wrapper.getHandle()), ignoreWorld != null && ignoreWorld);
                }
            }
        };
        public Block block;
        public Key newType;

        @Override
        public void reuseLocationCaches(IEventArgs previous) {
            // Only the block's island: the entity can be moved between priorities.
            EntityChangeBlockEvent other = (EntityChangeBlockEvent) previous;
            if (Objects.equals(other.block, this.block))
                this.blockIslandResolver.copyComputedFrom(other.blockIslandResolver);
        }

    }

    public static class EntityDamageEvent extends EntityEvent {

        public org.bukkit.event.entity.EntityDamageEvent.DamageCause damageCause;
        @Nullable
        public Entity damager;

    }

    public static class EntityMoveEvent extends EntityEvent {

        public final LazyNullableReference<Island, Boolean> fromIslandResolver = new LazyNullableReference<Island, Boolean>() {
            @Override
            protected Island create(Boolean ignoreWorld) {
                return plugin.getGrid().getIslandAt(from, ignoreWorld != null && ignoreWorld);
            }
        };
        public final LazyNullableReference<Island, Boolean> toIslandResolver = new LazyNullableReference<Island, Boolean>() {
            @Override
            protected Island create(Boolean ignoreWorld) {
                return plugin.getGrid().getIslandAt(to, ignoreWorld != null && ignoreWorld);
            }
        };
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

        public final LazyNullableReference<Island, Boolean> blockIslandResolver = new LazyNullableReference<Island, Boolean>() {
            @Override
            protected Island create(Boolean ignoreWorld) {
                if (clickedBlock == null)
                    return null;

                try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                    return plugin.getGrid().getIslandAt(clickedBlock.getLocation(wrapper.getHandle()), ignoreWorld != null && ignoreWorld);
                }
            }
        };
        public final LazyNullableReference<Key, Void> blockKeyResolver = new LazyNullableReference<Key, Void>() {
            @Override
            protected Key create(Void unused) {
                return clickedBlock == null ? null : Keys.of(clickedBlock);
            }
        };
        public final LazyNullableReference<Island, Boolean> entityIslandResolver = new LazyNullableReference<Island, Boolean>() {
            @Override
            protected Island create(Boolean ignoreWorld) {
                if (clickedEntity == null)
                    return null;

                try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                    return plugin.getGrid().getIslandAt(clickedEntity.getLocation(wrapper.getHandle()), ignoreWorld != null && ignoreWorld);
                }
            }
        };

        public Action action;
        @Nullable
        public PlayerHand usedHand;
        @Nullable
        public ItemStack usedItem;
        @Nullable
        public Block clickedBlock;
        @Nullable
        public Entity clickedEntity;

        @Override
        public void reuseLocationCaches(IEventArgs previous) {
            // Only the clicked block's island: the clicked entity can be moved between priorities.
            PlayerInteractEvent other = (PlayerInteractEvent) previous;
            if (this.clickedBlock != null && this.clickedBlock.equals(other.clickedBlock))
                this.blockIslandResolver.copyComputedFrom(other.blockIslandResolver);
        }

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

        public final LazyNullableReference<SuperiorPlayer, Void> superiorPlayerResolver = new LazyNullableReference<SuperiorPlayer, Void>() {
            @Override
            protected SuperiorPlayer create(Void unused) {
                return plugin.getPlayers().getSuperiorPlayer(player);
            }
        };
        public Player player;

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

    public static class InventoryClickEvent extends GameEventArgs {

        public org.bukkit.event.inventory.InventoryClickEvent bukkitEvent;

    }

    public static class DialogCloseEvent extends DialogEvent {

    }

    public static class DialogOpenEvent extends DialogEvent {

    }

    public static class DialogClickEvent extends DialogEvent {

        public int clickedSlot;

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


    public static class PlayerAttackEntityEvent extends PlayerEvent {

        public final LazyNullableReference<Island, Boolean> islandResolver = new LazyNullableReference<Island, Boolean>() {
            @Override
            protected Island create(Boolean ignoreWorld) {
                try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                    return plugin.getGrid().getIslandAt(entity.getLocation(wrapper.getHandle()), ignoreWorld != null && ignoreWorld);
                }
            }
        };
        public Entity entity;

    }


    public static class PlayerShearEntityEvent extends PlayerEvent {

        public final LazyNullableReference<Island, Boolean> islandResolver = new LazyNullableReference<Island, Boolean>() {
            @Override
            protected Island create(Boolean ignoreWorld) {
                try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                    return plugin.getGrid().getIslandAt(entity.getLocation(wrapper.getHandle()), ignoreWorld != null && ignoreWorld);
                }
            }
        };
        public Entity entity;

    }


    public static class PlayerLeashEntityEvent extends PlayerEvent {

        public final LazyNullableReference<Island, Boolean> islandResolver = new LazyNullableReference<Island, Boolean>() {
            @Override
            protected Island create(Boolean ignoreWorld) {
                try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                    return plugin.getGrid().getIslandAt(entity.getLocation(wrapper.getHandle()), ignoreWorld != null && ignoreWorld);
                }
            }
        };
        public Entity entity;

    }


    public static class PlayerUnleashEntityEvent extends PlayerEvent {

        public final LazyNullableReference<Island, Boolean> islandResolver = new LazyNullableReference<Island, Boolean>() {
            @Override
            protected Island create(Boolean ignoreWorld) {
                try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                    return plugin.getGrid().getIslandAt(entity.getLocation(wrapper.getHandle()), ignoreWorld != null && ignoreWorld);
                }
            }
        };
        public Entity entity;

    }


    public static class EntityCollisionEvent extends EntityEvent {

        public Entity target;

    }


    public static class PlayerDropItemEvent extends PlayerEvent {

        public final LazyNullableReference<Island, Boolean> islandResolver = new LazyNullableReference<Island, Boolean>() {
            @Override
            protected Island create(Boolean ignoreWorld) {
                try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                    return plugin.getGrid().getIslandAt(droppedItem.getLocation(wrapper.getHandle()), ignoreWorld != null && ignoreWorld);
                }
            }
        };
        public Item droppedItem;

    }


    public static class PlayerPickupItemEvent extends PlayerEvent {

        public final LazyNullableReference<Island, Boolean> islandResolver = new LazyNullableReference<Island, Boolean>() {
            @Override
            protected Island create(Boolean ignoreWorld) {
                try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                    return plugin.getGrid().getIslandAt(pickedUpItem.getLocation(wrapper.getHandle()), ignoreWorld != null && ignoreWorld);
                }
            }
        };
        public Item pickedUpItem;

    }

    public static class PlayerPickupArrowEvent extends PlayerPickupItemEvent {

    }

    public static class PlayerTakeLecternBookEvent extends PlayerEvent {

        public Block clickedBlock;

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

    public static class BlockUpdateShapeEvent extends BlockEvent {

        public BlockState oldState;

    }

    public static class GenericGameEvent extends WorldEvent {

        public String gameEvent;
        public Location location;

    }

    public static class EntityInteractEvent extends EntityEvent {

        @Nullable
        public Block block;

    }

    private static class BlockEvent extends GameEventArgs {

        public final LazyNullableReference<Island, Boolean> islandResolver = new LazyNullableReference<Island, Boolean>() {
            @Override
            protected Island create(Boolean ignoreWorld) {
                try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                    return plugin.getGrid().getIslandAt(block.getLocation(wrapper.getHandle()), ignoreWorld != null && ignoreWorld);
                }
            }
        };
        public final LazyNullableReference<Key, Void> keyResolver = new LazyNullableReference<Key, Void>() {
            @Override
            protected Key create(Void unused) {
                return Keys.of(block);
            }
        };
        public Block block;

        @Override
        public void reuseLocationCaches(IEventArgs previous) {
            // Only the island: the block itself can be changed between priorities, so its key is not reused.
            BlockEvent other = (BlockEvent) previous;
            if (Objects.equals(other.block, this.block))
                this.islandResolver.copyComputedFrom(other.islandResolver);
        }

    }

    private static class PistonEvent extends BlockEvent {

        public List<Block> blocks;
        public BlockFace direction;

    }

    private static class ChunkEvent extends GameEventArgs {

        public Chunk chunk;

    }

    private static class EntityEvent extends GameEventArgs {

        public final LazyNullableReference<Island, Boolean> islandResolver = new LazyNullableReference<Island, Boolean>() {
            @Override
            protected Island create(Boolean ignoreWorld) {
                try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                    return plugin.getGrid().getIslandAt(entity.getLocation(wrapper.getHandle()), ignoreWorld != null && ignoreWorld);
                }
            }
        };
        public Entity entity;

    }

    private static class DialogEvent extends GameEventArgs {

        public SuperiorPlayer superiorPlayer;
        public DialogWrapper<?> dialog;

    }

    private static class PlayerEvent extends GameEventArgs {

        public final LazyNullableReference<SuperiorPlayer, Void> superiorPlayerResolver = new LazyNullableReference<SuperiorPlayer, Void>() {
            @Override
            protected SuperiorPlayer create(Void unused) {
                return plugin.getPlayers().getSuperiorPlayer(player);
            }
        };
        public Player player;

    }

    private static class WorldEvent extends GameEventArgs {

        public World world;

    }

    private static class BlockTransformEvent extends BlockEvent {

        public BlockState newState;

    }

    private static class PlayerBucketEvent extends PlayerEvent {

        public final LazyNullableReference<Island, Boolean> islandResolver = new LazyNullableReference<Island, Boolean>() {
            @Override
            protected Island create(Boolean ignoreWorld) {
                try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                    return plugin.getGrid().getIslandAt(clickedBlock.getLocation(wrapper.getHandle()), ignoreWorld != null && ignoreWorld);
                }
            }
        };
        public Material bucket;
        public Block clickedBlock;

        @Override
        public void reuseLocationCaches(IEventArgs previous) {
            PlayerBucketEvent other = (PlayerBucketEvent) previous;
            if (Objects.equals(other.clickedBlock, this.clickedBlock))
                this.islandResolver.copyComputedFrom(other.islandResolver);
        }

    }

}

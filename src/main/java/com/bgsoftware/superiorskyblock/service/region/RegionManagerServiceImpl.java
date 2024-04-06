package com.bgsoftware.superiorskyblock.service.region;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandEnterEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandLeaveEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandRestrictMoveEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPreview;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.player.PlayerStatus;
import com.bgsoftware.superiorskyblock.api.service.region.InteractionResult;
import com.bgsoftware.superiorskyblock.api.service.region.MoveResult;
import com.bgsoftware.superiorskyblock.api.service.region.RegionManagerService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.flag.IslandFlags;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.service.IService;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import com.bgsoftware.superiorskyblock.world.BukkitItems;
import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Mule;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.Optional;

public class RegionManagerServiceImpl implements RegionManagerService, IService {

    private static final Material FARMLAND = EnumHelper.getEnum(Material.class, "FARMLAND", "SOIL");
    @Nullable
    private static final Material TURTLE_EGG = EnumHelper.getEnum(Material.class, "TURTLE_EGG");
    @Nullable
    private static final Material SWEET_BERRY_BUSH = EnumHelper.getEnum(Material.class, "SWEET_BERRY_BUSH");
    @Nullable
    private static final Material LECTERN = EnumHelper.getEnum(Material.class, "LECTERN");
    @Nullable
    private static final EntityType AXOLOTL_TYPE = getSafeEntityType("AXOLOTL");
    private static final int MAX_PICKUP_DISTANCE = 1;

    private final SuperiorSkyblockPlugin plugin;

    public RegionManagerServiceImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Class<?> getAPIClass() {
        return RegionManagerService.class;
    }

    @Override
    public InteractionResult handleBlockPlace(SuperiorPlayer superiorPlayer, Block block) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(block, "block cannot be null");

        Location blockLocation = block.getLocation();
        Island island = plugin.getGrid().getIslandAt(blockLocation);

        return handleInteractionInternal(superiorPlayer, blockLocation, island, IslandPrivileges.BUILD, 0, true, true);
    }

    @Override
    public InteractionResult handleBlockBreak(SuperiorPlayer superiorPlayer, Block block) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(block, "block cannot be null");

        Location blockLocation = block.getLocation();
        Island island = plugin.getGrid().getIslandAt(blockLocation);

        Material blockType = block.getType();
        IslandPrivilege islandPrivilege = blockType == Materials.SPAWNER.toBukkitType() ? IslandPrivileges.SPAWNER_BREAK : IslandPrivileges.BREAK;

        InteractionResult interactionResult = handleInteractionInternal(superiorPlayer, blockLocation, island, islandPrivilege, 0, true, true);

        if (interactionResult != InteractionResult.SUCCESS) return interactionResult;

        if (island == null) return InteractionResult.SUCCESS;

        if (plugin.getSettings().getValuableBlocks().contains(Keys.of(block)))
            return handleInteractionInternal(superiorPlayer, blockLocation, island, IslandPrivileges.VALUABLE_BREAK, 0, false, false);

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult handleBlockInteract(SuperiorPlayer superiorPlayer, Block block, Action action, @Nullable ItemStack usedItem) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(block, "block cannot be null");

        Location blockLocation = block.getLocation();
        Material blockType = block.getType();

        int stackedBlockAmount = plugin.getStackedBlocks().getStackedBlockAmount(blockLocation);
        if (stackedBlockAmount <= 1 && !plugin.getSettings().getInteractables().contains(blockType.name()))
            return InteractionResult.SUCCESS;

        Island island = plugin.getGrid().getIslandAt(blockLocation);

        InteractionResult interactionResult = handleInteractionInternal(superiorPlayer, blockLocation, island,
                null, 0, true, false);

        if (interactionResult != InteractionResult.SUCCESS)
            return interactionResult;

        if (island == null)
            return InteractionResult.SUCCESS;

        BlockState blockState = block.getState();
        EntityType spawnType = usedItem == null ? EntityType.UNKNOWN : BukkitItems.getEntityType(usedItem);

        IslandPrivilege islandPrivilege;

        if (spawnType != EntityType.UNKNOWN) {
            islandPrivilege = BukkitEntities.getCategory(spawnType).getSpawnPrivilege();
        } else if (usedItem != null && Materials.isMinecart(usedItem.getType()) ? Materials.isRail(blockType) : Materials.isBoat(blockType)) {
            islandPrivilege = IslandPrivileges.MINECART_PLACE;
        } else if (Materials.isChest(blockType)) {
            islandPrivilege = IslandPrivileges.CHEST_ACCESS;
        } else if (blockState instanceof InventoryHolder) {
            islandPrivilege = IslandPrivileges.USE;
        } else if (blockState instanceof Sign) {
            islandPrivilege = IslandPrivileges.SIGN_INTERACT;
        } else if (blockType == Materials.SPAWNER.toBukkitType()) {
            islandPrivilege = IslandPrivileges.SPAWNER_BREAK;
        } else if (blockType == FARMLAND) {
            islandPrivilege = action == Action.PHYSICAL ? IslandPrivileges.FARM_TRAMPING : IslandPrivileges.BUILD;
        } else if (blockType == TURTLE_EGG) {
            islandPrivilege = action == Action.PHYSICAL ? IslandPrivileges.TURTLE_EGG_TRAMPING : IslandPrivileges.BUILD;
        } else if (blockType == SWEET_BERRY_BUSH && action == Action.RIGHT_CLICK_BLOCK) {
            islandPrivilege = Materials.BONE_MEAL.toBukkitItem().isSimilar(usedItem) ? IslandPrivileges.FERTILIZE : IslandPrivileges.FARM_TRAMPING;
        } else if (stackedBlockAmount > 1) {
            islandPrivilege = IslandPrivileges.BREAK;
        } else if (blockType == Material.PUMPKIN) {
            islandPrivilege = IslandPrivileges.BREAK;
        } else if (blockType == LECTERN) {
            islandPrivilege = IslandPrivileges.PICKUP_LECTERN_BOOK;
        } else {
            islandPrivilege = IslandPrivileges.INTERACT;
        }

        return handleInteractionInternal(superiorPlayer, blockLocation, island, islandPrivilege, 0, false, false);
    }

    @Override
    public InteractionResult handleBlockFertilize(SuperiorPlayer superiorPlayer, Block block) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(block, "block cannot be null");

        Location blockLocation = block.getLocation();
        Island island = plugin.getGrid().getIslandAt(blockLocation);

        return handleInteractionInternal(superiorPlayer, blockLocation, island, IslandPrivileges.FERTILIZE, 0, true, true);
    }

    @Override
    public InteractionResult handleEntityInteract(SuperiorPlayer superiorPlayer, Entity entity, @Nullable ItemStack usedItem) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(entity, "entity cannot be null");

        Location entityLocation = entity.getLocation();
        Island island = plugin.getGrid().getIslandAt(entityLocation);

        InteractionResult interactionResult = handleInteractionInternal(superiorPlayer, entityLocation, island, null, 0, true, false);

        if (interactionResult != InteractionResult.SUCCESS) return interactionResult;

        if (island == null) return InteractionResult.SUCCESS;

        boolean closeInventory = false;

        IslandPrivilege islandPrivilege;

        if (entity instanceof ArmorStand) {
            islandPrivilege = IslandPrivileges.INTERACT;
        } else if (usedItem != null && entity instanceof Animals && plugin.getNMSEntities().isAnimalFood(usedItem, (Animals) entity)) {
            islandPrivilege = IslandPrivileges.ANIMAL_BREED;
        } else if (usedItem != null && usedItem.getType() == Material.NAME_TAG) {
            islandPrivilege = IslandPrivileges.NAME_ENTITY;
        } else if (entity instanceof Villager) {
            islandPrivilege = IslandPrivileges.VILLAGER_TRADING;
            closeInventory = true;
        } else if (entity instanceof Horse || (ServerVersion.isAtLeast(ServerVersion.v1_11) && (entity instanceof Mule || entity instanceof Donkey))) {
            islandPrivilege = IslandPrivileges.HORSE_INTERACT;
            closeInventory = true;
        } else if (usedItem != null && entity instanceof Creeper && usedItem.getType() == Material.FLINT_AND_STEEL) {
            islandPrivilege = IslandPrivileges.IGNITE_CREEPER;
        } else if (usedItem != null && ServerVersion.isAtLeast(ServerVersion.v1_17) && usedItem.getType() == Material.WATER_BUCKET && entity.getType() == AXOLOTL_TYPE) {
            islandPrivilege = IslandPrivileges.PICKUP_AXOLOTL;
        } else if (entity instanceof ItemFrame) {
            islandPrivilege = IslandPrivileges.ITEM_FRAME;
        } else if (entity instanceof Painting) {
            islandPrivilege = IslandPrivileges.PAINTING;
        } else if (entity instanceof Fish && !ServerVersion.isLegacy()) {
            islandPrivilege = IslandPrivileges.PICKUP_FISH;
        } else {
            return InteractionResult.SUCCESS;
        }

        interactionResult = handleInteractionInternal(superiorPlayer, entityLocation, island, islandPrivilege, 0, false, false);

        if (closeInventory && interactionResult != InteractionResult.SUCCESS) {
            BukkitExecutor.sync(() -> {
                Player player = superiorPlayer.asPlayer();
                if (player != null && player.isOnline()) {
                    Inventory openInventory = player.getOpenInventory().getTopInventory();
                    if (openInventory != null && (openInventory.getType() == InventoryType.MERCHANT || openInventory.getType() == InventoryType.CHEST))
                        player.closeInventory();
                }
            }, 1L);
        }

        return interactionResult;
    }

    @Override
    public InteractionResult handleEntityDamage(Entity damager, Entity entity) {
        Preconditions.checkNotNull(damager, "damager cannot be null");
        Preconditions.checkNotNull(entity, "entity cannot be null");

        Optional<SuperiorPlayer> damagerSource = BukkitEntities.getPlayerSource(damager).map(plugin.getPlayers()::getSuperiorPlayer);

        if (!damagerSource.isPresent()) return InteractionResult.SUCCESS;

        Location entityLocation = entity.getLocation();
        Island island = plugin.getGrid().getIslandAt(entityLocation);
        IslandPrivilege islandPrivilege = BukkitEntities.getCategory(entity.getType()).getDamagePrivilege();

        InteractionResult interactionResult = handleInteractionInternal(damagerSource.get(), entityLocation, island, islandPrivilege, 0, true, false);

        if (interactionResult != InteractionResult.SUCCESS && damager instanceof Arrow && entity.getFireTicks() > 0)
            entity.setFireTicks(0);

        return interactionResult;
    }

    @Override
    public InteractionResult handleEntityShear(SuperiorPlayer superiorPlayer, Entity entity) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(entity, "entity cannot be null");

        Location entityLocation = entity.getLocation();
        Island island = plugin.getGrid().getIslandAt(entityLocation);

        return handleInteractionInternal(superiorPlayer, entityLocation, island, IslandPrivileges.ANIMAL_SHEAR, 0, true, false);
    }

    @Override
    public InteractionResult handleEntityLeash(SuperiorPlayer superiorPlayer, Entity entity) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(entity, "entity cannot be null");

        Location entityLocation = entity.getLocation();
        Island island = plugin.getGrid().getIslandAt(entityLocation);

        return handleInteractionInternal(superiorPlayer, entityLocation, island, IslandPrivileges.LEASH, 0, true, false);
    }

    @Override
    public InteractionResult handlePlayerPickupItem(SuperiorPlayer superiorPlayer, Item item) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(item, "item cannot be null");

        if (plugin.getNMSPlayers().wasThrownByPlayer(item, superiorPlayer)) return InteractionResult.SUCCESS;

        Location itemLocation = item.getLocation();
        Island island = plugin.getGrid().getIslandAt(itemLocation);

        return handleInteractionInternal(superiorPlayer, itemLocation, island, IslandPrivileges.PICKUP_DROPS, MAX_PICKUP_DISTANCE, true, false);
    }

    @Override
    public InteractionResult handlePlayerDropItem(SuperiorPlayer superiorPlayer, Item item) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(item, "item cannot be null");

        Location itemLocation = item.getLocation();
        Island island = plugin.getGrid().getIslandAt(itemLocation);

        return handleInteractionInternal(superiorPlayer, itemLocation, island, IslandPrivileges.DROP_ITEMS, 0, true, false);
    }

    @Override
    public InteractionResult handlePlayerEnderPearl(SuperiorPlayer superiorPlayer, Location destination) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(destination, "destination cannot be null");
        Preconditions.checkArgument(destination.getWorld() != null, "destination's world cannot be null");

        Island island = plugin.getGrid().getIslandAt(destination);

        return handleInteractionInternal(superiorPlayer, destination, island, IslandPrivileges.ENDER_PEARL, 0, true, false);
    }

    @Override
    public InteractionResult handlePlayerConsumeChorusFruit(SuperiorPlayer superiorPlayer, Location location) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(location, "location cannot be null");
        Preconditions.checkArgument(location.getWorld() != null, "destination's world cannot be null");

        if (IslandPrivileges.CHORUS_FRUIT == null) {
            // Chorus Fruit privilege does not exist, we will just return SUCCESS in this case.
            return InteractionResult.SUCCESS;
        }

        Island island = plugin.getGrid().getIslandAt(location);
        return handleInteractionInternal(superiorPlayer, location, island, IslandPrivileges.CHORUS_FRUIT, 0, true, true);
    }

    @Override
    public InteractionResult handleCustomInteraction(SuperiorPlayer superiorPlayer, Location location, IslandPrivilege islandPrivilege) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(location, "location cannot be null");
        Preconditions.checkNotNull(islandPrivilege, "islandPrivilege cannot be null");

        Island island = plugin.getGrid().getIslandAt(location);
        return handleInteractionInternal(superiorPlayer, location, island, islandPrivilege, 0, true, false);
    }

    private InteractionResult handleInteractionInternal(SuperiorPlayer superiorPlayer, Location location, @Nullable Island island, @Nullable IslandPrivilege islandPrivilege, int extraRadius, boolean checkIslandBoundaries, boolean checkRecalculation) {
        if (superiorPlayer.hasBypassModeEnabled()) return InteractionResult.SUCCESS;

        if (checkIslandBoundaries) {
            if (island == null && plugin.getGrid().isIslandsWorld(superiorPlayer.getWorld()))
                return InteractionResult.OUTSIDE_ISLAND;

            if (island != null && !island.isInsideRange(location, extraRadius)) return InteractionResult.OUTSIDE_ISLAND;
        }

        if (island != null) {
            if (islandPrivilege != null && !island.hasPermission(superiorPlayer, islandPrivilege))
                return InteractionResult.MISSING_PRIVILEGE;

            if (checkRecalculation && island.isBeingRecalculated()) return InteractionResult.ISLAND_RECALCULATE;
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public MoveResult handlePlayerMove(SuperiorPlayer superiorPlayer, Location from, Location to) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(from, "from cannot be null");
        Preconditions.checkNotNull(to, "to cannot be null");

        if (from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ()) {
            // Handle moving while in teleport warmup.
            BukkitTask teleportTask = superiorPlayer.getTeleportTask();
            if (teleportTask != null) {
                teleportTask.cancel();
                superiorPlayer.setTeleportTask(null);
                Message.TELEPORT_WARMUP_CANCEL.send(superiorPlayer);
            }

            //Checking for out of distance from preview location.
            IslandPreview islandPreview = plugin.getGrid().getIslandPreview(superiorPlayer);
            if (islandPreview != null && (!islandPreview.getLocation().getWorld().equals(to.getWorld()) || islandPreview.getLocation().distanceSquared(to) > 10000)) {
                islandPreview.handleEscape();
                return MoveResult.ISLAND_PREVIEW_MOVED_TOO_FAR;
            }

            MoveResult moveResult;

            Island toIsland = plugin.getGrid().getIslandAt(to);
            if (toIsland != null) {
                moveResult = handlePlayerEnterIslandInternal(superiorPlayer, toIsland, from, to, IslandEnterEvent.EnterCause.PLAYER_MOVE);
                if (moveResult != MoveResult.SUCCESS) return moveResult;
            }

            Island fromIsland = plugin.getGrid().getIslandAt(from);
            if (fromIsland != null) {
                moveResult = handlePlayerLeaveIslandInternal(superiorPlayer, fromIsland, from, to, IslandLeaveEvent.LeaveCause.PLAYER_MOVE);
                if (moveResult != MoveResult.SUCCESS) return moveResult;
            }
        }

        if (from.getBlockY() != to.getBlockY() && to.getBlockY() <= plugin.getNMSWorld().getMinHeight(to.getWorld()) - 5) {
            Island island = plugin.getGrid().getIslandAt(from);

            if (island == null || (island.isVisitor(superiorPlayer, false) ? !plugin.getSettings().getVoidTeleport().isVisitors() : !plugin.getSettings().getVoidTeleport().isMembers()))
                return MoveResult.SUCCESS;

            Log.debug(Debug.VOID_TELEPORT, superiorPlayer.getName());

            superiorPlayer.setPlayerStatus(PlayerStatus.VOID_TELEPORT);

            superiorPlayer.teleport(island, result -> {
                if (!result) {
                    Message.TELEPORTED_FAILED.send(superiorPlayer);
                    superiorPlayer.teleport(plugin.getGrid().getSpawnIsland(), result2 -> {
                        forgetVoidTeleportPlayerStatus(superiorPlayer);
                    });
                } else {
                    forgetVoidTeleportPlayerStatus(superiorPlayer);
                }
            });

            return MoveResult.VOID_TELEPORT;
        }

        return MoveResult.SUCCESS;
    }

    private void forgetVoidTeleportPlayerStatus(SuperiorPlayer superiorPlayer) {
        BukkitExecutor.sync(() -> {
            superiorPlayer.removePlayerStatus(PlayerStatus.VOID_TELEPORT);
        }, 40L);
    }

    @Override
    public MoveResult handlePlayerTeleport(SuperiorPlayer superiorPlayer, Location from, Location to) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(from, "from cannot be null");
        Preconditions.checkArgument(from.getWorld() != null, "from world cannot be null");
        Preconditions.checkNotNull(to, "to cannot be null");
        Preconditions.checkArgument(to.getWorld() != null, "from world cannot be null");

        Island toIsland = plugin.getGrid().getIslandAt(to);
        if (toIsland != null) {
            MoveResult enterMove = handlePlayerEnterIslandInternal(superiorPlayer, toIsland, from, to, IslandEnterEvent.EnterCause.PLAYER_TELEPORT);
            if (enterMove != MoveResult.SUCCESS)
                return enterMove;
        }

        Island fromIsland = plugin.getGrid().getIslandAt(from);
        if (fromIsland != null) {
            return handlePlayerLeaveIslandInternal(superiorPlayer, fromIsland, from, to, IslandLeaveEvent.LeaveCause.PLAYER_TELEPORT);
        }

        return MoveResult.SUCCESS;
    }

    @Override
    public MoveResult handlePlayerTeleportByPortal(SuperiorPlayer superiorPlayer, Location portalLocation, Location teleportLocation) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(portalLocation, "portalLocation cannot be null");
        Preconditions.checkNotNull(portalLocation.getWorld(), "portalLocation world cannot be null");
        Preconditions.checkNotNull(teleportLocation, "teleportLocation cannot be null");
        Preconditions.checkNotNull(teleportLocation.getWorld(), "teleportLocation world cannot be null");

        Island island = plugin.getGrid().getIslandAt(teleportLocation);
        if (island != null) {
            return handlePlayerEnterIslandInternal(superiorPlayer, island, portalLocation, teleportLocation, IslandEnterEvent.EnterCause.PORTAL);
        }

        return MoveResult.SUCCESS;
    }

    @Override
    public MoveResult handlePlayerJoin(SuperiorPlayer superiorPlayer, Location location) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(location, "location cannot be null");
        Preconditions.checkArgument(location.getWorld() != null, "location's world cannot be null");

        Island island = plugin.getGrid().getIslandAt(location);

        return island == null ? MoveResult.SUCCESS : handlePlayerEnterIslandInternal(superiorPlayer, island, null, location, IslandEnterEvent.EnterCause.PLAYER_JOIN);
    }

    @Override
    public MoveResult handlePlayerQuit(SuperiorPlayer superiorPlayer, Location location) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");

        Island island = plugin.getGrid().getIslandAt(location);
        if (island == null) return MoveResult.SUCCESS;

        island.setPlayerInside(superiorPlayer, false);
        return handlePlayerLeaveIslandInternal(superiorPlayer, island, location, null, IslandLeaveEvent.LeaveCause.PLAYER_QUIT);
    }

    private MoveResult handlePlayerEnterIslandInternal(SuperiorPlayer superiorPlayer, Island toIsland, @Nullable Location from, Location to, IslandEnterEvent.EnterCause enterCause) {
        // This can happen after the leave event is cancelled.
        if (superiorPlayer.hasPlayerStatus(PlayerStatus.LEAVING_ISLAND)) {
            superiorPlayer.removePlayerStatus(PlayerStatus.LEAVING_ISLAND);
            return MoveResult.SUCCESS;
        }

        // Checking if the player is banned from the island.
        if (toIsland.isBanned(superiorPlayer) && !superiorPlayer.hasBypassModeEnabled() && !superiorPlayer.hasPermissionWithoutOP("superior.admin.ban.bypass")) {
            plugin.getEventsBus().callIslandRestrictMoveEvent(superiorPlayer, IslandRestrictMoveEvent.RestrictReason.BANNED_FROM_ISLAND);
            Message.BANNED_FROM_ISLAND.send(superiorPlayer);
            return MoveResult.BANNED_FROM_ISLAND;
        }

        // Checking if the player is locked to visitors.
        if (toIsland.isLocked() && !toIsland.hasPermission(superiorPlayer, IslandPrivileges.CLOSE_BYPASS)) {
            plugin.getEventsBus().callIslandRestrictMoveEvent(superiorPlayer, IslandRestrictMoveEvent.RestrictReason.LOCKED_ISLAND);
            Message.NO_CLOSE_BYPASS.send(superiorPlayer);
            return MoveResult.ISLAND_LOCKED;
        }

        Island fromIsland = from == null ? null : plugin.getGrid().getIslandAt(from);

        boolean equalIslands = toIsland.equals(fromIsland);
        boolean toInsideRange = toIsland.isInsideRange(to);
        boolean fromInsideRange = from != null && fromIsland != null && fromIsland.isInsideRange(from);
        boolean equalWorlds = from != null && to.getWorld().equals(from.getWorld());

        if (toInsideRange && (!equalIslands || !fromInsideRange) && !plugin.getEventsBus().callIslandEnterProtectedEvent(superiorPlayer, toIsland, enterCause)) {
            plugin.getEventsBus().callIslandRestrictMoveEvent(superiorPlayer, IslandRestrictMoveEvent.RestrictReason.ENTER_PROTECTED_EVENT_CANCELLED);
            return MoveResult.ENTER_EVENT_CANCELLED;
        }

        if (equalIslands) {
            if (!equalWorlds) {
                BukkitExecutor.sync(() -> plugin.getNMSWorld().setWorldBorder(superiorPlayer, toIsland), 1L);
                superiorPlayer.setPlayerStatus(PlayerStatus.PORTALS_IMMUNED);
                BukkitExecutor.sync(() -> {
                    superiorPlayer.removePlayerStatus(PlayerStatus.PORTALS_IMMUNED);
                }, 100L);
            }

            return MoveResult.SUCCESS;
        }

        if (!plugin.getEventsBus().callIslandEnterEvent(superiorPlayer, toIsland, enterCause)) {
            plugin.getEventsBus().callIslandRestrictMoveEvent(superiorPlayer, IslandRestrictMoveEvent.RestrictReason.ENTER_EVENT_CANCELLED);
            return MoveResult.ENTER_EVENT_CANCELLED;
        }

        toIsland.setPlayerInside(superiorPlayer, true);

        if (!toIsland.isMember(superiorPlayer) && toIsland.hasSettingsEnabled(IslandFlags.PVP)) {
            Message.ENTER_PVP_ISLAND.send(superiorPlayer);
            if (plugin.getSettings().isImmuneToPvPWhenTeleport()) {
                superiorPlayer.setPlayerStatus(PlayerStatus.PVP_IMMUNED);
                BukkitExecutor.sync(() -> {
                    superiorPlayer.removePlayerStatus(PlayerStatus.PVP_IMMUNED);
                }, 200L);
            }
        }

        superiorPlayer.setPlayerStatus(PlayerStatus.PORTALS_IMMUNED);
        BukkitExecutor.sync(() -> {
            superiorPlayer.removePlayerStatus(PlayerStatus.PORTALS_IMMUNED);
        }, 100L);

        Player player = superiorPlayer.asPlayer();
        if (player != null && (plugin.getSettings().getSpawn().isProtected() || !toIsland.isSpawn())) {
            BukkitExecutor.sync(() -> {
                // Update player time and player weather with a delay.
                // Fixes https://github.com/BG-Software-LLC/SuperiorSkyblock2/issues/1260
                if (toIsland.hasSettingsEnabled(IslandFlags.ALWAYS_DAY)) {
                    player.setPlayerTime(0, false);
                } else if (toIsland.hasSettingsEnabled(IslandFlags.ALWAYS_MIDDLE_DAY)) {
                    player.setPlayerTime(6000, false);
                } else if (toIsland.hasSettingsEnabled(IslandFlags.ALWAYS_NIGHT)) {
                    player.setPlayerTime(14000, false);
                } else if (toIsland.hasSettingsEnabled(IslandFlags.ALWAYS_MIDDLE_NIGHT)) {
                    player.setPlayerTime(18000, false);
                }

                if (toIsland.hasSettingsEnabled(IslandFlags.ALWAYS_SHINY)) {
                    player.setPlayerWeather(WeatherType.CLEAR);
                } else if (toIsland.hasSettingsEnabled(IslandFlags.ALWAYS_RAIN)) {
                    player.setPlayerWeather(WeatherType.DOWNFALL);
                }
            }, 1L);
        }

        if (superiorPlayer.hasIslandFlyEnabled() && !superiorPlayer.hasFlyGamemode()) {
            BukkitExecutor.sync(() -> {
                if (player != null) toIsland.updateIslandFly(superiorPlayer);
            }, 5L);
        }

        BukkitExecutor.sync(() -> {
            toIsland.applyEffects(superiorPlayer);
            plugin.getNMSWorld().setWorldBorder(superiorPlayer, toIsland);
        }, 1L);

        return MoveResult.SUCCESS;
    }

    private MoveResult handlePlayerLeaveIslandInternal(SuperiorPlayer superiorPlayer, Island fromIsland, Location from, @Nullable Location to, IslandLeaveEvent.LeaveCause leaveCause) {
        Island toIsland = to == null ? null : plugin.getGrid().getIslandAt(to);

        boolean equalWorlds = to != null && from.getWorld().equals(to.getWorld());
        boolean equalIslands = fromIsland.equals(toIsland);
        boolean fromInsideRange = fromIsland.isInsideRange(from);
        boolean toInsideRange = to != null && toIsland != null && toIsland.isInsideRange(to);

        //Checking for the stop leaving feature.
        if (plugin.getSettings().isStopLeaving() && fromInsideRange && !toInsideRange && !superiorPlayer.hasBypassModeEnabled() && !fromIsland.isSpawn() && equalWorlds) {
            plugin.getEventsBus().callIslandRestrictMoveEvent(superiorPlayer, IslandRestrictMoveEvent.RestrictReason.LEAVE_ISLAND_TO_OUTSIDE);
            superiorPlayer.setPlayerStatus(PlayerStatus.LEAVING_ISLAND);
            return MoveResult.LEAVE_ISLAND_TO_OUTSIDE;
        }

        // Handling the leave protected event
        if (fromInsideRange && (!equalIslands || !toInsideRange)) {
            if (!plugin.getEventsBus().callIslandLeaveProtectedEvent(superiorPlayer, fromIsland, leaveCause, to)) {
                plugin.getEventsBus().callIslandRestrictMoveEvent(superiorPlayer, IslandRestrictMoveEvent.RestrictReason.LEAVE_PROTECTED_EVENT_CANCELLED);
                return MoveResult.ENTER_EVENT_CANCELLED;
            }
        }

        if (equalIslands) return MoveResult.SUCCESS;

        if (!plugin.getEventsBus().callIslandLeaveEvent(superiorPlayer, fromIsland, leaveCause, to)) {
            plugin.getEventsBus().callIslandRestrictMoveEvent(superiorPlayer, IslandRestrictMoveEvent.RestrictReason.LEAVE_EVENT_CANCELLED);
            return MoveResult.ENTER_EVENT_CANCELLED;
        }

        fromIsland.setPlayerInside(superiorPlayer, false);

        Player player = superiorPlayer.asPlayer();
        if (player != null) {
            player.resetPlayerTime();
            player.resetPlayerWeather();
            fromIsland.removeEffects(superiorPlayer);

            if (superiorPlayer.hasIslandFlyEnabled() && (toIsland == null || toIsland.isSpawn()) && !superiorPlayer.hasFlyGamemode()) {
                player.setAllowFlight(false);
                player.setFlying(false);
                Message.ISLAND_FLY_DISABLED.send(player);
            }
        }

        if (toIsland == null) plugin.getNMSWorld().setWorldBorder(superiorPlayer, null);

        return MoveResult.SUCCESS;
    }

    @Nullable
    private static EntityType getSafeEntityType(String entityType) {
        try {
            return EntityType.valueOf(entityType);
        } catch (IllegalArgumentException error) {
            return null;
        }
    }

}

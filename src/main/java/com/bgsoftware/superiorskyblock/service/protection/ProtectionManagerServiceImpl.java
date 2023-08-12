package com.bgsoftware.superiorskyblock.service.protection;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.service.protection.InteractionResult;
import com.bgsoftware.superiorskyblock.api.service.protection.ProtectionManagerService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.service.IService;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import com.bgsoftware.superiorskyblock.world.BukkitItems;
import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ProtectionManagerServiceImpl implements ProtectionManagerService, IService {

    private static final Material FARMLAND = Materials.getMaterialSafe("FARMLAND", "SOIL");
    @Nullable
    private static final Material TURTLE_EGG = Materials.getMaterialSafe("TURTLE_EGG");
    @Nullable
    private static final Material SWEET_BERRY_BUSH = Materials.getMaterialSafe("SWEET_BERRY_BUSH");
    @Nullable
    private static final Material LECTERN = Materials.getMaterialSafe("LECTERN");
    @Nullable
    private static final EntityType AXOLOTL_TYPE = getSafeEntityType("AXOLOTL");
    private static final int MAX_PICKUP_DISTANCE = 1;

    private final SuperiorSkyblockPlugin plugin;

    public ProtectionManagerServiceImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Class<?> getAPIClass() {
        return ProtectionManagerService.class;
    }

    @Override
    public InteractionResult handleBlockPlace(SuperiorPlayer superiorPlayer, Block block) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(block, "block cannot be null");

        Location blockLocation = block.getLocation();
        Island island = plugin.getGrid().getIslandAt(blockLocation);

        return handleInteractionInternal(superiorPlayer, blockLocation, island, IslandPrivileges.BUILD,
                0, true, true);
    }

    @Override
    public InteractionResult handleBlockBreak(SuperiorPlayer superiorPlayer, Block block) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(block, "block cannot be null");

        Location blockLocation = block.getLocation();
        Island island = plugin.getGrid().getIslandAt(blockLocation);

        Material blockType = block.getType();
        IslandPrivilege islandPrivilege = blockType == Materials.SPAWNER.toBukkitType() ?
                IslandPrivileges.SPAWNER_BREAK : IslandPrivileges.BREAK;

        InteractionResult interactionResult = handleInteractionInternal(superiorPlayer, blockLocation, island, islandPrivilege,
                0, true, true);

        if (interactionResult != InteractionResult.SUCCESS)
            return interactionResult;

        if (island == null)
            return InteractionResult.SUCCESS;

        if (plugin.getSettings().getValuableBlocks().contains(Keys.of(block)))
            return handleInteractionInternal(superiorPlayer, blockLocation, island, IslandPrivileges.VALUABLE_BREAK,
                    0, false, false);

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult handleBlockInteract(SuperiorPlayer superiorPlayer, Block block, Action action,
                                                 @Nullable ItemStack usedItem) {
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

        return handleInteractionInternal(superiorPlayer, blockLocation, island, islandPrivilege,
                0, false, false);
    }

    @Override
    public InteractionResult handleBlockFertilize(SuperiorPlayer superiorPlayer, Block block) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(block, "block cannot be null");

        Location blockLocation = block.getLocation();
        Island island = plugin.getGrid().getIslandAt(blockLocation);

        return handleInteractionInternal(superiorPlayer, blockLocation, island, IslandPrivileges.FERTILIZE,
                0, true, true);
    }

    @Override
    public InteractionResult handleEntityInteract(SuperiorPlayer superiorPlayer, Entity entity, @Nullable ItemStack usedItem) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(entity, "entity cannot be null");

        Location entityLocation = entity.getLocation();
        Island island = plugin.getGrid().getIslandAt(entityLocation);

        InteractionResult interactionResult = handleInteractionInternal(superiorPlayer, entityLocation, island,
                null, 0, true, false);

        if (interactionResult != InteractionResult.SUCCESS)
            return interactionResult;

        if (island == null)
            return InteractionResult.SUCCESS;

        boolean closeInventory = false;

        IslandPrivilege islandPrivilege;

        if (entity instanceof ArmorStand) {
            islandPrivilege = IslandPrivileges.INTERACT;
        } else if (usedItem != null && entity instanceof Animals &&
                plugin.getNMSEntities().isAnimalFood(usedItem, (Animals) entity)) {
            islandPrivilege = IslandPrivileges.ANIMAL_BREED;
        } else if (usedItem != null && usedItem.getType() == Material.NAME_TAG) {
            islandPrivilege = IslandPrivileges.NAME_ENTITY;
        } else if (entity instanceof Villager) {
            islandPrivilege = IslandPrivileges.VILLAGER_TRADING;
            closeInventory = true;
        } else if (entity instanceof Horse || (ServerVersion.isAtLeast(ServerVersion.v1_11) && (
                entity instanceof Mule || entity instanceof Donkey))) {
            islandPrivilege = IslandPrivileges.HORSE_INTERACT;
            closeInventory = true;
        } else if (usedItem != null && entity instanceof Creeper &&
                usedItem.getType() == Material.FLINT_AND_STEEL) {
            islandPrivilege = IslandPrivileges.IGNITE_CREEPER;
        } else if (usedItem != null && ServerVersion.isAtLeast(ServerVersion.v1_17) &&
                usedItem.getType() == Material.WATER_BUCKET && entity.getType() == AXOLOTL_TYPE) {
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

        interactionResult = handleInteractionInternal(superiorPlayer, entityLocation, island, islandPrivilege,
                0, false, false);

        if (closeInventory && interactionResult != InteractionResult.SUCCESS) {
            BukkitExecutor.sync(() -> {
                Player player = superiorPlayer.asPlayer();
                if (player != null && player.isOnline()) {
                    Inventory openInventory = player.getOpenInventory().getTopInventory();
                    if (openInventory != null && (openInventory.getType() == InventoryType.MERCHANT ||
                            openInventory.getType() == InventoryType.CHEST))
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

        if (!damagerSource.isPresent())
            return InteractionResult.SUCCESS;

        Location entityLocation = entity.getLocation();
        Island island = plugin.getGrid().getIslandAt(entityLocation);
        IslandPrivilege islandPrivilege = BukkitEntities.getCategory(entity.getType()).getDamagePrivilege();

        InteractionResult interactionResult = handleInteractionInternal(damagerSource.get(), entityLocation, island, islandPrivilege,
                0, true, false);

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

        return handleInteractionInternal(superiorPlayer, entityLocation, island, IslandPrivileges.ANIMAL_SHEAR,
                0, true, false);
    }

    @Override
    public InteractionResult handleEntityLeash(SuperiorPlayer superiorPlayer, Entity entity) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(entity, "entity cannot be null");

        Location entityLocation = entity.getLocation();
        Island island = plugin.getGrid().getIslandAt(entityLocation);

        return handleInteractionInternal(superiorPlayer, entityLocation, island, IslandPrivileges.LEASH,
                0, true, false);
    }

    @Override
    public InteractionResult handlePlayerPickupItem(SuperiorPlayer superiorPlayer, Item item) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(item, "item cannot be null");

        if (plugin.getNMSPlayers().wasThrownByPlayer(item, superiorPlayer))
            return InteractionResult.SUCCESS;

        Location itemLocation = item.getLocation();
        Island island = plugin.getGrid().getIslandAt(itemLocation);

        return handleInteractionInternal(superiorPlayer, itemLocation, island, IslandPrivileges.PICKUP_DROPS,
                MAX_PICKUP_DISTANCE, true, false);
    }

    @Override
    public InteractionResult handlePlayerDropItem(SuperiorPlayer superiorPlayer, Item item) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(item, "item cannot be null");

        Location itemLocation = item.getLocation();
        Island island = plugin.getGrid().getIslandAt(itemLocation);

        return handleInteractionInternal(superiorPlayer, itemLocation, island, IslandPrivileges.DROP_ITEMS,
                0, true, false);
    }

    @Override
    public InteractionResult handlePlayerEnderPearl(SuperiorPlayer superiorPlayer, Location destination) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(destination, "destination cannot be null");
        Preconditions.checkArgument(destination.getWorld() != null, "destination's world cannot be null");

        Island island = plugin.getGrid().getIslandAt(destination);

        InteractionResult interactionResult = handleInteractionInternal(superiorPlayer, destination, island,
                null, 0, true, false);

        if (interactionResult != null)
            return interactionResult;

        if (island == null)
            return InteractionResult.SUCCESS;

        return handleInteractionInternal(superiorPlayer, destination, island, IslandPrivileges.ENDER_PEARL,
                0, false, false);
    }

    @Override
    public InteractionResult handleCustomInteraction(SuperiorPlayer superiorPlayer, Location location, IslandPrivilege islandPrivilege) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(location, "location cannot be null");
        Preconditions.checkNotNull(islandPrivilege, "islandPrivilege cannot be null");

        Island island = plugin.getGrid().getIslandAt(location);
        return handleInteractionInternal(superiorPlayer, location, island, islandPrivilege,
                0, true, false);
    }

    private InteractionResult handleInteractionInternal(SuperiorPlayer superiorPlayer, Location location,
                                                        @Nullable Island island, @Nullable IslandPrivilege islandPrivilege,
                                                        int extraRadius, boolean checkIslandBoundaries, boolean checkRecalculation) {
        if (superiorPlayer.hasBypassModeEnabled())
            return InteractionResult.SUCCESS;

        if (checkIslandBoundaries) {
            if (island == null && plugin.getGrid().isIslandsWorld(superiorPlayer.getWorld()))
                return InteractionResult.OUTSIDE_ISLAND;

            if (island != null && !island.isInsideRange(location, extraRadius))
                return InteractionResult.OUTSIDE_ISLAND;
        }

        if (island != null) {
            if (islandPrivilege != null && !island.hasPermission(superiorPlayer, islandPrivilege))
                return InteractionResult.MISSING_PRIVILEGE;

            if (checkRecalculation && island.isBeingRecalculated())
                return InteractionResult.ISLAND_RECALCULATE;
        }

        return InteractionResult.SUCCESS;
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

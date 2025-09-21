package com.bgsoftware.superiorskyblock.service.region;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandEnterEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandLeaveEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandRestrictMoveEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPreview;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.api.player.PlayerStatus;
import com.bgsoftware.superiorskyblock.api.service.region.InteractionResult;
import com.bgsoftware.superiorskyblock.api.service.region.MoveResult;
import com.bgsoftware.superiorskyblock.api.service.region.RegionManagerService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.collections.EnumerateSet;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventType;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsDispatcher;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.key.set.KeySets;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Villager;
import org.bukkit.entity.minecart.PoweredMinecart;
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
    private static final Material VAULT = EnumHelper.getEnum(Material.class, "VAULT");
    @Nullable
    private static final Material TRIAL_KEY = EnumHelper.getEnum(Material.class, "TRIAL_KEY");
    @Nullable
    private static final Material OMINOUS_TRIAL_KEY = EnumHelper.getEnum(Material.class, "OMINOUS_TRIAL_KEY");
    @Nullable
    private static final EntityType AXOLOTL_TYPE = EnumHelper.getEnum(EntityType.class, "AXOLOTL");
    @Nullable
    private static final EntityType LLAMA_TYPE = EnumHelper.getEnum(EntityType.class, "LLAMA");
    @Nullable
    private static final EntityType HAPPY_GHAST_TYPE = EnumHelper.getEnum(EntityType.class, "HAPPY_GHAST");

    private static final int MAX_PICKUP_DISTANCE = 1;
    private static EnumerateSet<IslandPrivilege> WORLD_PERMISSIONS_CACHE;
    private static KeySet INTERACTABLES_CACHE;

    private final SuperiorSkyblockPlugin plugin;

    public RegionManagerServiceImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    public static void registerCallbacks(PluginEventsDispatcher dispatcher) {
        dispatcher.registerCallback(PluginEventType.SETTINGS_UPDATE_EVENT, RegionManagerServiceImpl::onSettingsUpdate);
    }

    private static void onSettingsUpdate() {
        SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
        WORLD_PERMISSIONS_CACHE = new EnumerateSet<>(IslandPrivilege.values());
        plugin.getSettings().getWorldPermissions().forEach(islandPrivilageName -> {
            try {
                WORLD_PERMISSIONS_CACHE.add(IslandPrivilege.getByName(islandPrivilageName));
            } catch (Throwable ignored) {
            }
        });

        INTERACTABLES_CACHE = KeySets.createHashSet(KeyIndicator.MATERIAL);
        plugin.getSettings().getInteractables().forEach(interactableName -> {
            INTERACTABLES_CACHE.add(Keys.ofMaterialAndData(interactableName));
        });

    }

    @Override
    public Class<?> getAPIClass() {
        return RegionManagerService.class;
    }

    @Override
    public InteractionResult handleBlockPlace(SuperiorPlayer superiorPlayer, Block block) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(block, "block cannot be null");

        // We do not care about spawn island when spawn protection is disabled,
        // and therefore only island worlds are relevant.
        if (!plugin.getSettings().getSpawn().isProtected() && !plugin.getGrid().isIslandsWorld(block.getWorld()))
            return InteractionResult.SUCCESS;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location blockLocation = block.getLocation(wrapper.getHandle());
            return handleInteractionInternal(superiorPlayer, blockLocation, IslandPrivileges.BUILD,
                    0, true, true);
        }
    }

    @Override
    public InteractionResult handleBlockBreak(SuperiorPlayer superiorPlayer, Block block) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(block, "block cannot be null");

        // We do not care about spawn island when spawn protection is disabled,
        // and therefore only island worlds are relevant.
        if (!plugin.getSettings().getSpawn().isProtected() && !plugin.getGrid().isIslandsWorld(block.getWorld()))
            return InteractionResult.SUCCESS;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location blockLocation = block.getLocation(wrapper.getHandle());
            Island island = plugin.getGrid().getIslandAt(blockLocation);

            Material blockType = block.getType();
            IslandPrivilege islandPrivilege = blockType == Materials.SPAWNER.toBukkitType() ? IslandPrivileges.SPAWNER_BREAK : IslandPrivileges.BREAK;

            InteractionResult interactionResult = handleInteractionInternal(superiorPlayer, blockLocation, islandPrivilege,
                    0, true, true, island, false);

            if (interactionResult != InteractionResult.SUCCESS)
                return interactionResult;

            if (island == null)
                return InteractionResult.SUCCESS;

            if (plugin.getSettings().getValuableBlocks().contains(Keys.of(block)))
                return handleInteractionInternal(superiorPlayer, blockLocation, IslandPrivileges.VALUABLE_BREAK,
                        0, false, false, island, false);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult handleBlockInteract(SuperiorPlayer superiorPlayer, Block block, Action action, @Nullable ItemStack usedItem) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(block, "block cannot be null");

        // We do not care about spawn island when spawn protection is disabled,
        // and therefore only island worlds are relevant.
        if (!plugin.getSettings().getSpawn().isProtected() && !plugin.getGrid().isIslandsWorld(block.getWorld()))
            return InteractionResult.SUCCESS;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location blockLocation = block.getLocation(wrapper.getHandle());
            Key blockKey = Keys.of(block);

            boolean isInteractableItem = BukkitItems.isInteractableItem(usedItem);

            int stackedBlockAmount = plugin.getStackedBlocks().getStackedBlockAmount(blockLocation);

            if (!isInteractableItem && stackedBlockAmount <= 1 && !INTERACTABLES_CACHE.contains(blockKey))
                return InteractionResult.SUCCESS;

            BlockState blockState = block.getState();
            EntityType spawnType = usedItem == null ? EntityType.UNKNOWN : BukkitItems.getEntityType(usedItem);
            Material blockType = block.getType();

            IslandPrivilege islandPrivilege;

            if (spawnType != EntityType.UNKNOWN) {
                islandPrivilege = BukkitEntities.getCategory(spawnType).getSpawnPrivilege();
            } else if (usedItem != null && Materials.isMinecart(usedItem.getType()) ? Materials.isRail(blockType) : Materials.isBoat(blockType)) {
                islandPrivilege = IslandPrivileges.MINECART_PLACE;
            } else if (Materials.isChest(blockType)) {
                islandPrivilege = IslandPrivileges.CHEST_ACCESS;
            } else if (blockState instanceof InventoryHolder) {
                islandPrivilege = IslandPrivileges.USE;
            } else if (usedItem != null && blockType == VAULT && (usedItem.getType() == TRIAL_KEY || usedItem.getType() == OMINOUS_TRIAL_KEY)) {
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

            return handleInteractionInternal(superiorPlayer, blockLocation, islandPrivilege,
                    0, true, true);
        }
    }

    @Override
    public InteractionResult handleBlockFertilize(SuperiorPlayer superiorPlayer, Block block) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(block, "block cannot be null");

        // We do not care about spawn island when spawn protection is disabled,
        // and therefore only island worlds are relevant.
        if (!plugin.getSettings().getSpawn().isProtected() && !plugin.getGrid().isIslandsWorld(block.getWorld()))
            return InteractionResult.SUCCESS;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location blockLocation = block.getLocation(wrapper.getHandle());
            return handleInteractionInternal(superiorPlayer, blockLocation, IslandPrivileges.FERTILIZE,
                    0, true, true);
        }
    }

    @Override
    public InteractionResult handleEntityInteract(SuperiorPlayer superiorPlayer, Entity entity, @Nullable ItemStack usedItem) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(entity, "entity cannot be null");

        // We do not care about spawn island when spawn protection is disabled,
        // and therefore only island worlds are relevant.
        if (!plugin.getSettings().getSpawn().isProtected() && !plugin.getGrid().isIslandsWorld(entity.getWorld()))
            return InteractionResult.SUCCESS;

        InteractionResult interactionResult;
        boolean closeInventory = false;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location entityLocation = entity.getLocation(wrapper.getHandle());

            EntityType entityType = entity.getType();
            Material usedItemType = usedItem == null ? Material.AIR : usedItem.getType();

            IslandPrivilege islandPrivilege;

            if (entity instanceof ArmorStand) {
                islandPrivilege = IslandPrivileges.INTERACT;
            } else if (usedItem != null && entity instanceof Animals && plugin.getNMSEntities().isAnimalFood(usedItem, (Animals) entity)) {
                islandPrivilege = IslandPrivileges.ANIMAL_BREED;
            } else if (usedItemType == Material.NAME_TAG) {
                islandPrivilege = IslandPrivileges.NAME_ENTITY;
            } else if (usedItemType == Material.SADDLE || (entityType == LLAMA_TYPE && Materials.isCarpet(usedItemType)) ||
                    (entityType == HAPPY_GHAST_TYPE && Materials.isHarness(usedItemType)) ||
                    (usedItemType == Material.SHEARS && plugin.getNMSEntities().canShearSaddleFromEntity(entity))) {
                islandPrivilege = IslandPrivileges.SADDLE_ENTITY;
            } else if (entity instanceof Villager) {
                islandPrivilege = IslandPrivileges.VILLAGER_TRADING;
                closeInventory = true;
            } else if (BukkitEntities.isHorse(entity)) {
                islandPrivilege = IslandPrivileges.HORSE_INTERACT;
                closeInventory = true;
            } else if (usedItemType == Material.FLINT_AND_STEEL && entity instanceof Creeper) {
                islandPrivilege = IslandPrivileges.IGNITE_CREEPER;
            } else if (usedItemType == Material.WATER_BUCKET && entityType == AXOLOTL_TYPE && ServerVersion.isAtLeast(ServerVersion.v1_17)) {
                islandPrivilege = IslandPrivileges.PICKUP_AXOLOTL;
            } else if (entity instanceof ItemFrame) {
                islandPrivilege = IslandPrivileges.ITEM_FRAME;
            } else if (entity instanceof Painting) {
                islandPrivilege = IslandPrivileges.PAINTING;
            } else if (entity instanceof Fish && !ServerVersion.isLegacy()) {
                islandPrivilege = IslandPrivileges.PICKUP_FISH;
            } else if (usedItem != null && entity instanceof PoweredMinecart &&
                    plugin.getNMSEntities().isMinecartFuel(usedItem, (PoweredMinecart) entity)) {
                islandPrivilege = IslandPrivileges.MINECART_OPEN;
            } else if (entity instanceof Sheep && Materials.isDye(usedItemType)) {
                islandPrivilege = IslandPrivileges.DYE_SHEEP;
            } else {
                return InteractionResult.SUCCESS;
            }

            interactionResult = handleInteractionInternal(superiorPlayer, entityLocation, islandPrivilege,
                    0, true, true);
        }

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

        // We do not care about spawn island when spawn protection is disabled,
        // and therefore only island worlds are relevant.
        if (!plugin.getSettings().getSpawn().isProtected() && !plugin.getGrid().isIslandsWorld(entity.getWorld()))
            return InteractionResult.SUCCESS;

        Optional<SuperiorPlayer> damagerSource = BukkitEntities.getPlayerSource(damager).map(plugin.getPlayers()::getSuperiorPlayer);

        if (!damagerSource.isPresent())
            return InteractionResult.SUCCESS;

        InteractionResult interactionResult;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location entityLocation = entity.getLocation(wrapper.getHandle());
            IslandPrivilege islandPrivilege = BukkitEntities.isTameable(entity) ? IslandPrivileges.TAMED_ANIMAL_DAMAGE :
                    BukkitEntities.getCategory(entity.getType()).getDamagePrivilege();

            interactionResult = handleInteractionInternal(damagerSource.get(), entityLocation, islandPrivilege,
                    0, true, false);
        }

        if (interactionResult != InteractionResult.SUCCESS && damager instanceof Arrow && entity.getFireTicks() > 0)
            entity.setFireTicks(0);

        return interactionResult;
    }

    @Override
    public InteractionResult handleEntityRide(SuperiorPlayer superiorPlayer, Entity vehicle) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(vehicle, "vehicle cannot be null");

        // We do not care about spawn island when spawn protection is disabled,
        // and therefore only island worlds are relevant.
        if (!plugin.getSettings().getSpawn().isProtected() && !plugin.getGrid().isIslandsWorld(vehicle.getWorld()))
            return InteractionResult.SUCCESS;

        IslandPrivilege islandPrivilege = BukkitEntities.isHorse(vehicle) ? IslandPrivileges.HORSE_INTERACT :
                vehicle instanceof Animals ? IslandPrivileges.ENTITY_RIDE : IslandPrivileges.MINECART_ENTER;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location entityLocation = vehicle.getLocation(wrapper.getHandle());
            return handleInteractionInternal(superiorPlayer, entityLocation, islandPrivilege,
                    0, true, false);
        }
    }

    @Override
    public InteractionResult handleEntityShear(SuperiorPlayer superiorPlayer, Entity entity) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(entity, "entity cannot be null");

        // We do not care about spawn island when spawn protection is disabled,
        // and therefore only island worlds are relevant.
        if (!plugin.getSettings().getSpawn().isProtected() && !plugin.getGrid().isIslandsWorld(entity.getWorld()))
            return InteractionResult.SUCCESS;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location entityLocation = entity.getLocation(wrapper.getHandle());
            return handleInteractionInternal(superiorPlayer, entityLocation, IslandPrivileges.ANIMAL_SHEAR,
                    0, true, false);
        }
    }

    @Override
    public InteractionResult handleEntityLeash(SuperiorPlayer superiorPlayer, Entity entity) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(entity, "entity cannot be null");

        // We do not care about spawn island when spawn protection is disabled,
        // and therefore only island worlds are relevant.
        if (!plugin.getSettings().getSpawn().isProtected() && !plugin.getGrid().isIslandsWorld(entity.getWorld()))
            return InteractionResult.SUCCESS;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location entityLocation = entity.getLocation(wrapper.getHandle());
            return handleInteractionInternal(superiorPlayer, entityLocation, IslandPrivileges.LEASH,
                    0, true, false);
        }
    }

    @Override
    public InteractionResult handlePlayerPickupItem(SuperiorPlayer superiorPlayer, Item item) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(item, "item cannot be null");

        if (plugin.getNMSPlayers().wasThrownByPlayer(item, superiorPlayer))
            return InteractionResult.SUCCESS;

        // We do not care about spawn island when spawn protection is disabled,
        // and therefore only island worlds are relevant.
        if (!plugin.getSettings().getSpawn().isProtected() && !plugin.getGrid().isIslandsWorld(item.getWorld()))
            return InteractionResult.SUCCESS;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location itemLocation = item.getLocation(wrapper.getHandle());
            return handleInteractionInternal(superiorPlayer, itemLocation, IslandPrivileges.PICKUP_DROPS,
                    MAX_PICKUP_DISTANCE, true, false);
        }
    }

    @Override
    public InteractionResult handlePlayerDropItem(SuperiorPlayer superiorPlayer, Item item) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(item, "item cannot be null");

        // We do not care about spawn island when spawn protection is disabled,
        // and therefore only island worlds are relevant.
        if (!plugin.getSettings().getSpawn().isProtected() && !plugin.getGrid().isIslandsWorld(item.getWorld()))
            return InteractionResult.SUCCESS;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location itemLocation = item.getLocation(wrapper.getHandle());
            return handleInteractionInternal(superiorPlayer, itemLocation, IslandPrivileges.DROP_ITEMS,
                    0, true, false);
        }
    }

    @Override
    public InteractionResult handlePlayerEnderPearl(SuperiorPlayer superiorPlayer, Location destination) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(destination, "destination cannot be null");
        Preconditions.checkArgument(destination.getWorld() != null, "destination's world cannot be null");

        // We do not care about spawn island when spawn protection is disabled,
        // and therefore only island worlds are relevant.
        if (!plugin.getSettings().getSpawn().isProtected() && !plugin.getGrid().isIslandsWorld(destination.getWorld()))
            return InteractionResult.SUCCESS;

        return handleInteractionInternal(superiorPlayer, destination, IslandPrivileges.ENDER_PEARL,
                0, true, false);
    }

    @Override
    public InteractionResult handlePlayerConsumeChorusFruit(SuperiorPlayer superiorPlayer, Location location) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(location, "location cannot be null");
        Preconditions.checkArgument(location.getWorld() != null, "location's world cannot be null");

        if (IslandPrivileges.CHORUS_FRUIT == null) {
            // Chorus Fruit privilege does not exist, we will just return SUCCESS in this case.
            return InteractionResult.SUCCESS;
        }

        // We do not care about spawn island when spawn protection is disabled,
        // and therefore only island worlds are relevant.
        if (!plugin.getSettings().getSpawn().isProtected() && !plugin.getGrid().isIslandsWorld(location.getWorld()))
            return InteractionResult.SUCCESS;

        return handleInteractionInternal(superiorPlayer, location, IslandPrivileges.CHORUS_FRUIT,
                0, true, true);
    }

    @Override
    public InteractionResult handlePlayerUseWindCharge(SuperiorPlayer superiorPlayer, Location location) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(location, "location cannot be null");
        Preconditions.checkArgument(location.getWorld() != null, "location's world cannot be null");

        if (IslandPrivileges.WIND_CHARGE == null) {
            // Wind Charge privilege does not exist, we will just return SUCCESS in this case.
            return InteractionResult.SUCCESS;
        }

        // We do not care about spawn island when spawn protection is disabled,
        // and therefore only island worlds are relevant.
        if (!plugin.getSettings().getSpawn().isProtected() && !plugin.getGrid().isIslandsWorld(location.getWorld()))
            return InteractionResult.SUCCESS;

        return handleInteractionInternal(superiorPlayer, location, IslandPrivileges.WIND_CHARGE,
                0, true, true);
    }

    @Override
    public InteractionResult handleCustomInteraction(SuperiorPlayer superiorPlayer, Location location, IslandPrivilege islandPrivilege) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(location, "location cannot be null");
        Preconditions.checkNotNull(islandPrivilege, "islandPrivilege cannot be null");

        // We do not care about spawn island when spawn protection is disabled,
        // and therefore only island worlds are relevant.
        if (!plugin.getSettings().getSpawn().isProtected() && !plugin.getGrid().isIslandsWorld(location.getWorld()))
            return InteractionResult.SUCCESS;

        return handleInteractionInternal(superiorPlayer, location, islandPrivilege,
                0, true, false);
    }

    private InteractionResult handleInteractionInternal(SuperiorPlayer superiorPlayer, Location location,
                                                        IslandPrivilege islandPrivilege, int extraRadius,
                                                        boolean checkIslandBoundaries, boolean checkRecalculation) {
        return handleInteractionInternal(superiorPlayer, location, islandPrivilege, extraRadius, checkIslandBoundaries,
                checkRecalculation, null, true);
    }

    private InteractionResult handleInteractionInternal(SuperiorPlayer superiorPlayer, Location location,
                                                        IslandPrivilege islandPrivilege, int extraRadius,
                                                        boolean checkIslandBoundaries, boolean checkRecalculation,
                                                        @Nullable Island island, boolean callIslandLookup) {
        if (superiorPlayer.hasBypassModeEnabled())
            return InteractionResult.SUCCESS;

        if (callIslandLookup) {
            island = plugin.getGrid().getIslandAt(location);
        }

        if (checkIslandBoundaries && !WORLD_PERMISSIONS_CACHE.contains(islandPrivilege)) {
            if (island == null && plugin.getGrid().isIslandsWorld(superiorPlayer.getWorld()))
                return InteractionResult.OUTSIDE_ISLAND;

            if (island != null && !island.isInsideRange(location, extraRadius))
                return InteractionResult.OUTSIDE_ISLAND;
        }

        if (island != null) {
            if (!island.hasPermission(superiorPlayer, islandPrivilege))
                return InteractionResult.MISSING_PRIVILEGE;

            if (checkRecalculation && island.isBeingRecalculated())
                return InteractionResult.ISLAND_RECALCULATE;
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public MoveResult handlePlayerMove(SuperiorPlayer superiorPlayer, Location from, Location to) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(from, "from cannot be null");
        Preconditions.checkNotNull(to, "to cannot be null");

        Island fromIsland = null;
        boolean lookupFromIsland = true;

        //Checking for out of distance from preview location.
        IslandPreview islandPreview = plugin.getGrid().getIslandPreview(superiorPlayer);
        if (islandPreview != null) {
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                Location islandPreviewLocation = islandPreview.getLocation(wrapper.getHandle());
                if (!islandPreviewLocation.getWorld().equals(to.getWorld()) ||
                        islandPreviewLocation.distance(to) > plugin.getSettings().getIslandPreviews().getMaxDistance()) {
                    islandPreview.handleEscape();
                    return MoveResult.ISLAND_PREVIEW_MOVED_TOO_FAR;
                }
            }
        }

        if (from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ()) {
            // Handle moving while in teleport warmup.
            BukkitTask teleportTask = superiorPlayer.getTeleportTask();
            if (teleportTask != null) {
                teleportTask.cancel();
                superiorPlayer.setTeleportTask(null);
                Message.TELEPORT_WARMUP_CANCEL.send(superiorPlayer);
            }

            MoveResult moveResult;

            Island toIsland = plugin.getGrid().getIslandAt(to);
            if (toIsland != null) {
                moveResult = handlePlayerEnterIslandInternal(superiorPlayer, toIsland, from, to, IslandEnterEvent.EnterCause.PLAYER_MOVE);
                if (moveResult != MoveResult.SUCCESS)
                    return moveResult;
            }

            lookupFromIsland = false;

            fromIsland = plugin.getGrid().getIslandAt(from);
            if (fromIsland != null) {
                moveResult = handlePlayerLeaveIslandInternal(superiorPlayer, fromIsland, from, to, IslandLeaveEvent.LeaveCause.PLAYER_MOVE);
                if (moveResult != MoveResult.SUCCESS)
                    return moveResult;
            }
        }

        if (from.getBlockY() != to.getBlockY() && to.getBlockY() <= plugin.getNMSWorld().getMinHeight(to.getWorld()) - 5) {
            if (lookupFromIsland) {
                fromIsland = plugin.getGrid().getIslandAt(from);
            }

            if (fromIsland == null || (fromIsland.isVisitor(superiorPlayer, false) ?
                    !plugin.getSettings().getVoidTeleport().isVisitors() : !plugin.getSettings().getVoidTeleport().isMembers()))
                return MoveResult.SUCCESS;

            Log.debug(Debug.VOID_TELEPORT, superiorPlayer.getName());

            superiorPlayer.setPlayerStatus(PlayerStatus.VOID_TELEPORT);

            superiorPlayer.teleport(fromIsland, result -> {
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
        if (island == null)
            return MoveResult.SUCCESS;

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
            PluginEventsFactory.callIslandRestrictMoveEvent(toIsland, superiorPlayer, IslandRestrictMoveEvent.RestrictReason.BANNED_FROM_ISLAND);
            Message.BANNED_FROM_ISLAND.send(superiorPlayer);
            return MoveResult.BANNED_FROM_ISLAND;
        }

        // Checking if the player is locked to visitors.
        if (toIsland.isLocked() && !toIsland.hasPermission(superiorPlayer, IslandPrivileges.CLOSE_BYPASS)) {
            PluginEventsFactory.callIslandRestrictMoveEvent(toIsland, superiorPlayer, IslandRestrictMoveEvent.RestrictReason.LOCKED_ISLAND);
            Message.NO_CLOSE_BYPASS.send(superiorPlayer);
            return MoveResult.ISLAND_LOCKED;
        }

        Island fromIsland = from == null ? null : plugin.getGrid().getIslandAt(from);

        boolean equalIslands = toIsland.equals(fromIsland);
        boolean toInsideRange = toIsland.isInsideRange(to);
        boolean fromInsideRange = from != null && fromIsland != null && fromIsland.isInsideRange(from);
        boolean equalWorlds = from != null && to.getWorld().equals(from.getWorld());

        if (toInsideRange && (!equalIslands || !fromInsideRange) && !PluginEventsFactory.callIslandEnterProtectedEvent(toIsland, superiorPlayer, enterCause)) {
            PluginEventsFactory.callIslandRestrictMoveEvent(toIsland, superiorPlayer, IslandRestrictMoveEvent.RestrictReason.ENTER_PROTECTED_EVENT_CANCELLED);
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

        if (!PluginEventsFactory.callIslandEnterEvent(toIsland, superiorPlayer, enterCause)) {
            PluginEventsFactory.callIslandRestrictMoveEvent(toIsland, superiorPlayer, IslandRestrictMoveEvent.RestrictReason.ENTER_EVENT_CANCELLED);
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
            PluginEventsFactory.callIslandRestrictMoveEvent(fromIsland, superiorPlayer, IslandRestrictMoveEvent.RestrictReason.LEAVE_ISLAND_TO_OUTSIDE);
            superiorPlayer.setPlayerStatus(PlayerStatus.LEAVING_ISLAND);
            return MoveResult.LEAVE_ISLAND_TO_OUTSIDE;
        }

        // Handling the leave protected event
        if (fromInsideRange && (!equalIslands || !toInsideRange)) {
            if (!PluginEventsFactory.callIslandLeaveProtectedEvent(fromIsland, superiorPlayer, leaveCause, to)) {
                PluginEventsFactory.callIslandRestrictMoveEvent(fromIsland, superiorPlayer, IslandRestrictMoveEvent.RestrictReason.LEAVE_PROTECTED_EVENT_CANCELLED);
                return MoveResult.ENTER_EVENT_CANCELLED;
            }
        }

        if (equalIslands)
            return MoveResult.SUCCESS;

        if (!PluginEventsFactory.callIslandLeaveEvent(fromIsland, superiorPlayer, leaveCause, to)) {
            PluginEventsFactory.callIslandRestrictMoveEvent(fromIsland, superiorPlayer, IslandRestrictMoveEvent.RestrictReason.LEAVE_EVENT_CANCELLED);
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

}

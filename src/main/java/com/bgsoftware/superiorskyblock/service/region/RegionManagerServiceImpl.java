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
import com.bgsoftware.superiorskyblock.api.player.PlayerStatus;
import com.bgsoftware.superiorskyblock.api.player.algorithm.PlayerTeleportAlgorithm;
import com.bgsoftware.superiorskyblock.api.service.dragon.DragonBattleService;
import com.bgsoftware.superiorskyblock.api.service.region.InteractionResult;
import com.bgsoftware.superiorskyblock.api.service.region.MoveResult;
import com.bgsoftware.superiorskyblock.api.service.region.RegionManagerService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Computed;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.MutableBlockPosition;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.collections.UnparsedEnumerateSet;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventType;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsDispatcher;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.flag.IslandFlags;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.player.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.service.IService;
import com.bgsoftware.superiorskyblock.service.dragon.DragonBattleServiceImpl;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import com.bgsoftware.superiorskyblock.world.BukkitItems;
import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Villager;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class RegionManagerServiceImpl implements RegionManagerService, IService {

    private static final Material FARMLAND = EnumHelper.getEnum(Material.class, "FARMLAND", "SOIL");
    @Nullable
    private static final Material CAVE_VINES = EnumHelper.getEnum(Material.class, "CAVE_VINES");
    @Nullable
    private static final Material CAVE_VINES_PLANT = EnumHelper.getEnum(Material.class, "CAVE_VINES_PLANT");
    @Nullable
    private static final Material FIRE_CHARGE = EnumHelper.getEnum(Material.class, "FIRE_CHARGE");
    @Nullable
    private static final Material GOLDEN_DANDELION = EnumHelper.getEnum(Material.class, "GOLDEN_DANDELION");
    @Nullable
    private static final Material LECTERN = EnumHelper.getEnum(Material.class, "LECTERN");
    @Nullable
    private static final Material OMINOUS_TRIAL_KEY = EnumHelper.getEnum(Material.class, "OMINOUS_TRIAL_KEY");
    @Nullable
    private static final Material SWEET_BERRY_BUSH = EnumHelper.getEnum(Material.class, "SWEET_BERRY_BUSH");
    @Nullable
    private static final Material TRIAL_KEY = EnumHelper.getEnum(Material.class, "TRIAL_KEY");
    @Nullable
    private static final Material TURTLE_EGG = EnumHelper.getEnum(Material.class, "TURTLE_EGG");
    @Nullable
    private static final Material VAULT = EnumHelper.getEnum(Material.class, "VAULT");

    @Nullable
    private static final EntityType HAPPY_GHAST = EnumHelper.getEnum(EntityType.class, "HAPPY_GHAST");
    @Nullable
    private static final EntityType LLAMA = EnumHelper.getEnum(EntityType.class, "LLAMA");
    @Nullable
    private static final EntityType PARROT = EnumHelper.getEnum(EntityType.class, "PARROT");

    private static final int MAX_PICKUP_DISTANCE = 1;
    private static UnparsedEnumerateSet<IslandPrivilege> WORLD_PERMISSIONS_CACHE;
    private static List<IslandPrivilege> LECTERN_PLACE_PERMISSIONS;
    private static List<IslandPrivilege> LECTERN_BREAK_PERMISSIONS;

    private final SuperiorSkyblockPlugin plugin;

    private final LazyReference<DragonBattleServiceImpl> dragonBattleService = new LazyReference<DragonBattleServiceImpl>() {
        @Override
        protected DragonBattleServiceImpl create() {
            return (DragonBattleServiceImpl) plugin.getServices().getService(DragonBattleService.class);
        }
    };

    public RegionManagerServiceImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    public static void registerListeners(PluginEventsDispatcher dispatcher) {
        dispatcher.registerCallback(PluginEventType.SETTINGS_UPDATE_EVENT, RegionManagerServiceImpl::onSettingsUpdate);
    }

    private static void onSettingsUpdate() {
        SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
        WORLD_PERMISSIONS_CACHE = new UnparsedEnumerateSet<IslandPrivilege>(IslandPrivilege.values()) {
            @Override
            protected IslandPrivilege parseName(String name) {
                return IslandPrivilege.getByNameOrNull(name);
            }

            @Override
            protected String getName(IslandPrivilege islandPrivilege) {
                return islandPrivilege.getName();
            }
        };
        plugin.getSettings().getWorldPermissions().forEach(WORLD_PERMISSIONS_CACHE::addName);
        LECTERN_PLACE_PERMISSIONS = ProtectionHelper.getBlockPlacePrivileges(ConstantKeys.LECTERN);
        LECTERN_BREAK_PERMISSIONS = ProtectionHelper.getBlockBreakPrivileges(ConstantKeys.LECTERN);
    }

    @Override
    public Class<?> getAPIClass() {
        return RegionManagerService.class;
    }

    @Override
    public InteractionResult handleBlockPlace(SuperiorPlayer superiorPlayer, Block block) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(block, "block cannot be null");

        return handleBlockPlace(superiorPlayer, block, Keys.of(block), this::lookupIslandFunction);
    }

    public InteractionResult handleBlockPlace(SuperiorPlayer superiorPlayer, Block block, Key blockKey,
                                              Function<Location, Island> islandSupplier) {
        // We do not care about spawn island when spawn protection is disabled,
        // and therefore only island worlds are relevant.
        if (!plugin.getSettings().getSpawn().isProtected() && !plugin.getGrid().isIslandsWorld(block.getWorld())) {
            return InteractionResult.SUCCESS;
        }

        List<IslandPrivilege> islandPrivileges = ProtectionHelper.getBlockPlacePrivileges(blockKey);

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location blockLocation = block.getLocation(wrapper.getHandle());

            return handleInteractionInternal(superiorPlayer, blockLocation, islandPrivileges, 0,
                    true, true, islandSupplier, true, true);
        }
    }

    @Override
    public InteractionResult handleBlockBreak(SuperiorPlayer superiorPlayer, Block block) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(block, "block cannot be null");

        return handleBlockBreak(superiorPlayer, block, Keys.of(block), this::lookupIslandFunction);
    }

    public InteractionResult handleBlockBreak(SuperiorPlayer superiorPlayer, Block block, Key blockKey,
                                              Function<Location, Island> islandSupplier) {
        // We do not care about spawn island when spawn protection is disabled,
        // and therefore only island worlds are relevant.
        if (!plugin.getSettings().getSpawn().isProtected() && !plugin.getGrid().isIslandsWorld(block.getWorld())) {
            return InteractionResult.SUCCESS;
        }

        List<IslandPrivilege> islandPrivileges = ProtectionHelper.getBlockBreakPrivileges(blockKey);

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location blockLocation = block.getLocation(wrapper.getHandle());

            return handleInteractionInternal(superiorPlayer, blockLocation, islandPrivileges, 0,
                    true, true, islandSupplier, true, true);
        }
    }

    @Override
    public InteractionResult handleBlockInteract(SuperiorPlayer superiorPlayer, Block block, Action action,
                                                 @Nullable ItemStack usedItem) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(block, "block cannot be null");

        return handleBlockInteract(superiorPlayer, block, Keys.of(block), action, usedItem, this::lookupIslandFunction);
    }

    public InteractionResult handleBlockInteract(SuperiorPlayer superiorPlayer, Block block, Key blockKey,
                                                 Action action, @Nullable ItemStack usedItem,
                                                 Function<Location, Island> islandSupplier) {
        // We do not care about spawn island when spawn protection is disabled,
        // and therefore only island worlds are relevant.
        if (!plugin.getSettings().getSpawn().isProtected() && !plugin.getGrid().isIslandsWorld(block.getWorld())) {
            return InteractionResult.SUCCESS;
        }

        // Fast-check bypass mode of players
        if (superiorPlayer.hasBypassModeEnabled()) {
            return InteractionResult.SUCCESS;
        }

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location blockLocation = block.getLocation(wrapper.getHandle());

            boolean isInteractableItem = BukkitItems.isInteractableItem(usedItem, block);
            int stackedBlockAmount = plugin.getStackedBlocks().getStackedBlockAmount(blockLocation);

            if (!isInteractableItem && stackedBlockAmount <= 1 &&
                    plugin.getSettings().getBlockCategoriesMapping().getCategories(blockKey).isEmpty()) {
                return InteractionResult.SUCCESS;
            }

            Material blockType = block.getType();
            Material usedItemType = usedItem == null ? null : usedItem.getType();

            EntityType entityType = isInteractableItem ? BukkitItems.getEntityType(usedItem) : EntityType.UNKNOWN;

            if (entityType != EntityType.UNKNOWN) {
                List<IslandPrivilege> islandPrivileges = ProtectionHelper.getEntitySpawnPrivileges(Keys.of(entityType));
                return handleInteractionInternal(superiorPlayer, blockLocation, islandPrivileges, 0,
                        true, true, islandSupplier, false, false);
            }

            List<IslandPrivilege> islandPrivileges;
            if ((blockType == FARMLAND || blockType == TURTLE_EGG) && action != Action.PHYSICAL) {
                return InteractionResult.SUCCESS;
            } else if (blockType == VAULT && (usedItem == null || (usedItemType != TRIAL_KEY && usedItemType != OMINOUS_TRIAL_KEY))) {
                return InteractionResult.SUCCESS;
            } else if (blockType == Materials.GRASS_BLOCK.toBukkitType() || Materials.isDirt(blockType)) {
                if (usedItem != null && Materials.isHoe(usedItemType)) {
                    islandPrivileges = ProtectionHelper.getBlockPlacePrivileges(blockKey);
                } else {
                    return InteractionResult.SUCCESS;
                }
            } else if (stackedBlockAmount > 1) {
                islandPrivileges = ProtectionHelper.getBlockBreakPrivileges(blockKey);
            } else if (action == Action.RIGHT_CLICK_BLOCK && blockType == SWEET_BERRY_BUSH
                    && Materials.BONE_MEAL.toBukkitItem().isSimilar(usedItem)
                    && ((Ageable) plugin.getNMSWorld().getBlockData(block)).getAge() < 3) {
                islandPrivileges = Collections.singletonList(IslandPrivileges.FERTILIZE);
            } else if (action == Action.RIGHT_CLICK_BLOCK && (blockType == CAVE_VINES || blockType == CAVE_VINES_PLANT)
                    && Materials.BONE_MEAL.toBukkitItem().isSimilar(usedItem)
                    && !plugin.getNMSWorld().hasBerries(block)) {
                islandPrivileges = Collections.singletonList(IslandPrivileges.FERTILIZE);
            } else if (action == Action.RIGHT_CLICK_BLOCK && blockType == LECTERN && usedItem != null &&
                    plugin.getNMSAlgorithms().canPlaceBookIntoLectern(block, usedItem)) {
                islandPrivileges = LECTERN_PLACE_PERMISSIONS;
            } else {
                islandPrivileges = ProtectionHelper.getBlockInteractPrivileges(blockKey);
            }

            return handleInteractionInternal(superiorPlayer, blockLocation, islandPrivileges, 0,
                    true, true, islandSupplier, false, false);
        }
    }

    @Override
    public InteractionResult handleBlockFertilize(SuperiorPlayer superiorPlayer, Block block) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(block, "block cannot be null");

        return handleBlockFertilize(superiorPlayer, block, this::lookupIslandFunction);
    }

    public InteractionResult handleBlockFertilize(SuperiorPlayer superiorPlayer, Block block,
                                                  Function<Location, Island> islandSupplier) {
        return handleInteractionInternal(superiorPlayer, block, IslandPrivileges.FERTILIZE, islandSupplier);
    }

    @Override
    public InteractionResult handleEntityInteract(SuperiorPlayer superiorPlayer, Entity entity, @Nullable ItemStack usedItem) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(entity, "entity cannot be null");

        return handleEntityInteract(superiorPlayer, entity, usedItem, this::lookupIslandFunction);
    }

    public InteractionResult handleEntityInteract(SuperiorPlayer superiorPlayer, Entity entity, @Nullable ItemStack usedItem,
                                                  Function<Location, Island> islandSupplier) {
        // We do not care about spawn island when spawn protection is disabled,
        // and therefore only island worlds are relevant.
        if (!plugin.getSettings().getSpawn().isProtected() && !plugin.getGrid().isIslandsWorld(entity.getWorld())) {
            return InteractionResult.SUCCESS;
        }

        // Fast-check bypass mode of players
        if (superiorPlayer.hasBypassModeEnabled()) {
            return InteractionResult.SUCCESS;
        }

        InteractionResult interactionResult;
        boolean closeInventory = false;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location entityLocation = entity.getLocation(wrapper.getHandle());

            EntityType entityType = entity.getType();
            Material usedItemType = usedItem == null ? Material.AIR : usedItem.getType();

            if (entity instanceof Villager || BukkitEntities.isHorse(entity) || BukkitEntities.isNautilus(entityType)) {
                closeInventory = true;
            }

            IslandPrivilege islandPrivilege = null;
            if (usedItem != null && entity instanceof Animals && (usedItemType == GOLDEN_DANDELION
                    || plugin.getNMSEntities().isAnimalFood(usedItem, (Animals) entity))) {
                islandPrivilege = IslandPrivileges.ANIMAL_BREED;
            } else if (usedItemType == Material.NAME_TAG) {
                islandPrivilege = IslandPrivileges.NAME_ENTITY;
            } else if (usedItemType == Material.SADDLE || (entityType == LLAMA && Materials.isCarpet(usedItemType))
                    || (entityType == HAPPY_GHAST && Materials.isHarness(usedItemType))
                    || (usedItemType == Material.SHEARS && plugin.getNMSEntities().canShearSaddleFromEntity(entity))) {
                islandPrivilege = IslandPrivileges.SADDLE_ENTITY;
            } else if ((usedItemType == Material.FLINT_AND_STEEL || usedItemType == FIRE_CHARGE) && entity instanceof Creeper) {
                islandPrivilege = IslandPrivileges.IGNITE_CREEPER;
            } else if (usedItem != null && entity instanceof PoweredMinecart
                    && plugin.getNMSEntities().isMinecartFuel(usedItem, (PoweredMinecart) entity)) {
                islandPrivilege = IslandPrivileges.MINECART_OPEN;
            } else if (entity instanceof Sheep && Materials.isDye(usedItemType)) {
                islandPrivilege = IslandPrivileges.DYE_SHEEP;
            }

            List<IslandPrivilege> islandPrivileges;
            if (islandPrivilege != null) {
                islandPrivileges = Collections.singletonList(islandPrivilege);
            } else if (entityType == PARROT && usedItemType == Material.COOKIE) {
                islandPrivileges = ProtectionHelper.getEntityDamagePrivileges(entity);
            } else {
                islandPrivileges = ProtectionHelper.getEntityInteractPrivileges(entity);
            }

            interactionResult = handleInteractionInternal(superiorPlayer, entityLocation, islandPrivileges, 0,
                    true, true, islandSupplier, false, false);
        }

        if (closeInventory && interactionResult != InteractionResult.SUCCESS) {
            BukkitExecutor.sync(() -> {
                Player player = superiorPlayer.asPlayer();
                if (player != null && player.isOnline()) {
                    Inventory openInventory = player.getOpenInventory().getTopInventory();
                    if (openInventory != null && (openInventory.getType() == InventoryType.MERCHANT
                            || openInventory.getType() == InventoryType.CHEST)) {
                        player.closeInventory();
                    }
                }
            }, 1L);
        }

        return interactionResult;
    }

    @Override
    public InteractionResult handleEntityDamage(Entity damager, Entity entity) {
        Preconditions.checkNotNull(damager, "damager cannot be null");
        Preconditions.checkNotNull(entity, "entity cannot be null");

        return handleEntityDamage(damager, entity, this::lookupIslandFunction);
    }

    public InteractionResult handleEntityDamage(Entity damager, Entity entity, Function<Location, Island> islandSupplier) {
        // We do not care about spawn island when spawn protection is disabled,
        // and therefore only island worlds are relevant.
        if (!plugin.getSettings().getSpawn().isProtected() && !plugin.getGrid().isIslandsWorld(entity.getWorld())) {
            return InteractionResult.SUCCESS;
        }

        List<IslandPrivilege> islandPrivileges = ProtectionHelper.getEntityDamagePrivileges(entity);

        if (islandPrivileges.isEmpty()) {
            return InteractionResult.SUCCESS;
        }

        SuperiorPlayer superiorPlayer = BukkitEntities.getSuperiorPlayerSource(damager);

        if (superiorPlayer == null) {
            return InteractionResult.SUCCESS;
        }

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location entityLocation = entity.getLocation(wrapper.getHandle());

            InteractionResult interactionResult = handleInteractionInternal(superiorPlayer, entityLocation, islandPrivileges,
                    0, true, false, islandSupplier, false, true);

            if (interactionResult != InteractionResult.SUCCESS && damager instanceof Arrow && entity.getFireTicks() > 0) {
                entity.setFireTicks(0);
            }

            return interactionResult;
        }
    }

    @Override
    public InteractionResult handleEntityRide(SuperiorPlayer superiorPlayer, Entity vehicle) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(vehicle, "vehicle cannot be null");

        return handleEntityRide(superiorPlayer, vehicle, this::lookupIslandFunction);
    }

    public InteractionResult handleEntityRide(SuperiorPlayer superiorPlayer, Entity vehicle,
                                              Function<Location, Island> islandSupplier) {
        // We do not care about spawn island when spawn protection is disabled,
        // and therefore only island worlds are relevant.
        if (!plugin.getSettings().getSpawn().isProtected() && !plugin.getGrid().isIslandsWorld(vehicle.getWorld())) {
            return InteractionResult.SUCCESS;
        }

        // Fast-check bypass mode of players
        if (superiorPlayer.hasBypassModeEnabled()) {
            return InteractionResult.SUCCESS;
        }

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location entityLocation = vehicle.getLocation(wrapper.getHandle());

            // Both checks below are done on the same island.
            Island island = islandSupplier.apply(entityLocation);
            Function<Location, Island> cachedIslandSupplier = unused -> island;

            List<IslandPrivilege> islandPrivileges = ProtectionHelper.getEntityInteractPrivileges(vehicle);

            InteractionResult interactionResult = handleInteractionInternal(superiorPlayer, entityLocation, islandPrivileges,
                    0, true, false, cachedIslandSupplier, false, false);

            if (interactionResult != InteractionResult.SUCCESS) {
                return interactionResult;
            }

            IslandPrivilege islandPrivilege = vehicle instanceof Animals ? IslandPrivileges.ENTITY_RIDE
                    : IslandPrivileges.MINECART_ENTER;
            return handleInteractionInternal(superiorPlayer, entityLocation, Collections.singletonList(islandPrivilege),
                    0, true, false, cachedIslandSupplier, false, false);
        }
    }

    @Override
    public InteractionResult handleEntityShear(SuperiorPlayer superiorPlayer, Entity entity) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(entity, "entity cannot be null");

        return handleEntityShear(superiorPlayer, entity, this::lookupIslandFunction);
    }

    public InteractionResult handleEntityShear(SuperiorPlayer superiorPlayer, Entity entity, Function<Location, Island> islandSupplier) {
        return handleInteractionInternal(superiorPlayer, entity, IslandPrivileges.ANIMAL_SHEAR, islandSupplier);
    }

    @Override
    public InteractionResult handleEntityLeash(SuperiorPlayer superiorPlayer, Entity entity) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(entity, "entity cannot be null");

        return handleEntityLeash(superiorPlayer, entity, this::lookupIslandFunction);
    }

    public InteractionResult handleEntityLeash(SuperiorPlayer superiorPlayer, Entity entity, Function<Location, Island> islandSupplier) {
        return handleInteractionInternal(superiorPlayer, entity, IslandPrivileges.LEASH, islandSupplier);
    }

    @Override
    public InteractionResult handlePlayerPickupItem(SuperiorPlayer superiorPlayer, Item item) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(item, "item cannot be null");

        return handlePlayerPickupItem(superiorPlayer, item, this::lookupIslandFunction);
    }

    public InteractionResult handlePlayerPickupItem(SuperiorPlayer superiorPlayer, Item item, Function<Location, Island> islandSupplier) {
        if (plugin.getNMSPlayers().wasThrownByPlayer(item, superiorPlayer)) {
            return InteractionResult.SUCCESS;
        }

        return handleInteractionInternal(superiorPlayer, item, IslandPrivileges.PICKUP_DROPS, MAX_PICKUP_DISTANCE, islandSupplier);
    }

    @Override
    public InteractionResult handlePlayerDropItem(SuperiorPlayer superiorPlayer, Item item) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(item, "item cannot be null");

        return handlePlayerDropItem(superiorPlayer, item, this::lookupIslandFunction);
    }

    public InteractionResult handlePlayerDropItem(SuperiorPlayer superiorPlayer, Item item, Function<Location, Island> islandSupplier) {
        return handleInteractionInternal(superiorPlayer, item, IslandPrivileges.DROP_ITEMS, islandSupplier);
    }

    @Override
    public InteractionResult handlePlayerEnderPearl(SuperiorPlayer superiorPlayer, Location destination) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(destination, "destination cannot be null");
        Preconditions.checkArgument(destination.getWorld() != null, "destination's world cannot be null");

        return handleInteractionInternal(superiorPlayer, destination, IslandPrivileges.ENDER_PEARL, this::lookupIslandFunction);
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
        if (!plugin.getSettings().getSpawn().isProtected() && !plugin.getGrid().isIslandsWorld(location.getWorld())) {
            return InteractionResult.SUCCESS;
        }

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
        if (!plugin.getSettings().getSpawn().isProtected() && !plugin.getGrid().isIslandsWorld(location.getWorld())) {
            return InteractionResult.SUCCESS;
        }

        return handleInteractionInternal(superiorPlayer, location, IslandPrivileges.WIND_CHARGE,
                0, true, true);
    }

    @Override
    public InteractionResult handlePlayerTakeLecternBook(SuperiorPlayer superiorPlayer, Location location) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(location, "location cannot be null");
        Preconditions.checkArgument(location.getWorld() != null, "location's world cannot be null");

        if (LECTERN_BREAK_PERMISSIONS.isEmpty()) {
            // No break permissions, fast fail
            return InteractionResult.SUCCESS;
        }

        // We do not care about spawn island when spawn protection is disabled,
        // and therefore only island worlds are relevant.
        if (!plugin.getSettings().getSpawn().isProtected() && !plugin.getGrid().isIslandsWorld(location.getWorld())) {
            return InteractionResult.SUCCESS;
        }

        return handleInteractionInternal(superiorPlayer, location, LECTERN_BREAK_PERMISSIONS,
                0, true, true);
    }

    @Override
    public InteractionResult handleCustomInteraction(SuperiorPlayer superiorPlayer, Location location,
                                                     IslandPrivilege islandPrivilege) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(location, "location cannot be null");
        Preconditions.checkNotNull(islandPrivilege, "islandPrivilege cannot be null");

        return handleCustomInteraction(superiorPlayer, location, islandPrivilege, this::lookupIslandFunction);
    }

    public InteractionResult handleCustomInteraction(SuperiorPlayer superiorPlayer, Location location,
                                                     IslandPrivilege islandPrivilege,
                                                     Function<Location, Island> islandSupplier) {
        return handleCustomInteraction(superiorPlayer, location, Collections.singletonList(islandPrivilege), islandSupplier);
    }

    @Override
    public InteractionResult handleCustomInteraction(SuperiorPlayer superiorPlayer, Location location,
                                                     List<IslandPrivilege> islandPrivileges) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(location, "location cannot be null");
        Preconditions.checkNotNull(islandPrivileges, "islandPrivileges cannot be null");

        return handleCustomInteraction(superiorPlayer, location, islandPrivileges, this::lookupIslandFunction);
    }

    public InteractionResult handleCustomInteraction(SuperiorPlayer superiorPlayer, Location location,
                                                     List<IslandPrivilege> islandPrivileges,
                                                     Function<Location, Island> islandSupplier) {
        return handleInteractionInternal(superiorPlayer, location, islandPrivileges, islandSupplier);
    }

    private InteractionResult handleInteractionInternal(SuperiorPlayer superiorPlayer, Entity entity,
                                                        IslandPrivilege islandPrivilege,
                                                        Function<Location, Island> islandSupplier) {
        return handleInteractionInternal(superiorPlayer, entity, islandPrivilege, 0, islandSupplier);
    }

    private InteractionResult handleInteractionInternal(SuperiorPlayer superiorPlayer, Entity entity,
                                                        IslandPrivilege islandPrivilege, int extraRadius,
                                                        Function<Location, Island> islandSupplier) {
        // We do not care about spawn island when spawn protection is disabled,
        // and therefore only island worlds are relevant.
        if (!plugin.getSettings().getSpawn().isProtected() && !plugin.getGrid().isIslandsWorld(entity.getWorld())) {
            return InteractionResult.SUCCESS;
        }

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location entityLocation = entity.getLocation(wrapper.getHandle());
            return handleInteractionInternal(superiorPlayer, entityLocation, Collections.singletonList(islandPrivilege),
                    extraRadius, true, false, islandSupplier, false, true);
        }
    }

    private InteractionResult handleInteractionInternal(SuperiorPlayer superiorPlayer, Block block,
                                                        IslandPrivilege islandPrivilege, Function<Location, Island> islandSupplier) {
        // We do not care about spawn island when spawn protection is disabled,
        // and therefore only island worlds are relevant.
        if (!plugin.getSettings().getSpawn().isProtected() && !plugin.getGrid().isIslandsWorld(block.getWorld())) {
            return InteractionResult.SUCCESS;
        }

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location blockLocation = block.getLocation(wrapper.getHandle());
            return handleInteractionInternal(superiorPlayer, blockLocation, Collections.singletonList(islandPrivilege),
                    0, true, true, islandSupplier, false, true);
        }
    }

    private InteractionResult handleInteractionInternal(SuperiorPlayer superiorPlayer, Location location,
                                                        IslandPrivilege islandPrivilege,
                                                        Function<Location, Island> islandSupplier) {
        return handleInteractionInternal(superiorPlayer, location, Collections.singletonList(islandPrivilege), islandSupplier);
    }

    private InteractionResult handleInteractionInternal(SuperiorPlayer superiorPlayer, Location location,
                                                        List<IslandPrivilege> islandPrivileges,
                                                        Function<Location, Island> islandSupplier) {
        // We do not care about spawn island when spawn protection is disabled,
        // and therefore only island worlds are relevant.
        if (!plugin.getSettings().getSpawn().isProtected() && !plugin.getGrid().isIslandsWorld(location.getWorld())) {
            return InteractionResult.SUCCESS;
        }

        return handleInteractionInternal(superiorPlayer, location, islandPrivileges,
                0, true, false, islandSupplier, false, true);
    }

    @SuppressWarnings("SameParameterValue")
    private InteractionResult handleInteractionInternal(SuperiorPlayer superiorPlayer, Location location,
                                                        IslandPrivilege islandPrivilege, int extraRadius,
                                                        boolean checkIslandBoundaries, boolean checkRecalculation) {
        return handleInteractionInternal(superiorPlayer, location, Collections.singletonList(islandPrivilege),
                extraRadius, checkIslandBoundaries, checkRecalculation);
    }

    @SuppressWarnings("SameParameterValue")
    private InteractionResult handleInteractionInternal(SuperiorPlayer superiorPlayer, Location location,
                                                        List<IslandPrivilege> islandPrivileges, int extraRadius,
                                                        boolean checkIslandBoundaries, boolean checkRecalculation) {
        return handleInteractionInternal(superiorPlayer, location, islandPrivileges, extraRadius, checkIslandBoundaries,
                checkRecalculation, this::lookupIslandFunction, false, true);
    }

    private InteractionResult handleInteractionInternal(SuperiorPlayer superiorPlayer, Location location,
                                                        List<IslandPrivilege> islandPrivileges, int extraRadius,
                                                        boolean checkIslandBoundaries, boolean checkRecalculation,
                                                        Function<Location, Island> islandSupplier,
                                                        boolean checkBoundariesWithoutPrivileges,
                                                        boolean callBypassModeCheck) {
        if (callBypassModeCheck && superiorPlayer.hasBypassModeEnabled()) {
            return InteractionResult.SUCCESS;
        }

        Island island = islandSupplier.apply(location);

        if (checkIslandBoundaries) {
            boolean checkBoundaries = checkBoundariesWithoutPrivileges && islandPrivileges.isEmpty();

            for (IslandPrivilege islandPrivilege : islandPrivileges) {
                if (!WORLD_PERMISSIONS_CACHE.contains(islandPrivilege)) {
                    checkBoundaries = true;
                    break;
                }
            }

            if (checkBoundaries) {
                if (island == null && plugin.getGrid().isIslandsWorld(location.getWorld())) {
                    return InteractionResult.OUTSIDE_ISLAND;
                }

                if (island != null && !isInsideRange(island, location, extraRadius)) {
                    return InteractionResult.OUTSIDE_ISLAND;
                }
            }
        }

        if (island != null) {
            // Each privilege check looks up the Bukkit player, so the lookup is cached while checking several privileges.
            long cacheStamp = islandPrivileges.size() > 1 ? SSuperiorPlayer.CACHED_PLAYER_RESULT.startCache() : -1;
            try {
                for (IslandPrivilege islandPrivilege : islandPrivileges) {
                    if (!island.hasPermission(superiorPlayer, islandPrivilege)) {
                        return InteractionResult.MISSING_PRIVILEGE;
                    }
                }
            } finally {
                SSuperiorPlayer.CACHED_PLAYER_RESULT.endCache(cacheStamp);
            }

            if (checkRecalculation && island.isBeingRecalculated()) {
                return InteractionResult.ISLAND_RECALCULATE;
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public MoveResult handlePlayerMove(SuperiorPlayer superiorPlayer, Location from, Location to) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(from, "from cannot be null");
        Preconditions.checkNotNull(to, "to cannot be null");

        return handlePlayerMove(superiorPlayer, from, this::lookupIslandFunction, to, this::lookupIslandFunction);
    }

    public MoveResult handlePlayerMove(SuperiorPlayer superiorPlayer,
                                       Location from, Function<Location, Island> fromIslandSupplier,
                                       Location to, Function<Location, Island> toIslandSupplier) {
        Computed<Island> computedFromIsland = new Computed<>();

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

        if (from.getBlockX() != to.getBlockX() || from.getBlockZ() != to.getBlockZ() ||
                superiorPlayer.asPlayer().getFallDistance() > 0) {
            // Handle moving while in teleport warmup.
            BukkitTask teleportTask = superiorPlayer.getTeleportTask();
            if (teleportTask != null) {
                teleportTask.cancel();
                superiorPlayer.setTeleportTask(null);
                Message.TELEPORT_WARMUP_CANCEL.send(superiorPlayer);
            }

            Island toIsland = toIslandSupplier.apply(to);

            MoveResult moveResult;

            if (toIsland != null) {
                moveResult = handlePlayerEnterIslandInternal(superiorPlayer, to, toIslandSupplier,
                        from, l -> computedFromIsland.tryCompute(l, fromIslandSupplier), IslandEnterEvent.EnterCause.PLAYER_MOVE);
                if (moveResult != MoveResult.SUCCESS)
                    return moveResult;
            }

            Island fromIsland = computedFromIsland.tryCompute(from, fromIslandSupplier);
            if (fromIsland != null) {
                moveResult = handlePlayerLeaveIslandInternal(superiorPlayer, from, fromIsland, to, toIsland,
                        IslandLeaveEvent.LeaveCause.PLAYER_MOVE);
                if (moveResult != MoveResult.SUCCESS)
                    return moveResult;
            }
        }

        if (from.getBlockY() != to.getBlockY() && to.getBlockY() <= plugin.getNMSWorld().getMinHeight(to.getWorld()) - 5) {
            Island fromIsland = computedFromIsland.tryCompute(from, fromIslandSupplier);

            if (fromIsland == null || (fromIsland.isVisitor(superiorPlayer, false) ?
                    !plugin.getSettings().getVoidTeleport().isVisitors() : !plugin.getSettings().getVoidTeleport().isMembers()))
                return MoveResult.SUCCESS;

            Log.debug(Debug.VOID_TELEPORT, superiorPlayer.getName());

            superiorPlayer.setPlayerStatus(PlayerStatus.VOID_TELEPORT);

            superiorPlayer.teleportWithResult(fromIsland, result -> {
                if (result == PlayerTeleportAlgorithm.TeleportResult.SUCCESS) {
                    forgetVoidTeleportPlayerStatus(superiorPlayer);
                } else {
                    superiorPlayer.teleportWithResult(plugin.getGrid().getSpawnIsland(), unused -> {
                        forgetVoidTeleportPlayerStatus(superiorPlayer);
                    });
                    if (result != PlayerTeleportAlgorithm.TeleportResult.CUSTOM)
                        Message.TELEPORTED_FAILED.send(superiorPlayer);
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

        return handlePlayerTeleport(superiorPlayer, from, this::lookupIslandFunction, to, this::lookupIslandFunction);
    }

    public MoveResult handlePlayerTeleport(SuperiorPlayer superiorPlayer,
                                           Location from, Function<Location, Island> fromIslandSupplier,
                                           Location to, Function<Location, Island> toIslandSupplier) {
        Computed<Island> computedFromIsland = new Computed<>();

        Island toIsland = toIslandSupplier.apply(to);
        if (toIsland != null) {
            MoveResult enterMove = handlePlayerEnterIslandInternal(superiorPlayer, to, l -> toIsland,
                    from, l -> computedFromIsland.tryCompute(l, fromIslandSupplier), IslandEnterEvent.EnterCause.PLAYER_TELEPORT);
            if (enterMove != MoveResult.SUCCESS)
                return enterMove;
        }

        Island fromIsland = computedFromIsland.tryCompute(from, fromIslandSupplier);
        if (fromIsland != null) {
            return handlePlayerLeaveIslandInternal(superiorPlayer, from, fromIsland, to, toIsland,
                    IslandLeaveEvent.LeaveCause.PLAYER_TELEPORT);
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
        if (island == null)
            return MoveResult.SUCCESS;

        return handlePlayerEnterIslandInternal(superiorPlayer, teleportLocation, l -> island,
                portalLocation, this::lookupIslandFunction, IslandEnterEvent.EnterCause.PORTAL);
    }

    @Override
    public MoveResult handlePlayerJoin(SuperiorPlayer superiorPlayer, Location location) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");
        Preconditions.checkNotNull(location, "location cannot be null");
        Preconditions.checkArgument(location.getWorld() != null, "location's world cannot be null");

        Island island = plugin.getGrid().getIslandAt(location);
        if (island == null)
            return MoveResult.SUCCESS;

        return handlePlayerEnterIslandInternal(superiorPlayer, location, l -> island,
                null, null, IslandEnterEvent.EnterCause.PLAYER_JOIN);
    }

    @Override
    public MoveResult handlePlayerQuit(SuperiorPlayer superiorPlayer, Location location) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer cannot be null");

        Island island = plugin.getGrid().getIslandAt(location);
        if (island == null)
            return MoveResult.SUCCESS;

        island.setPlayerInside(superiorPlayer, false);

        return handlePlayerLeaveIslandInternal(superiorPlayer, location, island,
                null, null, IslandLeaveEvent.LeaveCause.PLAYER_QUIT);
    }

    private MoveResult handlePlayerEnterIslandInternal(SuperiorPlayer superiorPlayer,
                                                       Location to, Function<Location, Island> toIslandSupplier,
                                                       @Nullable Location from, Function<Location, Island> fromIslandSupplier,
                                                       IslandEnterEvent.EnterCause enterCause) {
        // This can happen after the leave event is cancelled.
        if (superiorPlayer.hasPlayerStatus(PlayerStatus.LEAVING_ISLAND)) {
            superiorPlayer.removePlayerStatus(PlayerStatus.LEAVING_ISLAND);
            return MoveResult.SUCCESS;
        }

        Island toIsland = toIslandSupplier.apply(to);

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

        Island fromIsland = from == null ? null : fromIslandSupplier.apply(from);

        boolean equalIslands = toIsland.equals(fromIsland);
        boolean equalWorlds = from != null && to.getWorld().equals(from.getWorld());
        boolean toInsideRange = isInsideRange(toIsland, to, 0);
        boolean fromInsideRange = fromIsland != null && isInsideRange(fromIsland, from, 0);

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
                this.dragonBattleService.get().restoreKilledEnderDragonBattle(toIsland, to.getWorld());
            }

            return MoveResult.SUCCESS;
        }

        if (!PluginEventsFactory.callIslandEnterEvent(toIsland, superiorPlayer, enterCause)) {
            PluginEventsFactory.callIslandRestrictMoveEvent(toIsland, superiorPlayer, IslandRestrictMoveEvent.RestrictReason.ENTER_EVENT_CANCELLED);
            return MoveResult.ENTER_EVENT_CANCELLED;
        }

        toIsland.setPlayerInside(superiorPlayer, true);

        handleIslandEnterEffects(superiorPlayer, toIsland, to);

        return MoveResult.SUCCESS;
    }

    private void handleIslandEnterEffects(SuperiorPlayer superiorPlayer, Island toIsland, Location to) {
        this.dragonBattleService.get().restoreKilledEnderDragonBattle(toIsland, to.getWorld());

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
            BukkitExecutor.sync(() -> updatePlayerTimeAndWeather(player, toIsland), 1L);
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
    }

    private static void updatePlayerTimeAndWeather(Player player, Island toIsland) {
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
    }

    private MoveResult handlePlayerLeaveIslandInternal(SuperiorPlayer superiorPlayer,
                                                       Location from, Island fromIsland,
                                                       @Nullable Location to, @Nullable Island toIsland,
                                                       IslandLeaveEvent.LeaveCause leaveCause) {
        boolean equalWorlds = to != null && from.getWorld().equals(to.getWorld());
        boolean equalIslands = fromIsland.equals(toIsland);
        boolean toInsideRange = toIsland != null && isInsideRange(toIsland, to, 0);
        boolean fromInsideRange = isInsideRange(fromIsland, from, 0);

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

    private Island lookupIslandFunction(Location location) {
        return plugin.getGrid().getIslandAt(location);
    }

    private static boolean isInsideRange(Island island, Location location, int extraRadius) {
        try (MutableBlockPosition blockPosition = MutableBlockPosition.POOL.obtain()) {
            return island.isInsideRange(blockPosition.set(location), extraRadius);
        }
    }

}

package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.service.region.InteractionResult;
import com.bgsoftware.superiorskyblock.api.service.region.RegionManagerService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.nms.ICachedBlock;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import com.bgsoftware.superiorskyblock.service.region.ProtectionHelper;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Iterator;

public class ProtectionListener extends AbstractGameEventListener {

    @Nullable
    private static final Material CHORUS_FLOWER = EnumHelper.getEnum(Material.class, "CHORUS_FLOWER");
    @Nullable
    private static final Material CHORUS_FRUIT = EnumHelper.getEnum(Material.class, "CHORUS_FRUIT");
    @Nullable
    private static final Material BRUSH = EnumHelper.getEnum(Material.class, "BRUSH");
    @Nullable
    private static final EntityType WIND_CHARGE = EnumHelper.getEnum(EntityType.class, "WIND_CHARGE");
    @Nullable
    private static final Material POINTED_DRIPSTONE = EnumHelper.getEnum(Material.class, "POINTED_DRIPSTONE");
    @Nullable
    private static final EntityType TRIDENT = EnumHelper.getEnum(EntityType.class, "TRIDENT");

    private final LazyReference<RegionManagerService> protectionManager = new LazyReference<RegionManagerService>() {
        @Override
        protected RegionManagerService create() {
            return plugin.getServices().getService(RegionManagerService.class);
        }
    };

    public ProtectionListener(SuperiorSkyblockPlugin plugin) {
        super(plugin);
        registerListeners();
    }

    /* BLOCK INTERACTS */

    private void onBlockPlace(GameEvent<GameEventArgs.BlockPlaceEvent> e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getArgs().player);
        InteractionResult interactionResult = this.protectionManager.get().handleBlockPlace(superiorPlayer, e.getArgs().block);
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled();
    }

    private void onPlayerInteract(GameEvent<GameEventArgs.PlayerInteractEvent> e) {
        if (handleBlockFertilize(e)) return;
        if (handleBedPlace(e)) return;
        if (handleSignColorChange(e)) return;
        if (handleBrushUse(e)) return;
        if (handleMinecartPlace(e)) return;
        if (handleEntityInteract(e)) return;
        handleBlockInteract(e);
    }

    private boolean handleBlockFertilize(GameEvent<GameEventArgs.PlayerInteractEvent> e) {
        Action action = e.getArgs().action;
        ItemStack usedItem = e.getArgs().usedItem;

        if (action != Action.RIGHT_CLICK_BLOCK || usedItem == null || !Materials.BONE_MEAL.toBukkitItem().isSimilar(usedItem))
            return false;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getArgs().player);
        InteractionResult interactionResult = this.protectionManager.get().handleBlockFertilize(superiorPlayer, e.getArgs().clickedBlock);
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true)) {
            e.setCancelled();
            return true;
        }

        return false;
    }

    // Patching a dupe glitch with crops and beds: https://github.com/BG-Software-LLC/SuperiorSkyblock2/issues/1672
    private boolean handleBedPlace(GameEvent<GameEventArgs.PlayerInteractEvent> e) {
        Action action = e.getArgs().action;
        ItemStack usedItem = e.getArgs().usedItem;

        if (action != Action.RIGHT_CLICK_BLOCK || usedItem == null || !Materials.isBed(usedItem.getType()))
            return false;

        // The player right-clicked a block with a bed in his hand
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getArgs().player);
        InteractionResult interactionResult = this.protectionManager.get().handleBlockPlace(superiorPlayer, e.getArgs().clickedBlock);
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true)) {
            e.setCancelled();
            return true;
        }

        return false;
    }

    private boolean handleSignColorChange(GameEvent<GameEventArgs.PlayerInteractEvent> e) {
        Action action = e.getArgs().action;
        ItemStack usedItem = e.getArgs().usedItem;

        if (action != Action.RIGHT_CLICK_BLOCK || usedItem == null || ServerVersion.isLegacy() || !Materials.isDye(usedItem.getType()))
            return false;

        Block clickedBlock = e.getArgs().clickedBlock;
        BlockState blockState = clickedBlock.getState();

        if (!(blockState instanceof Sign))
            return false;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getArgs().player);
        InteractionResult interactionResult = this.protectionManager.get().handleBlockPlace(superiorPlayer, clickedBlock);
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true)) {
            e.setCancelled();
            return true;
        }

        return false;
    }

    private boolean handleBrushUse(GameEvent<GameEventArgs.PlayerInteractEvent> e) {
        Action action = e.getArgs().action;
        ItemStack usedItem = e.getArgs().usedItem;

        if (action != Action.RIGHT_CLICK_BLOCK || usedItem == null || usedItem.getType() != BRUSH)
            return false;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getArgs().player);

        InteractionResult interactionResult;
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            interactionResult = this.protectionManager.get().handleCustomInteraction(
                    superiorPlayer, e.getArgs().clickedBlock.getLocation(wrapper.getHandle()), IslandPrivileges.BRUSH);
        }
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true)) {
            e.setCancelled();
            return true;
        }

        return false;
    }

    private boolean handleMinecartPlace(GameEvent<GameEventArgs.PlayerInteractEvent> e) {
        Action action = e.getArgs().action;
        ItemStack usedItem = e.getArgs().usedItem;

        if (action != Action.RIGHT_CLICK_BLOCK || usedItem == null)
            return false;

        Material handItemType = usedItem.getType();
        Material clickedBlockType = e.getArgs().clickedBlock.getType();
        if (!Materials.isBoat(handItemType) && (!Materials.isMinecart(handItemType) || !Materials.isRail(clickedBlockType)))
            return false;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getArgs().player);

        InteractionResult interactionResult;
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            interactionResult = this.protectionManager.get().handleCustomInteraction(superiorPlayer,
                    e.getArgs().clickedBlock.getLocation(wrapper.getHandle()), IslandPrivileges.MINECART_PLACE);
        }
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true)) {
            e.setCancelled();
            return true;
        }

        return false;
    }

    private boolean handleEntityInteract(GameEvent<GameEventArgs.PlayerInteractEvent> e) {
        if (e.getArgs().clickedEntity == null)
            return false;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getArgs().player);
        InteractionResult interactionResult = this.protectionManager.get().handleEntityInteract(superiorPlayer,
                e.getArgs().clickedEntity, e.getArgs().usedItem);
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true)) {
            e.setCancelled();
            return true;
        }

        return false;
    }

    private void handleBlockInteract(GameEvent<GameEventArgs.PlayerInteractEvent> e) {
        if (e.getArgs().clickedBlock == null)
            return;

        ItemStack usedItem = e.getArgs().usedItem;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getArgs().player);
        InteractionResult interactionResult = this.protectionManager.get().handleBlockInteract(superiorPlayer,
                e.getArgs().clickedBlock, e.getArgs().action, usedItem);
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled();
    }

    private void onBlockBreak(GameEvent<GameEventArgs.BlockBreakEvent> e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getArgs().player);
        InteractionResult interactionResult = this.protectionManager.get().handleBlockBreak(superiorPlayer, e.getArgs().block);
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled();
    }

    private void onFrostWalker(GameEvent<GameEventArgs.EntityBlockFormEvent> e) {
        Entity entity = e.getArgs().entity;
        if (!(entity instanceof Player))
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer((Player) entity);
        InteractionResult interactionResult = this.protectionManager.get().handleBlockPlace(superiorPlayer, e.getArgs().block);
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, false))
            e.setCancelled();
    }

    private void onBucketEmpty(GameEvent<GameEventArgs.PlayerEmptyBucketEvent> e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getArgs().player);
        InteractionResult interactionResult = this.protectionManager.get().handleBlockPlace(superiorPlayer, e.getArgs().clickedBlock);
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled();
    }

    private void onBucketFill(GameEvent<GameEventArgs.PlayerFillBucketEvent> e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getArgs().player);
        InteractionResult interactionResult = this.protectionManager.get().handleBlockPlace(superiorPlayer, e.getArgs().clickedBlock);
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled();
    }

    private void onBucketDispense(GameEvent<GameEventArgs.BlockDispenseEvent> e) {
        Material itemType = e.getArgs().dispensedItem.getType();

        if (!(itemType == Material.BUCKET || itemType == Material.WATER_BUCKET || itemType == Material.LAVA_BUCKET))
            return;

        Vector velocity = e.getArgs().velocity;

        Location dispenseBlockLocation = new Location(e.getArgs().block.getWorld(), velocity.getBlockX(),
                velocity.getBlockY(), velocity.getBlockZ());

        Island island = plugin.getGrid().getIslandAt(dispenseBlockLocation);

        if (island != null && !island.isInsideRange(dispenseBlockLocation))
            e.setCancelled();
    }

    private void onChorusFruitConsume(GameEvent<GameEventArgs.PlayerItemConsumeEvent> e) {
        if (CHORUS_FRUIT == null || e.getArgs().consumedItem.getType() != CHORUS_FRUIT)
            return;

        Player player = e.getArgs().player;
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);

        InteractionResult interactionResult;
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            interactionResult = this.protectionManager.get().handlePlayerConsumeChorusFruit(
                    superiorPlayer, player.getLocation(wrapper.getHandle()));
        }
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled();
    }

    /* ENTITY INTERACTS */

    private void onEntityAttack(GameEvent<GameEventArgs.EntityDamageEvent> e) {
        Entity entity = e.getArgs().entity;
        Entity damager = e.getArgs().damager;

        if (damager == null || entity instanceof Player)
            return;

        SuperiorPlayer damagerSource = BukkitEntities.getPlayerSource(damager)
                .map(plugin.getPlayers()::getSuperiorPlayer).orElse(null);
        InteractionResult interactionResult = this.protectionManager.get().handleEntityDamage(damager, entity);
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, damagerSource, true))
            e.setCancelled();
    }

    private void onEntityShearing(GameEvent<GameEventArgs.PlayerShearEntityEvent> e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getArgs().player);
        InteractionResult interactionResult = this.protectionManager.get().handleEntityShear(superiorPlayer, e.getArgs().entity);
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled();
    }

    private void onHangingBreak(GameEvent<GameEventArgs.HangingBreakEvent> e) {
        BukkitEntities.getPlayerSource(e.getArgs().remover).map(plugin.getPlayers()::getSuperiorPlayer).ifPresent(removerPlayer -> {
            InteractionResult interactionResult = this.protectionManager.get().handleEntityInteract(removerPlayer, e.getArgs().entity, null);
            if (ProtectionHelper.shouldPreventInteraction(interactionResult, removerPlayer, true))
                e.setCancelled();
        });
    }

    private void onHangingPlace(GameEvent<GameEventArgs.HangingPlaceEvent> e) {
        if (!(e.getArgs().entity instanceof Hanging))
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getArgs().player);
        InteractionResult interactionResult = this.protectionManager.get().handleEntityInteract(superiorPlayer, e.getArgs().entity, null);
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled();
    }

    private void onEntityTarget(GameEvent<GameEventArgs.EntityTargetEvent> e) {
        Entity entity = e.getArgs().entity;
        Entity target = e.getArgs().target;

        if (!(target instanceof Player))
            return;

        InteractionResult interactionResult = this.protectionManager.get().handleEntityDamage(target, entity);
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, null, false))
            e.setCancelled();
    }

    private void onVillagerTrade(GameEvent<GameEventArgs.InventoryClickEvent> e) {
        Inventory openInventory = e.getArgs().bukkitEvent.getView().getTopInventory();

        if (openInventory == null || openInventory.getType() != InventoryType.MERCHANT)
            return;

        HumanEntity humanEntity = e.getArgs().bukkitEvent.getWhoClicked();
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(humanEntity);

        InteractionResult interactionResult;
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location location = humanEntity.getLocation(wrapper.getHandle());
            interactionResult = this.protectionManager.get().handleCustomInteraction(superiorPlayer,
                    location, IslandPrivileges.VILLAGER_TRADING);
        }
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, null, false)) {
            e.setCancelled();
            humanEntity.closeInventory();
        }
    }

    private void onPlayerLeash(GameEvent<GameEventArgs.PlayerLeashEntityEvent> e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getArgs().player);
        InteractionResult interactionResult = this.protectionManager.get().handleEntityLeash(superiorPlayer, e.getArgs().entity);
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled();
    }

    private void onPlayerUnleash(GameEvent<GameEventArgs.PlayerUnleashEntityEvent> e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getArgs().player);
        InteractionResult interactionResult = this.protectionManager.get().handleEntityLeash(superiorPlayer, e.getArgs().entity);
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled();
    }

    /* VEHICLE INTERACTS */

    private void onVehicleEnter(GameEvent<GameEventArgs.EntityRideEvent> e) {
        Entity rider = e.getArgs().entity;
        if (!(rider instanceof Player))
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer((Player) rider);
        InteractionResult interactionResult = this.protectionManager.get().handleEntityRide(superiorPlayer, e.getArgs().vehicle);
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled();
    }

    private void onVehicleOpen(GameEvent<GameEventArgs.InventoryOpenEvent> e) {
        InventoryHolder inventoryHolder = e.getArgs().bukkitEvent.getInventory().getHolder();

        if (!(inventoryHolder instanceof Vehicle))
            return;

        IslandPrivilege islandPrivilege = BukkitEntities.isHorse((Vehicle) inventoryHolder) ? IslandPrivileges.HORSE_INTERACT :
                inventoryHolder instanceof Animals ? IslandPrivileges.ENTITY_RIDE : IslandPrivileges.MINECART_OPEN;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getArgs().bukkitEvent.getPlayer());
        InteractionResult interactionResult;
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location minecartLocation = ((Vehicle) inventoryHolder).getLocation(wrapper.getHandle());
            interactionResult = this.protectionManager.get().handleCustomInteraction(superiorPlayer,
                    minecartLocation, islandPrivilege);
        }
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled();
    }

    private void onPlayerCollideWithEntity(GameEvent<GameEventArgs.EntityCollisionEvent> e) {
        Entity entity = e.getArgs().entity;
        if (!(entity instanceof Player))
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer((Player) entity);
        InteractionResult interactionResult;
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location vehicleLocation = e.getArgs().target.getLocation(wrapper.getHandle());
            interactionResult = this.protectionManager.get().handleCustomInteraction(superiorPlayer,
                    vehicleLocation, IslandPrivileges.MINECART_ENTER);
        }
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, false))
            e.setCancelled();
    }

    /* ITEMS */

    private void onPlayerDropItem(GameEvent<GameEventArgs.PlayerDropItemEvent> e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getArgs().player);
        InteractionResult interactionResult = this.protectionManager.get().handlePlayerDropItem(superiorPlayer, e.getArgs().droppedItem);
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled();
    }

    private void onPlayerPickupItem(GameEvent<GameEventArgs.PlayerPickupItemEvent> e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getArgs().player);
        InteractionResult interactionResult = this.protectionManager.get().handlePlayerPickupItem(superiorPlayer, e.getArgs().pickedUpItem);
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled();
    }

    private void onPlayerPickupArrow(GameEvent<GameEventArgs.PlayerPickupArrowEvent> e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getArgs().player);
        InteractionResult interactionResult = protectionManager.get().handlePlayerPickupItem(superiorPlayer, e.getArgs().pickedUpItem);
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled();
    }

    /* PROJECTILE INTERACTS */

    private void onPlayerLaunchProjectile(GameEvent<GameEventArgs.ProjectileLaunchEvent> e) {
        Entity entity = e.getArgs().entity;
        BukkitEntities.getPlayerSource(entity).map(plugin.getPlayers()::getSuperiorPlayer).ifPresent(launcherPlayer -> {
            EntityType entityType = entity.getType();

            IslandPrivilege islandPrivilege;
            if (entity instanceof FishHook && !ServerVersion.isLegacy()) {
                islandPrivilege = IslandPrivileges.FISH;
            } else if (entityType == TRIDENT) {
                islandPrivilege = IslandPrivileges.PICKUP_DROPS;
            } else if (entity instanceof Egg) {
                islandPrivilege = IslandPrivileges.ANIMAL_SPAWN;
            } else if (entity instanceof EnderPearl) {
                islandPrivilege = IslandPrivileges.ENDER_PEARL;
            } else if (entityType == WIND_CHARGE) {
                islandPrivilege = IslandPrivileges.WIND_CHARGE;
            } else {
                return;
            }

            InteractionResult interactionResult;
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                Location entityLocation = entity.getLocation(wrapper.getHandle());
                interactionResult = this.protectionManager.get().handleCustomInteraction(launcherPlayer,
                        entityLocation, islandPrivilege);
            }
            if (ProtectionHelper.shouldPreventInteraction(interactionResult, launcherPlayer, true))
                e.setCancelled();
        });
    }

    private void onProjectileHit(GameEvent<GameEventArgs.ProjectileHitEvent> e) {
        Entity entity = e.getArgs().entity;
        BukkitEntities.getPlayerSource(entity).map(plugin.getPlayers()::getSuperiorPlayer).ifPresent(shooterPlayer -> {
            Location location;
            IslandPrivilege islandPrivilege;
            Block hitBlock;
            InteractionResult interactionResult;

            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                if (entity instanceof FishHook) {
                    Entity hitEntity = e.getArgs().hitEntity;
                    if (hitEntity == null)
                        return;

                    location = hitEntity.getLocation(wrapper.getHandle());
                    islandPrivilege = BukkitEntities.getCategory(entity.getType()).getDamagePrivilege();
                    hitBlock = null;
                } else {
                    hitBlock = e.getArgs().hitBlock;
                    if (hitBlock == null || hitBlock.getType() != CHORUS_FLOWER)
                        return;

                    location = hitBlock.getLocation(wrapper.getHandle());
                    islandPrivilege = IslandPrivileges.BREAK;
                }

                interactionResult = this.protectionManager.get().handleCustomInteraction(shooterPlayer, location, islandPrivilege);
            }
            if (ProtectionHelper.shouldPreventInteraction(interactionResult, shooterPlayer, true)) {
                entity.remove();
                if (hitBlock != null) {
                    ICachedBlock cachedBlock = plugin.getNMSWorld().cacheBlock(hitBlock);
                    hitBlock.setType(Material.AIR);
                    BukkitExecutor.sync(() -> {
                        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                            cachedBlock.setBlock(hitBlock.getLocation(wrapper.getHandle()));
                        }
                        cachedBlock.release();
                    }, 1L);
                }
            }
        });
    }

    private void onSoftExplodeEvent(GameEvent<GameEventArgs.EntityExplodeEvent> e) {
        if (!e.getArgs().isSoftExplosion)
            return;

        Entity entity = e.getArgs().entity;

        BukkitEntities.getPlayerSource(entity).map(plugin.getPlayers()::getSuperiorPlayer).ifPresent(shooterPlayer -> {
            Iterator<Block> blocksIterator = e.getArgs().blocks.iterator();
            while (blocksIterator.hasNext()) {
                Block block = blocksIterator.next();
                Material blockType = block.getType();

                IslandPrivilege islandPrivilege = blockType == CHORUS_FLOWER || blockType == POINTED_DRIPSTONE ?
                        IslandPrivileges.BREAK : IslandPrivileges.INTERACT;

                InteractionResult interactionResult;
                try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                    interactionResult = this.protectionManager.get().handleCustomInteraction(shooterPlayer,
                            block.getLocation(wrapper.getHandle()), islandPrivilege);
                }
                if (ProtectionHelper.shouldPreventInteraction(interactionResult, shooterPlayer, true))
                    blocksIterator.remove();
            }
        });

    }

    /* WORLD EVENTS */

    private void onRaidTrigger(GameEvent<GameEventArgs.RaidTriggerEvent> e) {
        if (!plugin.getGrid().isIslandsWorld(e.getArgs().world))
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getArgs().player);
        Location raidLocation = e.getArgs().raidLocation;

        Island island = plugin.getGrid().getIslandAt(raidLocation);

        if (island == null || island.isSpawn() || !island.isInside(raidLocation) || !island.isMember(superiorPlayer))
            e.setCancelled();
    }

    /* INTERNAL */

    private void registerListeners() {
        registerCallback(GameEventType.BLOCK_PLACE_EVENT, GameEventPriority.NORMAL, this::onBlockPlace);
        registerCallback(GameEventType.PLAYER_INTERACT_EVENT, GameEventPriority.NORMAL, this::onPlayerInteract);
        registerCallback(GameEventType.BLOCK_BREAK_EVENT, GameEventPriority.NORMAL, this::onBlockBreak);
        registerCallback(GameEventType.ENTITY_BLOCK_FORM_EVENT, GameEventPriority.NORMAL, this::onFrostWalker);
        registerCallback(GameEventType.PLAYER_EMPTY_BUCKET_EVENT, GameEventPriority.NORMAL, this::onBucketEmpty);
        registerCallback(GameEventType.PLAYER_FILL_BUCKET_EVENT, GameEventPriority.NORMAL, this::onBucketFill);
        registerCallback(GameEventType.BLOCK_DISPENSE_EVENT, GameEventPriority.NORMAL, this::onBucketDispense);
        registerCallback(GameEventType.PLAYER_ITEM_CONSUME_EVENT, GameEventPriority.NORMAL, this::onChorusFruitConsume);
        registerCallback(GameEventType.ENTITY_DAMAGE_EVENT, GameEventPriority.NORMAL, this::onEntityAttack);
        registerCallback(GameEventType.PLAYER_SHEAR_ENTITY_EVENT, GameEventPriority.NORMAL, this::onEntityShearing);
        registerCallback(GameEventType.HANGING_BREAK_EVENT, GameEventPriority.NORMAL, this::onHangingBreak);
        registerCallback(GameEventType.HANGING_PLACE_EVENT, GameEventPriority.NORMAL, this::onHangingPlace);
        registerCallback(GameEventType.ENTITY_TARGET_EVENT, GameEventPriority.NORMAL, this::onEntityTarget);
        registerCallback(GameEventType.INVENTORY_CLICK_EVENT, GameEventPriority.NORMAL, false, this::onVillagerTrade);
        registerCallback(GameEventType.PLAYER_LEASH_ENTITY_EVENT, GameEventPriority.NORMAL, this::onPlayerLeash);
        registerCallback(GameEventType.PLAYER_UNLEASH_ENTITY_EVENT, GameEventPriority.NORMAL, this::onPlayerUnleash);
        registerCallback(GameEventType.ENTITY_RIDE_EVENT, GameEventPriority.NORMAL, this::onVehicleEnter);
        registerCallback(GameEventType.INVENTORY_OPEN_EVENT, GameEventPriority.NORMAL, this::onVehicleOpen);
        registerCallback(GameEventType.ENTITY_COLLISION_EVENT, GameEventPriority.NORMAL, this::onPlayerCollideWithEntity);
        registerCallback(GameEventType.PLAYER_DROP_ITEM_EVENT, GameEventPriority.NORMAL, this::onPlayerDropItem);
        registerCallback(GameEventType.PLAYER_PICKUP_ITEM_EVENT, GameEventPriority.NORMAL, this::onPlayerPickupItem);
        registerCallback(GameEventType.PROJECTILE_LAUNCH_EVENT, GameEventPriority.NORMAL, this::onPlayerLaunchProjectile);
        registerCallback(GameEventType.PROJECTILE_HIT_EVENT, GameEventPriority.NORMAL, this::onProjectileHit);
        registerCallback(GameEventType.ENTITY_EXPLODE_EVENT, GameEventPriority.NORMAL, this::onSoftExplodeEvent);
        registerCallback(GameEventType.PLAYER_PICKUP_ARROW_EVENT, GameEventPriority.NORMAL, this::onPlayerPickupArrow);
        registerCallback(GameEventType.RAID_TRIGGER_EVENT, GameEventPriority.NORMAL, this::onRaidTrigger);
    }

}

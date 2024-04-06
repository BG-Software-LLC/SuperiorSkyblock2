package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.service.region.InteractionResult;
import com.bgsoftware.superiorskyblock.api.service.region.RegionManagerService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.PlayerHand;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.nms.ICachedBlock;
import com.bgsoftware.superiorskyblock.service.region.ProtectionHelper;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import com.bgsoftware.superiorskyblock.world.BukkitItems;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class ProtectionListener implements Listener {

    private static final ReflectMethod<Entity> PROJECTILE_HIT_TARGET_ENTITY = new ReflectMethod<>(
            ProjectileHitEvent.class, "getHitEntity");
    private static final ReflectMethod<Block> PROJECTILE_HIT_EVENT_TARGET_BLOCK = new ReflectMethod<>(
            ProjectileHitEvent.class, "getHitBlock");
    @Nullable
    private static final Material CHORUS_FLOWER = EnumHelper.getEnum(Material.class, "CHORUS_FLOWER");
    @Nullable
    private static final Material CHORUS_FRUIT = EnumHelper.getEnum(Material.class, "CHORUS_FRUIT");
    @Nullable
    private static final Material BRUSH = EnumHelper.getEnum(Material.class, "BRUSH");

    private final SuperiorSkyblockPlugin plugin;
    private final LazyReference<RegionManagerService> protectionManager = new LazyReference<RegionManagerService>() {
        @Override
        protected RegionManagerService create() {
            return plugin.getServices().getService(RegionManagerService.class);
        }
    };

    public ProtectionListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        this.registerPlayerArrowPickupListener();
        this.registerPlayerAttemptPickupItemListener();
        this.registerPlayerTakeLecternBookListener();
    }

    /* BLOCK INTERACTS */

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        InteractionResult interactionResult = this.protectionManager.get().handleBlockPlace(superiorPlayer, e.getBlock());
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockFertilize(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getItem() == null || e.getClickedBlock() == null ||
                !Materials.BONE_MEAL.toBukkitItem().isSimilar(e.getItem()))
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        InteractionResult interactionResult = this.protectionManager.get().handleBlockFertilize(superiorPlayer, e.getClickedBlock());
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled(true);
    }

    // Patching a dupe glitch with crops and beds: https://github.com/BG-Software-LLC/SuperiorSkyblock2/issues/1672
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBedPlace(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getItem() == null || !e.getItem().getType().name().contains("BED"))
            return;

        // The player right-clicked a block with a bed in his hand
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        InteractionResult interactionResult = this.protectionManager.get().handleBlockPlace(superiorPlayer, e.getClickedBlock());
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSignColorChange(PlayerInteractEvent e) {
        if (e.getItem() == null || e.getAction() != Action.RIGHT_CLICK_BLOCK || ServerVersion.isLegacy() ||
                !Materials.isDye(e.getItem().getType()))
            return;

        BlockState blockState = e.getClickedBlock().getState();

        if (!(blockState instanceof Sign))
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        InteractionResult interactionResult = this.protectionManager.get().handleBlockPlace(superiorPlayer, e.getClickedBlock());
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        InteractionResult interactionResult = this.protectionManager.get().handleBlockBreak(superiorPlayer, e.getBlock());
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null)
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        InteractionResult interactionResult = this.protectionManager.get().handleBlockInteract(superiorPlayer,
                e.getClickedBlock(), e.getAction(), e.getItem());
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onFrostWalker(EntityBlockFormEvent e) {
        if (!(e.getEntity() instanceof Player))
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer((Player) e.getEntity());
        InteractionResult interactionResult = this.protectionManager.get().handleBlockPlace(superiorPlayer, e.getBlock());
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, false))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        InteractionResult interactionResult = this.protectionManager.get().handleBlockPlace(superiorPlayer, e.getBlockClicked());
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        InteractionResult interactionResult = this.protectionManager.get().handleBlockPlace(superiorPlayer, e.getBlockClicked());
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBucketDispense(BlockDispenseEvent e) {
        Material itemType = e.getItem().getType();

        if (!(itemType == Material.BUCKET || itemType == Material.WATER_BUCKET || itemType == Material.LAVA_BUCKET))
            return;

        Location dispenseBlockLocation = new Location(e.getBlock().getWorld(), e.getVelocity().getBlockX(),
                e.getVelocity().getBlockY(), e.getVelocity().getBlockZ());

        Island island = plugin.getGrid().getIslandAt(dispenseBlockLocation);

        if (island != null && !island.isInsideRange(dispenseBlockLocation))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBrushUse(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getItem() == null || e.getClickedBlock() == null ||
                e.getItem().getType() != BRUSH)
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        InteractionResult interactionResult = this.protectionManager.get().handleCustomInteraction(
                superiorPlayer, e.getClickedBlock().getLocation(), IslandPrivileges.BRUSH);
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChorusFruitConsume(PlayerItemConsumeEvent e) {
        if (CHORUS_FRUIT == null || e.getItem().getType() != CHORUS_FRUIT)
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        InteractionResult interactionResult = this.protectionManager.get().handlePlayerConsumeChorusFruit(
                superiorPlayer, e.getPlayer().getLocation());
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onMinecartPlace(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null)
            return;

        PlayerHand playerHand = BukkitItems.getHand(e);
        if (playerHand != PlayerHand.MAIN_HAND)
            return;

        ItemStack handItem = BukkitItems.getHandItem(e.getPlayer(), playerHand);

        if (handItem == null)
            return;

        Material handItemType = handItem.getType();
        Material clickedBlockType = e.getClickedBlock().getType();

        if ((Materials.isMinecart(handItemType) && Materials.isRail(clickedBlockType)) || Materials.isBoat(handItemType)) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
            InteractionResult interactionResult = this.protectionManager.get().handleCustomInteraction(superiorPlayer,
                    e.getClickedBlock().getLocation(), IslandPrivileges.MINECART_PLACE);
            if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
                e.setCancelled(true);
        }
    }

    /* ENTITY INTERACTS */

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractEntityEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        InteractionResult interactionResult = this.protectionManager.get().handleEntityInteract(superiorPlayer,
                e.getRightClicked(), e.getPlayer().getItemInHand());
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractAtEntityEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        InteractionResult interactionResult = this.protectionManager.get().handleEntityInteract(superiorPlayer,
                e.getRightClicked(), e.getPlayer().getItemInHand());
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityAttack(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player)
            return;

        SuperiorPlayer damagerSource = BukkitEntities.getPlayerSource(e.getDamager())
                .map(plugin.getPlayers()::getSuperiorPlayer).orElse(null);
        InteractionResult interactionResult = this.protectionManager.get().handleEntityDamage(e.getDamager(), e.getEntity());
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, damagerSource, true))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityShearing(PlayerShearEntityEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        InteractionResult interactionResult = this.protectionManager.get().handleEntityShear(superiorPlayer, e.getEntity());
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent e) {
        BukkitEntities.getPlayerSource(e.getRemover()).map(plugin.getPlayers()::getSuperiorPlayer).ifPresent(removerPlayer -> {
            InteractionResult interactionResult = this.protectionManager.get().handleEntityInteract(removerPlayer, e.getEntity(), null);
            if (ProtectionHelper.shouldPreventInteraction(interactionResult, removerPlayer, true))
                e.setCancelled(true);
        });
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        InteractionResult interactionResult = this.protectionManager.get().handleEntityInteract(superiorPlayer, e.getEntity(), null);
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent e) {
        if (!(e.getTarget() instanceof Player))
            return;

        InteractionResult interactionResult = this.protectionManager.get().handleEntityDamage(e.getTarget(), e.getEntity());
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, null, false))
            e.setCancelled(true);
    }

    @EventHandler
    public void onVillagerTrade(InventoryClickEvent e) {
        Inventory openInventory = e.getView().getTopInventory();

        if (openInventory == null || openInventory.getType() != InventoryType.MERCHANT)
            return;

        Location location = e.getWhoClicked().getLocation();
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getWhoClicked());
        InteractionResult interactionResult = this.protectionManager.get().handleCustomInteraction(superiorPlayer,
                location, IslandPrivileges.VILLAGER_TRADING);
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, null, false)) {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerLeash(PlayerLeashEntityEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        InteractionResult interactionResult = this.protectionManager.get().handleEntityLeash(superiorPlayer, e.getEntity());
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerUnleash(PlayerUnleashEntityEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        InteractionResult interactionResult = this.protectionManager.get().handleEntityLeash(superiorPlayer, e.getEntity());
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled(true);
    }

    /* VEHICLE INTERACTS */

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDamageEvent e) {
        if (!(e.getAttacker() instanceof Player))
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getAttacker());
        InteractionResult interactionResult = this.protectionManager.get().handleEntityDamage(e.getAttacker(), e.getVehicle());
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent e) {
        if (!(e.getEntered() instanceof Player))
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getEntered());
        Location vehicleLocation = e.getVehicle().getLocation();
        InteractionResult interactionResult = this.protectionManager.get().handleCustomInteraction(superiorPlayer,
                vehicleLocation, IslandPrivileges.MINECART_ENTER);
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleOpen(InventoryOpenEvent e) {
        InventoryHolder inventoryHolder = e.getInventory().getHolder();

        if (!(inventoryHolder instanceof Minecart))
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        Location minecartLocation = ((Minecart) inventoryHolder).getLocation();
        InteractionResult interactionResult = this.protectionManager.get().handleCustomInteraction(superiorPlayer,
                minecartLocation, IslandPrivileges.MINECART_OPEN);
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled(true);
    }

    /* ITEMS */

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        InteractionResult interactionResult = this.protectionManager.get().handlePlayerDropItem(superiorPlayer, e.getItemDrop());
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        InteractionResult interactionResult = this.protectionManager.get().handlePlayerPickupItem(superiorPlayer, e.getItem());
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            e.setCancelled(true);
    }

    /* PROJECTILE INTERACTS */

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPearlTeleport(PlayerTeleportEvent e) {
        if (e.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL)
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        InteractionResult interactionResult = this.protectionManager.get().handlePlayerEnderPearl(superiorPlayer, e.getTo());

        if (!ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            return;

        e.setCancelled(true);

        if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            BukkitItems.addItem(new ItemStack(Material.ENDER_PEARL), e.getPlayer().getInventory(),
                    e.getPlayer().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerFish(ProjectileLaunchEvent e) {
        if (!(e.getEntity() instanceof FishHook) && (ServerVersion.isLegacy() || !(e.getEntity() instanceof Trident)))
            return;

        BukkitEntities.getPlayerSource(e.getEntity()).map(plugin.getPlayers()::getSuperiorPlayer).ifPresent(fisherPlayer -> {
            IslandPrivilege islandPrivilege = e.getEntity() instanceof FishHook ? IslandPrivileges.FISH : IslandPrivileges.PICKUP_DROPS;
            Location entityLocation = e.getEntity().getLocation();
            InteractionResult interactionResult = this.protectionManager.get().handleCustomInteraction(fisherPlayer, entityLocation, islandPrivilege);
            if (ProtectionHelper.shouldPreventInteraction(interactionResult, fisherPlayer, true))
                e.setCancelled(true);
        });
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent e) {
        BukkitEntities.getPlayerSource(e.getEntity()).map(plugin.getPlayers()::getSuperiorPlayer).ifPresent(shooterPlayer -> {
            Location location;
            IslandPrivilege islandPrivilege;
            Block hitBlock;

            if (e.getEntity() instanceof FishHook) {
                if (!PROJECTILE_HIT_TARGET_ENTITY.isValid())
                    return;

                Entity hitEntity = PROJECTILE_HIT_TARGET_ENTITY.invoke(e);

                if (hitEntity == null)
                    return;

                location = hitEntity.getLocation();
                islandPrivilege = BukkitEntities.getCategory(e.getEntityType()).getDamagePrivilege();
                hitBlock = null;
            } else {
                if (!PROJECTILE_HIT_EVENT_TARGET_BLOCK.isValid())
                    return;

                hitBlock = PROJECTILE_HIT_EVENT_TARGET_BLOCK.invoke(e);

                if (hitBlock == null || hitBlock.getType() != CHORUS_FLOWER)
                    return;

                location = hitBlock.getLocation();
                islandPrivilege = IslandPrivileges.BREAK;
            }

            InteractionResult interactionResult = this.protectionManager.get().handleCustomInteraction(shooterPlayer,
                    location, islandPrivilege);
            if (ProtectionHelper.shouldPreventInteraction(interactionResult, shooterPlayer, true)) {
                e.getEntity().remove();
                if (hitBlock != null) {
                    ICachedBlock cachedBlock = plugin.getNMSWorld().cacheBlock(hitBlock);
                    hitBlock.setType(Material.AIR);
                    BukkitExecutor.sync(() -> cachedBlock.setBlock(hitBlock.getLocation()), 1L);
                }
            }
        });
    }

    private void registerPlayerArrowPickupListener() {
        try {
            Class.forName("org.bukkit.event.player.PlayerPickupArrowEvent");
            Bukkit.getPluginManager().registerEvents(new PlayerArrowPickup(), plugin);
        } catch (Exception ignored) {
        }
    }

    private void registerPlayerAttemptPickupItemListener() {
        try {
            Class.forName("org.bukkit.event.player.PlayerAttemptPickupItemEvent");
            Bukkit.getPluginManager().registerEvents(new PaperAttemptPickupListener(), plugin);
        } catch (Exception ignored) {
        }
    }

    private void registerPlayerTakeLecternBookListener() {
        try {
            Class.forName("org.bukkit.event.player.PlayerTakeLecternBookEvent");
            Bukkit.getPluginManager().registerEvents(new PlayerTakeLecternBookListener(), plugin);
        } catch (Exception ignored) {
        }
    }

    private class PaperAttemptPickupListener implements Listener {

        @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
        public void onPlayerItemPickup(PlayerAttemptPickupItemEvent e) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
            InteractionResult interactionResult = protectionManager.get().handlePlayerPickupItem(superiorPlayer, e.getItem());
            if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
                e.setCancelled(true);
        }

    }

    private class PlayerArrowPickup implements Listener {

        @EventHandler
        public void onPlayerArrowPickup(PlayerPickupArrowEvent e) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
            InteractionResult interactionResult = protectionManager.get().handlePlayerPickupItem(superiorPlayer, e.getItem());
            if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
                e.setCancelled(true);
        }

    }

    private class PlayerTakeLecternBookListener implements Listener {

        @EventHandler
        public void onPlayerTakeLecternBook(PlayerTakeLecternBookEvent e) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
            InteractionResult interactionResult = protectionManager.get().handleBlockInteract(superiorPlayer,
                    e.getLectern().getBlock(), Action.RIGHT_CLICK_BLOCK, null);
            if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
                e.setCancelled(true);
        }

    }

}

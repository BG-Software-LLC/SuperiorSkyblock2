package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.entities.EntityUtils;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.items.ItemUtils;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.bgsoftware.superiorskyblock.utils.logic.ProtectionLogic;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fish;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
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
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

@SuppressWarnings("unused")
public final class ProtectionListener implements Listener {

    private static final ReflectMethod<Entity> PROJECTILE_HIT_TARGET_ENTITY = new ReflectMethod<>(ProjectileHitEvent.class, "getHitEntity");

    private final SuperiorSkyblockPlugin plugin;

    public ProtectionListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;

        try {
            Class.forName("org.bukkit.event.player.PlayerPickupArrowEvent");
            Bukkit.getPluginManager().registerEvents(new PlayerArrowPickup(), plugin);
        } catch (Exception ignored) {
        }

        try {
            Class.forName("org.bukkit.event.player.PlayerAttemptPickupItemEvent");
            Bukkit.getPluginManager().registerEvents(new PaperAttemptPickupListener(), plugin);
        } catch (Exception ignored) {
        }

    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!ProtectionLogic.handleBlockPlace(e.getBlock(), e.getPlayer(), true))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if (!ProtectionLogic.handleBlockBreak(e.getBlock(), e.getPlayer(), true))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null)
            return;

        if (!plugin.getSettings().getInteractables().contains(e.getClickedBlock().getType().name()) &&
                plugin.getStackedBlocks().getStackedBlockAmount(e.getClickedBlock()) <= 1)
            return;

        Block clickedBlock = e.getClickedBlock();

        Island island = plugin.getGrid().getIslandAt(clickedBlock.getLocation());
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if (island == null) {
            if (!superiorPlayer.hasBypassModeEnabled() && plugin.getGrid().isIslandsWorld(e.getPlayer().getWorld())) {
                Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
                e.setCancelled(true);
            }

            return;
        }

        IslandPrivilege requiredPrivilege;

        BlockState blockState = clickedBlock.getState();
        Material blockType = clickedBlock.getType();
        String blockTypeName = blockType.name();

        if (isChest(blockState, blockType)) requiredPrivilege = IslandPrivileges.CHEST_ACCESS;
        else if (blockState instanceof InventoryHolder) requiredPrivilege = IslandPrivileges.USE;
        else if (blockState instanceof Sign) requiredPrivilege = IslandPrivileges.SIGN_INTERACT;
        else if (blockType == Materials.SPAWNER.toBukkitType()) requiredPrivilege = IslandPrivileges.SPAWNER_BREAK;
        else if (blockTypeName.equals("SOIL") || blockTypeName.equals("FARMLAND"))
            requiredPrivilege = e.getAction() == Action.PHYSICAL ? IslandPrivileges.FARM_TRAMPING : IslandPrivileges.BUILD;
        else if (blockTypeName.equals("TURTLE_EGG"))
            requiredPrivilege = e.getAction() == Action.PHYSICAL ? IslandPrivileges.TURTLE_EGG_TRAMPING : IslandPrivileges.BUILD;
        else if (blockType.name().equals("SWEET_BERRY_BUSH") && e.getAction() == Action.RIGHT_CLICK_BLOCK)
            requiredPrivilege = IslandPrivileges.FARM_TRAMPING;
        else if (plugin.getStackedBlocks().getStackedBlockAmount(clickedBlock) > 1) requiredPrivilege = IslandPrivileges.BREAK;
        else requiredPrivilege = IslandPrivileges.INTERACT;

        if (!island.hasPermission(superiorPlayer, requiredPrivilege)) {
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
            return;
        }

        if (plugin.getSettings().getValuableBlocks().contains(Key.of(blockState)) &&
                !island.hasPermission(superiorPlayer, IslandPrivileges.VALUABLE_BREAK)) {
            e.setCancelled(true);
            Locale.sendProtectionMessage(e.getPlayer());
            return;
        }

        if (!island.isInsideRange(clickedBlock.getLocation())) {
            e.setCancelled(true);
            Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
        }
    }

    private boolean isChest(BlockState blockState, Material type) {
        return blockState instanceof Chest || type.name().contains("SHULKER_BOX") || type.name().contains("BARREL") || type == Material.ENDER_CHEST;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onFrostWalker(EntityBlockFormEvent e) {
        if (!(e.getEntity() instanceof Player))
            return;

        if (!ProtectionLogic.handleBlockPlace(e.getBlock(), (Player) e.getEntity(), false))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent e) {
        if (!(e.getTarget() instanceof Player))
            return;

        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getTarget());

        if (island != null && !island.hasPermission(superiorPlayer, IslandPrivileges.MONSTER_DAMAGE))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent e) {
        Player remover = null;

        if (e.getRemover() instanceof Player) {
            remover = (Player) e.getRemover();
        } else if (e.getRemover() instanceof Projectile) {
            ProjectileSource projectileSource = ((Projectile) e.getRemover()).getShooter();
            if (projectileSource instanceof Player)
                remover = (Player) projectileSource;
        }

        if (remover == null)
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(remover);
        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if (island == null) {
            if (!superiorPlayer.hasBypassModeEnabled() && plugin.getGrid().isIslandsWorld(superiorPlayer.getWorld())) {
                Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
                e.setCancelled(true);
            }

            return;
        }

        IslandPrivilege islandPermission = e.getEntity() instanceof ItemFrame ? IslandPrivileges.ITEM_FRAME : IslandPrivileges.PAINTING;
        if (!island.hasPermission(superiorPlayer, islandPermission)) {
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
            return;
        }

        if (!island.isInsideRange(e.getEntity().getLocation())) {
            e.setCancelled(true);
            Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if (island == null) {
            if (!superiorPlayer.hasBypassModeEnabled() && plugin.getGrid().isIslandsWorld(superiorPlayer.getWorld())) {
                Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
                e.setCancelled(true);
            }

            return;
        }

        IslandPrivilege islandPermission = e.getEntity() instanceof ItemFrame ? IslandPrivileges.ITEM_FRAME : IslandPrivileges.PAINTING;
        if (!island.hasPermission(superiorPlayer, islandPermission)) {
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
            return;
        }

        if (!island.isInsideRange(e.getEntity().getLocation())) {
            e.setCancelled(true);
            Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if (island == null) {
            if (plugin.getGrid().isIslandsWorld(e.getBlock().getWorld()))
                e.setCancelled(true);

            return;
        }

        for (Block block : e.getBlocks()) {
            if (!island.isInsideRange(block.getRelative(e.getDirection()).getLocation())) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if (island == null) {
            if (plugin.getGrid().isIslandsWorld(e.getBlock().getWorld()))
                e.setCancelled(true);

            return;
        }

        for (Block block : e.getBlocks()) {
            if (!island.isInsideRange(block.getRelative(e.getDirection()).getLocation())) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockFlow(BlockFromToEvent e) {
        Island fromIsland = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
        Location toLocation = e.getBlock().getRelative(e.getFace()).getLocation();

        if ((fromIsland == null && plugin.getGrid().isIslandsWorld(e.getBlock().getWorld())) ||
                (fromIsland != null && !fromIsland.isInsideRange(toLocation))) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getBlockClicked().getLocation());

        if (island == null) {
            if (!superiorPlayer.hasBypassModeEnabled() && plugin.getGrid().isIslandsWorld(e.getBlockClicked().getWorld())) {
                Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
                e.setCancelled(true);
            }

            return;
        }

        if (!island.hasPermission(superiorPlayer, IslandPrivileges.BUILD)) {
            e.setCancelled(true);
            Locale.sendProtectionMessage(e.getPlayer());
            return;
        }

        if (!island.isInsideRange(e.getBlockClicked().getLocation())) {
            e.setCancelled(true);
            Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getBlockClicked().getLocation());

        if (island == null) {
            if (!superiorPlayer.hasBypassModeEnabled() && plugin.getGrid().isIslandsWorld(e.getBlockClicked().getWorld())) {
                Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
                e.setCancelled(true);
            }

            return;
        }

        if (!island.hasPermission(superiorPlayer, IslandPrivileges.BREAK)) {
            e.setCancelled(true);
            Locale.sendProtectionMessage(e.getPlayer());
            return;
        }

        if (!island.isInsideRange(e.getBlockClicked().getLocation())) {
            e.setCancelled(true);
            Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractAtEntityEvent e) {
        ProtectionLogic.handleEntityInteract(e);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractEntityEvent e) {
        ProtectionLogic.handleEntityInteract(e);
    }

    @EventHandler
    public void onVillagerTrade(InventoryClickEvent e) {
        Inventory openInventory = e.getView().getTopInventory();

        if (openInventory == null || openInventory.getType() != InventoryType.MERCHANT)
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getWhoClicked());
        Island island = plugin.getGrid().getIslandAt(e.getWhoClicked().getLocation());

        if (island != null && !island.hasPermission(superiorPlayer, IslandPrivileges.VILLAGER_TRADING)) {
            e.setCancelled(true);
            e.getWhoClicked().closeInventory();
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerLeash(PlayerLeashEntityEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if (island != null && !island.hasPermission(superiorPlayer, IslandPrivileges.LEASH)) {
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onUnleashEntity(PlayerInteractAtEntityEvent e) {
        if (!(e.getRightClicked() instanceof LeashHitch))
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getRightClicked().getLocation());

        if (island != null && !island.hasPermission(superiorPlayer, IslandPrivileges.LEASH)) {
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(superiorPlayer.getLocation());

        if (island != null && !island.hasPermission(superiorPlayer, IslandPrivileges.DROP_ITEMS)) {
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerItemPickup(PlayerPickupItemEvent e) {
        if (!ProtectionLogic.handlePlayerPickupItem(e.getPlayer(), e.getItem()))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityAttack(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Painting || e.getEntity() instanceof ItemFrame || e.getEntity() instanceof Player)
            return;

        SuperiorPlayer damagerPlayer = EntityUtils.getPlayerDamager(e);

        if (damagerPlayer == null)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        IslandPrivilege islandPermission = EntityUtils.isMonster(e.getEntityType()) ?
                IslandPrivileges.MONSTER_DAMAGE : EntityUtils.isAnimal(e.getEntityType()) ?
                IslandPrivileges.ANIMAL_DAMAGE : IslandPrivileges.BREAK;

        if (island != null && !island.hasPermission(damagerPlayer, islandPermission)) {
            e.setCancelled(true);
            Locale.sendProtectionMessage(damagerPlayer);

            if (e.getDamager() instanceof Arrow && e.getEntity().getFireTicks() > 0)
                e.getEntity().setFireTicks(0);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntitySpawn(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || !e.hasItem())
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getClickedBlock().getLocation());

        EntityType spawnType = ItemUtils.getEntityType(e.getItem());

        if (spawnType == EntityType.UNKNOWN)
            return;

        try {
            IslandPrivilege islandPermission = EntityUtils.isMonster(spawnType) ?
                    IslandPrivileges.MONSTER_SPAWN : EntityUtils.isAnimal(spawnType) ?
                    IslandPrivileges.ANIMAL_SPAWN : IslandPrivileges.BUILD;

            if (island != null && !island.hasPermission(superiorPlayer, islandPermission)) {
                e.setCancelled(true);
                Locale.sendProtectionMessage(superiorPlayer);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleSpawn(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null || e.getItem() == null)
            return;

        if (!e.getItem().getType().name().contains("MINECART") && !e.getItem().getType().name().contains("BOAT"))
            return;

        if (e.getItem().getType().name().contains("MINECART") && !e.getClickedBlock().getType().name().contains("RAIL"))
            return;

        Island island = plugin.getGrid().getIslandAt(e.getClickedBlock().getLocation());

        if (island == null)
            return;

        if (!island.hasPermission(e.getPlayer(), IslandPrivileges.MINECART_PLACE)) {
            e.setCancelled(true);
            Locale.sendProtectionMessage(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDamageEvent e) {
        if (!(e.getAttacker() instanceof Player))
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getAttacker());
        Island island = plugin.getGrid().getIslandAt(e.getVehicle().getLocation());

        if (island == null)
            return;

        if (!island.hasPermission(superiorPlayer, IslandPrivileges.MINECART_DAMAGE)) {
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent e) {
        if (!(e.getEntered() instanceof Player))
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getEntered());
        Island island = plugin.getGrid().getIslandAt(e.getVehicle().getLocation());

        if (island == null)
            return;

        if (!island.hasPermission(superiorPlayer, IslandPrivileges.MINECART_ENTER)) {
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleOpen(InventoryOpenEvent e) {
        InventoryHolder inventoryHolder = e.getInventory().getHolder();

        if (!(inventoryHolder instanceof Minecart))
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(((Minecart) inventoryHolder).getLocation());

        if (island == null)
            return;

        if (!island.hasPermission(superiorPlayer, IslandPrivileges.MINECART_OPEN)) {
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPearlTeleport(PlayerTeleportEvent e) {
        if (e.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL || !plugin.getGrid().isIslandsWorld(e.getTo().getWorld()))
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getTo());

        if (island == null) {
            e.setCancelled(true);
            Locale.TELEPORT_OUTSIDE_ISLAND.send(superiorPlayer);
        } else if (!island.hasPermission(superiorPlayer, IslandPrivileges.ENDER_PEARL)) {
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
        } else if (!island.isInsideRange(e.getTo())) {
            e.setCancelled(true);
            Locale.TELEPORT_OUTSIDE_ISLAND.send(superiorPlayer);
        }

        if (e.isCancelled() && e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            e.getPlayer().getInventory().addItem(new ItemStack(Material.ENDER_PEARL));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerFish(ProjectileLaunchEvent e) {
        if (!(e.getEntity() instanceof FishHook) &&
                (ServerVersion.isLessThan(ServerVersion.v1_13) || !(e.getEntity() instanceof Trident)))
            return;

        ProjectileSource projectileSource = e.getEntity().getShooter();

        if (!(projectileSource instanceof Player))
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer((Player) projectileSource);
        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if (island == null)
            return;

        IslandPrivilege islandPrivilege = e.getEntity() instanceof FishHook ?
                IslandPrivileges.FISH : IslandPrivileges.PICKUP_DROPS;

        if (!island.hasPermission(superiorPlayer, islandPrivilege)) {
            e.setCancelled(true);
            // Using this method to fix issue #76 (A NPE error)
            Locale.sendProtectionMessage((Player) projectileSource, superiorPlayer.getUserLocale());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent e) {
        if (!PROJECTILE_HIT_TARGET_ENTITY.isValid() || !(e.getEntity() instanceof FishHook))
            return;

        ProjectileSource projectileSource = e.getEntity().getShooter();

        if (!(projectileSource instanceof Player))
            return;

        Entity hitEntity = PROJECTILE_HIT_TARGET_ENTITY.invoke(e);

        if (hitEntity == null)
            return;

        Island island = plugin.getGrid().getIslandAt(hitEntity.getLocation());

        if (island == null)
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer((Player) projectileSource);

        IslandPrivilege requiredPrivilege = EntityUtils.isMonster(e.getEntityType()) ?
                IslandPrivileges.MONSTER_DAMAGE : EntityUtils.isAnimal(e.getEntityType()) ?
                IslandPrivileges.ANIMAL_DAMAGE : IslandPrivileges.BREAK;

        if (!island.hasPermission(superiorPlayer, requiredPrivilege)) {
            e.getEntity().remove();
            // Using this method to fix issue #76 (A NPE error)
            Locale.sendProtectionMessage((Player) projectileSource, superiorPlayer.getUserLocale());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFertilize(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getItem() == null ||
                !Materials.BONE_MEAL.toBukkitItem().isSimilar(e.getItem()))
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getClickedBlock().getLocation());

        if (island == null) {
            if (!superiorPlayer.hasBypassModeEnabled() && plugin.getGrid().isIslandsWorld(e.getPlayer().getWorld())) {
                Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
                e.setCancelled(true);
            }

            return;
        }

        if (!island.hasPermission(superiorPlayer, IslandPrivileges.FERTILIZE)) {
            e.setCancelled(true);
            Locale.sendProtectionMessage(e.getPlayer());
            return;
        }

        if (!island.isInsideRange(e.getClickedBlock().getLocation())) {
            e.setCancelled(true);
            Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPickupFishBucket(PlayerInteractEntityEvent e) {
        //noinspection deprecation
        if (!(e.getRightClicked() instanceof Fish) || ServerVersion.isLegacy())
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getRightClicked().getLocation());

        if (island == null) {
            if (!superiorPlayer.hasBypassModeEnabled() && plugin.getGrid().isIslandsWorld(e.getPlayer().getWorld())) {
                Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
                e.setCancelled(true);
            }

            return;
        }

        if (!island.hasPermission(superiorPlayer, IslandPrivileges.PICKUP_FISH)) {
            e.setCancelled(true);
            Locale.sendProtectionMessage(e.getPlayer());
            return;
        }

        if (!island.isInsideRange(e.getRightClicked().getLocation())) {
            e.setCancelled(true);
            Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
        }

    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityShearing(PlayerShearEntityEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if (island != null && !island.hasPermission(superiorPlayer, IslandPrivileges.ANIMAL_SHEAR)) {
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
        }
    }

    private class PlayerArrowPickup implements Listener {

        @EventHandler
        public void onPlayerArrowPickup(PlayerPickupArrowEvent e) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
            Island island = plugin.getGrid().getIslandAt(superiorPlayer.getLocation());

            if (island != null && !island.hasPermission(superiorPlayer, IslandPrivileges.PICKUP_DROPS)) {
                e.setCancelled(true);
                Locale.sendProtectionMessage(superiorPlayer);
            }
        }

    }

    private static final class PaperAttemptPickupListener implements Listener {

        @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
        public void onPlayerItemPickup(PlayerAttemptPickupItemEvent e) {
            if (!ProtectionLogic.handlePlayerPickupItem(e.getPlayer(), e.getItem()))
                e.setCancelled(true);
        }

    }

}

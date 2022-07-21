package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.key.KeyImpl;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.nms.ICachedBlock;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import com.bgsoftware.superiorskyblock.world.BukkitItems;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Mule;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
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
import org.bukkit.event.player.PlayerUnleashEntityEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.EnumSet;

public class ProtectionListener implements Listener {

    private static final ReflectMethod<Entity> PROJECTILE_HIT_TARGET_ENTITY = new ReflectMethod<>(
            ProjectileHitEvent.class, "getHitEntity");
    private static final ReflectMethod<Block> PROJECTILE_HIT_EVENT_TARGET_BLOCK = new ReflectMethod<>(
            ProjectileHitEvent.class, "getHitBlock");

    private static final Material FARMLAND = Materials.getMaterialSafe("FARMLAND", "SOIL");
    @Nullable
    private static final Material TURTLE_EGG = Materials.getMaterialSafe("TURTLE_EGG");
    @Nullable
    private static final Material SWEET_BERRY_BUSH = Materials.getMaterialSafe("SWEET_BERRY_BUSH");
    @Nullable
    private static final EntityType AXOLOTL_TYPE = getSafeEntityType("AXOLOTL");
    @Nullable
    private static final Material CHORUS_FLOWER = Materials.getMaterialSafe("CHORUS_FLOWER");

    private final SuperiorSkyblockPlugin plugin;

    public ProtectionListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        this.registerPlayerArrowPickupListener();
        this.registerPlayerAttemptPickupItemListener();
    }

    public enum Flag {

        SEND_MESSAGES,
        PREVENT_OUTSIDE_ISLANDS

    }

    /* BLOCK INTERACTS */

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (preventBlockPlace(e.getBlock(), e.getPlayer(), Flag.SEND_MESSAGES, Flag.PREVENT_OUTSIDE_ISLANDS))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockFertilize(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getItem() != null &&
                Materials.BONE_MEAL.toBukkitItem().isSimilar(e.getItem())) {
            Location blockLocation = e.getClickedBlock().getLocation();
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
            Island island = plugin.getGrid().getIslandAt(blockLocation);
            if (preventInteraction(island, blockLocation, superiorPlayer, IslandPrivileges.FERTILIZE,
                    Flag.SEND_MESSAGES, Flag.PREVENT_OUTSIDE_ISLANDS))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if (preventBlockBreak(e.getBlock(), e.getPlayer(), Flag.SEND_MESSAGES, Flag.PREVENT_OUTSIDE_ISLANDS))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null)
            return;

        int stackedBlockAmount = plugin.getStackedBlocks().getStackedBlockAmount(e.getClickedBlock());

        if (stackedBlockAmount <= 1 && !plugin.getSettings().getInteractables().contains(e.getClickedBlock().getType().name()))
            return;

        Location blockLocation = e.getClickedBlock().getLocation();
        Island island = plugin.getGrid().getIslandAt(blockLocation);
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if (preventInteraction(island, blockLocation, superiorPlayer, Flag.SEND_MESSAGES, Flag.PREVENT_OUTSIDE_ISLANDS)) {
            e.setCancelled(true);
            return;
        }

        if (island == null)
            return;

        IslandPrivilege islandPrivilege;

        BlockState blockState = e.getClickedBlock().getState();
        Material blockType = e.getClickedBlock().getType();

        if (Materials.isChest(blockType)) {
            islandPrivilege = IslandPrivileges.CHEST_ACCESS;
        } else if (blockState instanceof InventoryHolder) {
            islandPrivilege = IslandPrivileges.USE;
        } else if (blockState instanceof Sign) {
            islandPrivilege = IslandPrivileges.SIGN_INTERACT;
        } else if (blockType == Materials.SPAWNER.toBukkitType()) {
            islandPrivilege = IslandPrivileges.SPAWNER_BREAK;
        } else if (blockType == FARMLAND) {
            islandPrivilege = e.getAction() == Action.PHYSICAL ? IslandPrivileges.FARM_TRAMPING : IslandPrivileges.BUILD;
        } else if (TURTLE_EGG != null && blockType == TURTLE_EGG) {
            islandPrivilege = e.getAction() == Action.PHYSICAL ? IslandPrivileges.TURTLE_EGG_TRAMPING : IslandPrivileges.BUILD;
        } else if (SWEET_BERRY_BUSH != null && blockType == SWEET_BERRY_BUSH && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            islandPrivilege = IslandPrivileges.FARM_TRAMPING;
        } else if (stackedBlockAmount > 1) {
            islandPrivilege = IslandPrivileges.BREAK;
        } else if (blockType == Material.PUMPKIN) {
            islandPrivilege = IslandPrivileges.BREAK;
        } else {
            islandPrivilege = IslandPrivileges.INTERACT;
        }

        if (preventInteraction(island, blockLocation, superiorPlayer, islandPrivilege, Flag.SEND_MESSAGES))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onFrostWalker(EntityBlockFormEvent e) {
        if (e.getEntity() instanceof Player) {
            if (preventBlockPlace(e.getBlock(), (Player) e.getEntity(), Flag.PREVENT_OUTSIDE_ISLANDS))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if (preventBlockPlace(e.getBlockClicked(), e.getPlayer(), Flag.SEND_MESSAGES, Flag.PREVENT_OUTSIDE_ISLANDS))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (preventBlockPlace(e.getBlockClicked(), e.getPlayer(), Flag.SEND_MESSAGES, Flag.PREVENT_OUTSIDE_ISLANDS))
            e.setCancelled(true);
    }

    public boolean preventBlockPlace(Block block, Player player, Flag... flags) {
        Location blockLocation = block.getLocation();
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);
        Island island = plugin.getGrid().getIslandAt(blockLocation);

        EnumSet<Flag> flagsSet = flags.length == 0 ? EnumSet.noneOf(Flag.class) : EnumSet.copyOf(Arrays.asList(flags));

        if (preventInteraction(island, blockLocation, superiorPlayer, IslandPrivileges.BUILD, flagsSet))
            return true;

        if (island != null && island.isBeingRecalculated()) {
            if (flagsSet.contains(Flag.SEND_MESSAGES))
                Message.ISLAND_BEING_CALCULATED.send(player);
            return true;
        }

        return false;
    }

    public boolean preventBlockBreak(Block block, Player player, Flag... flags) {
        Island island = plugin.getGrid().getIslandAt(block.getLocation());
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);

        EnumSet<Flag> flagsSet = flags.length == 0 ? EnumSet.noneOf(Flag.class) : EnumSet.copyOf(Arrays.asList(flags));

        Material blockType = block.getType();
        IslandPrivilege islandPrivilege = blockType == Materials.SPAWNER.toBukkitType() ?
                IslandPrivileges.SPAWNER_BREAK : IslandPrivileges.BREAK;

        Location blockLocation = block.getLocation();

        if (preventInteraction(island, blockLocation, superiorPlayer, islandPrivilege, flagsSet))
            return true;

        if (plugin.getSettings().getValuableBlocks().contains(KeyImpl.of(block))) {
            flagsSet.remove(Flag.PREVENT_OUTSIDE_ISLANDS);
            return preventInteraction(island, blockLocation, superiorPlayer, IslandPrivileges.VALUABLE_BREAK, flagsSet);
        }

        if (island != null && island.isBeingRecalculated()) {
            if (flagsSet.contains(Flag.SEND_MESSAGES))
                Message.ISLAND_BEING_CALCULATED.send(player);
            return true;
        }

        return false;
    }

    /* ENTITY INTERACTS */

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractEntityEvent e) {
        if (preventEntityInteract(e.getPlayer(), e.getRightClicked(), Flag.PREVENT_OUTSIDE_ISLANDS, Flag.SEND_MESSAGES))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractAtEntityEvent e) {
        if (preventEntityInteract(e.getPlayer(), e.getRightClicked(), Flag.PREVENT_OUTSIDE_ISLANDS, Flag.SEND_MESSAGES))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityAttack(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            BukkitEntities.getPlayerSource(e.getDamager()).map(plugin.getPlayers()::getSuperiorPlayer).ifPresent(damagerPlayer -> {
                Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

                IslandPrivilege islandPrivilege;

                if (BukkitEntities.isMonster(e.getEntityType())) {
                    islandPrivilege = IslandPrivileges.MONSTER_DAMAGE;
                } else if (BukkitEntities.isAnimal(e.getEntityType())) {
                    islandPrivilege = IslandPrivileges.ANIMAL_DAMAGE;
                } else if (e.getEntity() instanceof Painting || e.getEntity() instanceof ItemFrame) {
                    islandPrivilege = IslandPrivileges.ITEM_FRAME;
                } else {
                    islandPrivilege = IslandPrivileges.BREAK;
                }

                if (preventInteraction(island, e.getEntity().getLocation(), damagerPlayer, islandPrivilege,
                        Flag.SEND_MESSAGES, Flag.PREVENT_OUTSIDE_ISLANDS)) {
                    e.setCancelled(true);
                    if (e.getDamager() instanceof Arrow && e.getEntity().getFireTicks() > 0)
                        e.getEntity().setFireTicks(0);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityShearing(PlayerShearEntityEvent e) {
        Location entityLocation = e.getEntity().getLocation();
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(entityLocation);
        if (preventInteraction(island, entityLocation, superiorPlayer, IslandPrivileges.ANIMAL_SHEAR,
                Flag.SEND_MESSAGES, Flag.PREVENT_OUTSIDE_ISLANDS))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakByEntityEvent e) {
        BukkitEntities.getPlayerSource(e.getRemover()).map(plugin.getPlayers()::getSuperiorPlayer).ifPresent(removerPlayer -> {
            IslandPrivilege islandPrivilege = e.getEntity() instanceof ItemFrame ? IslandPrivileges.ITEM_FRAME : IslandPrivileges.PAINTING;
            Location entityLocation = e.getEntity().getLocation();
            Island island = plugin.getGrid().getIslandAt(entityLocation);
            if (preventInteraction(island, entityLocation, removerPlayer, islandPrivilege, Flag.SEND_MESSAGES, Flag.PREVENT_OUTSIDE_ISLANDS))
                e.setCancelled(true);
        });
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent e) {
        IslandPrivilege islandPrivilege = e.getEntity() instanceof ItemFrame ? IslandPrivileges.ITEM_FRAME : IslandPrivileges.PAINTING;
        Location entityLocation = e.getEntity().getLocation();
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(entityLocation);
        if (preventInteraction(island, entityLocation, superiorPlayer, islandPrivilege,
                Flag.PREVENT_OUTSIDE_ISLANDS, Flag.SEND_MESSAGES))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent e) {
        if (e.getTarget() instanceof Player) {
            Location entityLocation = e.getEntity().getLocation();
            Island island = plugin.getGrid().getIslandAt(entityLocation);
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getTarget());
            if (preventInteraction(island, entityLocation, superiorPlayer, IslandPrivileges.MONSTER_DAMAGE))
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onVillagerTrade(InventoryClickEvent e) {
        Inventory openInventory = e.getView().getTopInventory();
        if (openInventory != null && openInventory.getType() == InventoryType.MERCHANT) {
            Location location = e.getWhoClicked().getLocation();
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getWhoClicked());
            Island island = plugin.getGrid().getIslandAt(location);
            if (preventInteraction(island, location, superiorPlayer, IslandPrivileges.VILLAGER_TRADING)) {
                e.setCancelled(true);
                e.getWhoClicked().closeInventory();
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerLeash(PlayerLeashEntityEvent e) {
        Location entityLocation = e.getEntity().getLocation();
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(entityLocation);
        if (preventInteraction(island, entityLocation, superiorPlayer, IslandPrivileges.LEASH,
                Flag.SEND_MESSAGES, Flag.PREVENT_OUTSIDE_ISLANDS))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerUnleash(PlayerUnleashEntityEvent e) {
        Location entityLocation = e.getEntity().getLocation();
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(entityLocation);
        if (preventInteraction(island, entityLocation, superiorPlayer, IslandPrivileges.LEASH,
                Flag.SEND_MESSAGES, Flag.PREVENT_OUTSIDE_ISLANDS))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntitySpawn(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || !e.hasItem())
            return;

        EntityType spawnType = BukkitItems.getEntityType(e.getItem());

        if (spawnType != EntityType.UNKNOWN) {
            IslandPrivilege islandPrivilege = BukkitEntities.isMonster(spawnType) ?
                    IslandPrivileges.MONSTER_SPAWN : BukkitEntities.isAnimal(spawnType) ?
                    IslandPrivileges.ANIMAL_SPAWN : IslandPrivileges.BUILD;
            Location blockLocation = e.getClickedBlock().getLocation();
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
            Island island = plugin.getGrid().getIslandAt(blockLocation);
            if (preventInteraction(island, blockLocation, superiorPlayer, islandPrivilege,
                    Flag.SEND_MESSAGES, Flag.PREVENT_OUTSIDE_ISLANDS))
                e.setCancelled(true);
        }
    }

    public boolean preventEntityInteract(Player player, Entity rightClicked, Flag... flags) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);
        Island island = plugin.getGrid().getIslandAt(rightClicked.getLocation());

        EnumSet<Flag> flagsSet = flags.length == 0 ? EnumSet.noneOf(Flag.class) : EnumSet.copyOf(Arrays.asList(flags));

        Location entityLocation = rightClicked.getLocation();

        if (preventInteraction(island, entityLocation, superiorPlayer, flagsSet))
            return true;

        ItemStack usedItem = player.getItemInHand();

        boolean closeInventory = false;

        IslandPrivilege islandPrivilege;

        if (rightClicked instanceof ArmorStand) {
            islandPrivilege = IslandPrivileges.INTERACT;
        } else if (usedItem != null && rightClicked instanceof Animals &&
                plugin.getNMSEntities().isAnimalFood(usedItem, (Animals) rightClicked)) {
            islandPrivilege = IslandPrivileges.ANIMAL_BREED;
        } else if (usedItem != null && usedItem.getType() == Material.NAME_TAG) {
            islandPrivilege = IslandPrivileges.NAME_ENTITY;
        } else if (rightClicked instanceof Villager) {
            islandPrivilege = IslandPrivileges.VILLAGER_TRADING;
            closeInventory = true;
        } else if (rightClicked instanceof Horse || (ServerVersion.isAtLeast(ServerVersion.v1_11) && (
                rightClicked instanceof Mule || rightClicked instanceof Donkey))) {
            islandPrivilege = IslandPrivileges.HORSE_INTERACT;
            closeInventory = true;
        } else if (usedItem != null && rightClicked instanceof Creeper &&
                usedItem.getType() == Material.FLINT_AND_STEEL) {
            islandPrivilege = IslandPrivileges.IGNITE_CREEPER;
        } else if (usedItem != null && ServerVersion.isAtLeast(ServerVersion.v1_17) &&
                usedItem.getType() == Material.WATER_BUCKET && rightClicked.getType() == AXOLOTL_TYPE) {
            islandPrivilege = IslandPrivileges.PICKUP_AXOLOTL;
        } else if (rightClicked instanceof ItemFrame) {
            islandPrivilege = IslandPrivileges.ITEM_FRAME;
        } else if (rightClicked instanceof Fish && !ServerVersion.isLegacy()) {
            islandPrivilege = IslandPrivileges.PICKUP_FISH;
        } else {
            return false;
        }

        flagsSet.remove(Flag.PREVENT_OUTSIDE_ISLANDS); // Disable check for prevent outside island being called twice
        if (preventInteraction(island, entityLocation, superiorPlayer, islandPrivilege, flagsSet)) {
            if (closeInventory) {
                BukkitExecutor.sync(() -> {
                    if (player.isOnline()) {
                        Inventory openInventory = player.getOpenInventory().getTopInventory();
                        if (openInventory != null && (openInventory.getType() == InventoryType.MERCHANT ||
                                openInventory.getType() == InventoryType.CHEST))
                            player.closeInventory();
                    }
                }, 1L);
            }
            return true;
        }

        return false;
    }

    /* VEHICLE INTERACTS */

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleSpawn(PlayerInteractEvent e) {
        if (e.getClickedBlock() != null && e.getItem() != null &&
                (Materials.isMinecart(e.getItem().getType()) ?
                        Materials.isRail(e.getClickedBlock().getType()) : Materials.isBoat(e.getItem().getType()))) {
            Location blockLocation = e.getClickedBlock().getLocation();
            Island island = plugin.getGrid().getIslandAt(blockLocation);
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
            if (preventInteraction(island, blockLocation, superiorPlayer, IslandPrivileges.MINECART_PLACE,
                    Flag.SEND_MESSAGES, Flag.PREVENT_OUTSIDE_ISLANDS))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDamageEvent e) {
        if (e.getAttacker() instanceof Player) {
            Location vehicleLocation = e.getVehicle().getLocation();
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getAttacker());
            Island island = plugin.getGrid().getIslandAt(vehicleLocation);
            if (preventInteraction(island, vehicleLocation, superiorPlayer, IslandPrivileges.MINECART_DAMAGE,
                    Flag.SEND_MESSAGES, Flag.PREVENT_OUTSIDE_ISLANDS))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent e) {
        if (e.getEntered() instanceof Player) {
            Location vehicleLocation = e.getVehicle().getLocation();
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getEntered());
            Island island = plugin.getGrid().getIslandAt(e.getVehicle().getLocation());
            if (preventInteraction(island, vehicleLocation, superiorPlayer, IslandPrivileges.MINECART_ENTER,
                    Flag.SEND_MESSAGES, Flag.PREVENT_OUTSIDE_ISLANDS))
                e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onVehicleOpen(InventoryOpenEvent e) {
        InventoryHolder inventoryHolder = e.getInventory().getHolder();
        if (inventoryHolder instanceof Minecart) {
            Location minecartLocation = ((Minecart) inventoryHolder).getLocation();
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
            Island island = plugin.getGrid().getIslandAt(minecartLocation);
            if (preventInteraction(island, minecartLocation, superiorPlayer, IslandPrivileges.MINECART_OPEN,
                    Flag.SEND_MESSAGES, Flag.PREVENT_OUTSIDE_ISLANDS))
                e.setCancelled(true);
        }
    }

    /* ITEMS */

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        Location location = e.getPlayer().getLocation();
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(location);
        if (preventInteraction(island, location, superiorPlayer, IslandPrivileges.DROP_ITEMS, Flag.SEND_MESSAGES))
            e.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent e) {
        if (preventPlayerPickupItem(e.getItem(), e.getPlayer(), Flag.SEND_MESSAGES, Flag.PREVENT_OUTSIDE_ISLANDS))
            e.setCancelled(true);
    }

    public boolean preventPlayerPickupItem(Item item, Player player, Flag... flags) {
        return !plugin.getNMSPlayers().wasThrownByPlayer(item, player) &&
                preventPlayerPickupItem(player, item.getLocation(), flags);
    }

    public boolean preventPlayerPickupItem(Player player, Location location, Flag... flags) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);
        Island island = plugin.getGrid().getIslandAt(location);
        return preventInteraction(island, location, superiorPlayer, IslandPrivileges.PICKUP_DROPS, flags);
    }

    /* PROJECTILE INTERACTS */

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPearlTeleport(PlayerTeleportEvent e) {
        if (e.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            Location teleportLocation = e.getTo();
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
            Island island = plugin.getGrid().getIslandAt(teleportLocation);

            if (preventInteraction(island, teleportLocation, superiorPlayer, Flag.PREVENT_OUTSIDE_ISLANDS)) {
                e.setCancelled(true);
                Message.TELEPORT_OUTSIDE_ISLAND.send(superiorPlayer);
                if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    BukkitItems.addItem(new ItemStack(Material.ENDER_PEARL), e.getPlayer().getInventory(),
                            e.getPlayer().getLocation());
                }
                return;
            }

            if (preventInteraction(island, teleportLocation, superiorPlayer, IslandPrivileges.ENDER_PEARL,
                    Flag.SEND_MESSAGES, Flag.PREVENT_OUTSIDE_ISLANDS)) {
                e.setCancelled(true);
                if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                    BukkitItems.addItem(new ItemStack(Material.ENDER_PEARL), e.getPlayer().getInventory(),
                            e.getPlayer().getLocation());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerFish(ProjectileLaunchEvent e) {
        if (e.getEntity() instanceof FishHook || (!ServerVersion.isLegacy() && e.getEntity() instanceof Trident)) {
            BukkitEntities.getPlayerSource(e.getEntity()).map(plugin.getPlayers()::getSuperiorPlayer).ifPresent(fisherPlayer -> {
                IslandPrivilege islandPrivilege = e.getEntity() instanceof FishHook ? IslandPrivileges.FISH :
                        IslandPrivileges.PICKUP_DROPS;
                Location entityLocation = e.getEntity().getLocation();
                Island island = plugin.getGrid().getIslandAt(entityLocation);
                if (preventInteraction(island, entityLocation, fisherPlayer, islandPrivilege,
                        Flag.SEND_MESSAGES, Flag.PREVENT_OUTSIDE_ISLANDS))
                    e.setCancelled(true);
            });
        }
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
                islandPrivilege = BukkitEntities.isMonster(e.getEntityType()) ?
                        IslandPrivileges.MONSTER_DAMAGE : BukkitEntities.isAnimal(e.getEntityType()) ?
                        IslandPrivileges.ANIMAL_DAMAGE : IslandPrivileges.BREAK;
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

            Island island = plugin.getGrid().getIslandAt(location);
            if (preventInteraction(island, location, shooterPlayer, islandPrivilege,
                    Flag.SEND_MESSAGES, Flag.PREVENT_OUTSIDE_ISLANDS)) {
                e.getEntity().remove();
                if (hitBlock != null) {
                    ICachedBlock cachedBlock = plugin.getNMSWorld().cacheBlock(hitBlock);
                    hitBlock.setType(Material.AIR);
                    BukkitExecutor.sync(() -> cachedBlock.setBlock(hitBlock.getLocation()), 1L);
                }
            }
        });
    }

    /* INTERNAL */

    private boolean preventInteraction(@Nullable Island island, Location location, SuperiorPlayer superiorPlayer,
                                       IslandPrivilege islandPrivilege, Flag... flags) {
        return preventInteraction(island, location, superiorPlayer, islandPrivilege,
                flags.length == 0 ? EnumSet.noneOf(Flag.class) : EnumSet.copyOf(Arrays.asList(flags)));
    }

    private boolean preventInteraction(@Nullable Island island, Location location, SuperiorPlayer superiorPlayer,
                                       IslandPrivilege islandPrivilege, EnumSet<Flag> flagsSet) {
        if (flagsSet.contains(Flag.PREVENT_OUTSIDE_ISLANDS) &&
                preventInteraction(island, location, superiorPlayer, flagsSet))
            return true;

        boolean sendMessages = flagsSet.contains(Flag.SEND_MESSAGES);

        if (island != null && !island.hasPermission(superiorPlayer, islandPrivilege)) {
            if (sendMessages)
                Message.PROTECTION.send(superiorPlayer);
            return true;
        }

        return false;
    }

    private boolean preventInteraction(@Nullable Island island, Location location, SuperiorPlayer superiorPlayer, Flag... flags) {
        return preventInteraction(island, location, superiorPlayer,
                flags.length == 0 ? EnumSet.noneOf(Flag.class) : EnumSet.copyOf(Arrays.asList(flags)));
    }

    private boolean preventInteraction(@Nullable Island island, Location location, SuperiorPlayer superiorPlayer,
                                       EnumSet<Flag> flagsSet) {
        if (superiorPlayer.hasBypassModeEnabled())
            return false;

        boolean sendMessages = flagsSet.contains(Flag.SEND_MESSAGES);

        if (island == null) {
            if (flagsSet.contains(Flag.PREVENT_OUTSIDE_ISLANDS)) {
                if (!superiorPlayer.hasBypassModeEnabled() && plugin.getGrid().isIslandsWorld(superiorPlayer.getWorld())) {
                    if (sendMessages)
                        Message.BUILD_OUTSIDE_ISLAND.send(superiorPlayer);
                    return true;
                }
            }

            return false;
        }

        if (!island.isInsideRange(location)) {
            if (sendMessages)
                Message.BUILD_OUTSIDE_ISLAND.send(superiorPlayer);
            return true;
        }

        if (island.isSpawn() && plugin.getSettings().getSpawn().isProtected()) {
            if (sendMessages)
                Message.PROTECTION.send(superiorPlayer);
            return true;
        }

        return false;
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

    private class PaperAttemptPickupListener implements Listener {

        @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
        public void onPlayerItemPickup(PlayerAttemptPickupItemEvent e) {
            if (preventPlayerPickupItem(e.getItem(), e.getPlayer(), Flag.SEND_MESSAGES, Flag.PREVENT_OUTSIDE_ISLANDS))
                e.setCancelled(true);
        }

    }

    private class PlayerArrowPickup implements Listener {

        @EventHandler
        public void onPlayerArrowPickup(PlayerPickupArrowEvent e) {
            if (preventPlayerPickupItem(e.getPlayer(), e.getPlayer().getLocation(), Flag.SEND_MESSAGES, Flag.PREVENT_OUTSIDE_ISLANDS))
                e.setCancelled(true);
        }

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

package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.island.SIslandChest;
import com.bgsoftware.superiorskyblock.utils.LocationUtils;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.entities.EntityUtils;
import com.bgsoftware.superiorskyblock.utils.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public final class UpgradesListener implements Listener {

    private final Set<UUID> alreadySet = new HashSet<>();
    private final Set<UUID> noRightClickTwice = new HashSet<>();
    private final SuperiorSkyblockPlugin plugin;

    public UpgradesListener(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
        Executor.sync(() -> {
            if(Bukkit.getPluginManager().isPluginEnabled("WildStacker"))
                Bukkit.getPluginManager().registerEvents(new WildStackerListener(), plugin);
        }, 1L);
    }

    /*
     *   SPAWNER RATES
     */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawn(SpawnerSpawnEvent e) {
        handleSpawnerSpawn(e.getSpawner());
    }

    private void handleSpawnerSpawn(CreatureSpawner spawner){
        if(spawner == null || spawner.getLocation() == null)
            return;

        Island island = plugin.getGrid().getIslandAt(spawner.getLocation());

        if(island == null)
            return;

        double spawnerRatesMultiplier = island.getSpawnerRatesMultiplier();

        if(spawnerRatesMultiplier > 1 && !alreadySet.contains(island.getOwner().getUniqueId())){
            alreadySet.add(island.getOwner().getUniqueId());
            Executor.sync(() -> {
                int spawnDelay = plugin.getNMSAdapter().getSpawnerDelay(spawner);
                if(spawnDelay > 0) {
                    plugin.getNMSAdapter().setSpawnerDelay(spawner, (int) Math.round(spawnDelay / spawnerRatesMultiplier));
                    Executor.sync(() -> alreadySet.remove(island.getOwner().getUniqueId()), 10L);
                }
            }, 5L);
        }
    }

    /*
     *   MOB DROPS
     */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent e) {
        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if(island == null)
            return;

        if(e.getEntity() instanceof Player)
            return;

        if(plugin.getSettings().dropsUpgradePlayersMultiply){
            EntityDamageEvent lastDamage = e.getEntity().getLastDamageCause();
            if(!(lastDamage instanceof EntityDamageByEntityEvent))
                return;

            EntityDamageByEntityEvent lastDamageEvent = (EntityDamageByEntityEvent) lastDamage;
            Entity damager = null;

            if(lastDamageEvent.getDamager() instanceof Player){
                damager = lastDamageEvent.getDamager();
            }
            else if(lastDamageEvent.getDamager() instanceof Projectile){
                ProjectileSource projectileSource = ((Projectile) lastDamageEvent.getDamager()).getShooter();
                if(projectileSource instanceof Player)
                    damager = (Player) projectileSource;
            }

            if(!(damager instanceof Player))
                return;
        }

        double mobDropsMultiplier = island.getMobDropsMultiplier();

        if(mobDropsMultiplier > 1){
            List<ItemStack> dropItems = new ArrayList<>(e.getDrops());
            for(ItemStack itemStack : dropItems){
                if(itemStack != null && !EntityUtils.isEquipment(e.getEntity(), itemStack) &&
                        !plugin.getNMSTags().getNBTTag(itemStack).getValue().containsKey("WildChests")) {
                    int newAmount = (int) (itemStack.getAmount() * mobDropsMultiplier);

                    if(Bukkit.getPluginManager().isPluginEnabled("WildStacker")){
                        itemStack.setAmount(newAmount);
                    }
                    else {
                        int stackAmounts = newAmount / itemStack.getMaxStackSize();
                        int leftOvers = newAmount % itemStack.getMaxStackSize();
                        boolean usedOriginal = false;

                        if (stackAmounts > 0) {
                            itemStack.setAmount(itemStack.getMaxStackSize());
                            usedOriginal = true;

                            ItemStack stackItem = itemStack.clone();
                            stackItem.setAmount(itemStack.getMaxStackSize());

                            for (int i = 0; i < stackAmounts - 1; i++)
                                e.getDrops().add(itemStack.clone());
                        }

                        if (leftOvers > 0) {
                            if (usedOriginal) {
                                ItemStack leftOversItem = itemStack.clone();
                                leftOversItem.setAmount(leftOvers);
                                e.getDrops().add(leftOversItem);
                            } else {
                                itemStack.setAmount(leftOvers);
                            }
                        }
                    }
                }
            }
        }

        EntityUtils.clearEntityEquipment(e.getEntity());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLastDamageEntity(EntityDamageEvent e){
        if(!(e.getEntity() instanceof LivingEntity))
            return;

        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if(island == null)
            return;

        EntityUtils.cacheEntityEquipment((LivingEntity) e.getEntity());
    }

    /*
     *   BLOCK LIMIT
     */

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCartPlaceMonitor(PlayerInteractEvent e){
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK || noRightClickTwice.contains(e.getPlayer().getUniqueId()) ||
                !e.getClickedBlock().getType().name().contains("RAIL") || e.getItem() == null ||
                !e.getItem().getType().name().contains("MINECART"))
            return;

        Island island = plugin.getGrid().getIslandAt(e.getClickedBlock().getLocation());

        if(island == null)
            return;

        noRightClickTwice.add(e.getPlayer().getUniqueId());
        Executor.sync(() -> noRightClickTwice.remove(e.getPlayer().getUniqueId()), 2L);

        switch (e.getItem().getType().name()){
            case "HOPPER_MINECART":
                island.handleBlockPlace(ConstantKeys.HOPPER, 1);
                break;
            case "COMMAND_MINECART":
            case "COMMAND_BLOCK_MINECART":
                island.handleBlockPlace(ServerVersion.isAtLeast(ServerVersion.v1_13) ?
                        ConstantKeys.COMMAND_BLOCK : ConstantKeys.COMMAND, 1);
                break;
            case "EXPLOSIVE_MINECART":
            case "TNT_MINECART":
                island.handleBlockPlace(ConstantKeys.TNT, 1);
                break;
            case "POWERED_MINECART":
            case "FURNACE_MINECART":
                island.handleBlockPlace(ConstantKeys.FURNACE, 1);
                break;
            case "STORAGE_MINECART":
            case "CHEST_MINECART":
                island.handleBlockPlace(ConstantKeys.CHEST, 1);
                break;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCartBreakMonitor(VehicleDestroyEvent e){
        if(!(e.getVehicle() instanceof Minecart))
            return;

        Island island = plugin.getGrid().getIslandAt(e.getVehicle().getLocation());

        if(island == null)
            return;

        island.handleBlockBreak(plugin.getNMSBlocks().getMinecartBlock((Minecart) e.getVehicle()), 1);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlockPlaced().getLocation());

        if(island == null)
            return;

        Key blockKey = Key.of(e.getBlock());

        if(island.hasReachedBlockLimit(blockKey)){
            e.setCancelled(true);
            Locale.REACHED_BLOCK_LIMIT.send(e.getPlayer(), StringUtils.format(blockKey.toString()));
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCartPlace(PlayerInteractEvent e) {
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK || noRightClickTwice.contains(e.getPlayer().getUniqueId()) ||
                !e.getClickedBlock().getType().name().contains("RAIL") || e.getItem() == null ||
                !e.getItem().getType().name().contains("MINECART"))
            return;

        Island island = plugin.getGrid().getIslandAt(e.getClickedBlock().getLocation());

        if(island == null)
            return;

        Key key = null;

        switch (e.getItem().getType().name()){
            case "HOPPER_MINECART":
                key = ConstantKeys.HOPPER;
                break;
            case "COMMAND_MINECART":
            case "COMMAND_BLOCK_MINECART":
                key = ServerVersion.isAtLeast(ServerVersion.v1_13) ? ConstantKeys.COMMAND_BLOCK : ConstantKeys.COMMAND;
                break;
            case "EXPLOSIVE_MINECART":
            case "TNT_MINECART":
                key = ConstantKeys.TNT;
                break;
            case "POWERED_MINECART":
            case "FURNACE_MINECART":
                key = ConstantKeys.FURNACE;
                break;
            case "STORAGE_MINECART":
            case "CHEST_MINECART":
                key = ConstantKeys.CHEST;
                break;
        }

        if(key != null && island.hasReachedBlockLimit(key)){
            e.setCancelled(true);
            Locale.REACHED_BLOCK_LIMIT.send(e.getPlayer(), StringUtils.format(key.getGlobalKey()));
        }
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getBlockClicked().getLocation());

        if(island == null)
            return;

        Key blockKey = Key.of(e.getBucket().name().replace("_BUCKET", ""));

        if(island.hasReachedBlockLimit(blockKey)){
            e.setCancelled(true);
            Locale.REACHED_BLOCK_LIMIT.send(e.getPlayer(), StringUtils.format(blockKey.toString()));
        }
    }

    /*
     *   ENTITY LIMIT
     */

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getLocation());

        if(island == null)
            return;

        if(!EntityUtils.canHaveLimit(e.getEntityType()))
            return;

        island.hasReachedEntityLimit(Key.of(e.getEntity())).whenComplete((result, ex) -> {
            if(result)
                e.getEntity().remove();
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent e){
        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if(island == null)
            return;

        if(!EntityUtils.canHaveLimit(e.getEntity().getType()))
            return;

        island.hasReachedEntityLimit(Key.of(e.getEntity())).whenComplete((result, ex) -> {
            if(result && e.getEntity().isValid() && !e.getEntity().isDead()) {
                e.getEntity().remove();
                if(e.getPlayer().getGameMode() != GameMode.CREATIVE)
                    e.getPlayer().getInventory().addItem(asItemStack(e.getEntity()));
            }
        });
    }

    @SuppressWarnings("UnstableApiUsage")
    private final Cache<Location, UUID> vehiclesOwners = CacheBuilder.newBuilder()
            .expireAfterWrite(2, TimeUnit.SECONDS).build();

    @EventHandler
    public void onVehicleSpawn(PlayerInteractEvent e) {
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK || noRightClickTwice.contains(e.getPlayer().getUniqueId()) ||
                e.getPlayer().getGameMode() == GameMode.CREATIVE || e.getItem() == null ||
                !e.getClickedBlock().getType().name().contains("RAIL") ||
                !e.getItem().getType().name().contains("MINECART"))
            return;

        Island island = plugin.getGrid().getIslandAt(e.getClickedBlock().getLocation());

        if(island == null)
            return;

        //noinspection UnstableApiUsage
        vehiclesOwners.put(e.getClickedBlock().getLocation(), e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleSpawn(VehicleCreateEvent e){
        if(!(e.getVehicle() instanceof Minecart))
            return;

        Island island = plugin.getGrid().getIslandAt(e.getVehicle().getLocation());

        if(island == null)
            return;

        //noinspection UnstableApiUsage
        UUID placedVehicle = vehiclesOwners.asMap().get(LocationUtils.getBlockLocation(e.getVehicle().getLocation()));

        if(!EntityUtils.canHaveLimit(e.getVehicle().getType()))
            return;

        island.hasReachedEntityLimit(Key.of(e.getVehicle())).whenComplete((result, ex) -> {
            if(result && e.getVehicle().isValid() && !e.getVehicle().isDead()) {
                e.getVehicle().remove();
                if(placedVehicle != null)
                    Bukkit.getPlayer(placedVehicle).getInventory().addItem(asItemStack(e.getVehicle()));
            }
        });
    }

    /*
     *   ISLAND CHEST
     */

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onIslandChestInteract(InventoryClickEvent e){
        InventoryHolder inventoryHolder = e.getView().getTopInventory() == null ? null : e.getView().getTopInventory().getHolder();

        if(!(inventoryHolder instanceof IslandChest))
            return;

        SIslandChest islandChest = (SIslandChest) inventoryHolder;

        if(islandChest.isUpdating()) {
            e.setCancelled(true);
        }

        else{
            islandChest.updateContents();
        }
    }

    private ItemStack asItemStack(Entity entity){
        if(entity instanceof Hanging){
            switch (entity.getType()){
                case ITEM_FRAME:
                    return new ItemStack(Material.ITEM_FRAME);
                case PAINTING:
                    return new ItemStack(Material.PAINTING);
            }
        }
        else if(entity instanceof Minecart) {
            Material material = Material.valueOf(plugin.getNMSBlocks().getMinecartBlock((Minecart) entity).getGlobalKey());
            switch (material.name()) {
                case "HOPPER":
                    return new ItemStack(Material.HOPPER_MINECART);
                case "COMMAND_BLOCK":
                    return new ItemStack(Material.valueOf("COMMAND_BLOCK_MINECART"));
                case "COMMAND":
                    return new ItemStack(Material.COMMAND_MINECART);
                case "TNT":
                    return new ItemStack(ServerVersion.isLegacy() ? Material.EXPLOSIVE_MINECART : Material.valueOf("TNT_MINECART"));
                case "FURNACE":
                    return new ItemStack(ServerVersion.isLegacy() ? Material.POWERED_MINECART : Material.valueOf("FURNACE_MINECART"));
                case "CHEST":
                    return new ItemStack(ServerVersion.isLegacy() ? Material.STORAGE_MINECART : Material.valueOf("CHEST_MINECART"));
                default:
                    return new ItemStack(Material.MINECART);
            }
        }

        throw new IllegalArgumentException("Cannot find an item for " + entity.getType());
    }

    private class WildStackerListener implements Listener {

        @EventHandler
        public void onWildStackerStackSpawn(com.bgsoftware.wildstacker.api.events.SpawnerStackedEntitySpawnEvent e){
            handleSpawnerSpawn(e.getSpawner());
        }

    }

}

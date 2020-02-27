package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.entities.EntityUtils;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;

import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public final class UpgradesListener implements Listener {

    private SuperiorSkyblockPlugin plugin;

    public UpgradesListener(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    /*
     *   SPAWNER RATES
     */

    private Set<UUID> alreadySet = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawn(SpawnerSpawnEvent e) {
        if(e.getSpawner() == null || e.getSpawner().getLocation() == null)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());

        if(island == null)
            return;

        double spawnerRatesMultiplier = island.getSpawnerRatesMultiplier();

        if(spawnerRatesMultiplier > 1){
            Executor.sync(() -> {
                if(!alreadySet.contains(island.getOwner().getUniqueId())) {
                    alreadySet.add(island.getOwner().getUniqueId());
                    e.getSpawner().setDelay((int) (
                            plugin.getNMSAdapter().getSpawnerDelay(e.getSpawner()) / spawnerRatesMultiplier));
                    e.getSpawner().update();
                    Executor.sync(() -> alreadySet.remove(island.getOwner().getUniqueId()), 10L);
                }
            }, 1L);
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

        double mobDropsMultiplier = island.getMobDropsMultiplier();

        if(mobDropsMultiplier > 1){
            for(ItemStack itemStack : e.getDrops()){
                if(itemStack != null && !EntityUtils.isEquipment(e.getEntity(), itemStack)) {
                    itemStack.setAmount((int) (itemStack.getAmount() * mobDropsMultiplier));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLastDamageArmorStand(EntityDamageEvent e){
        if(!(e.getEntity() instanceof ArmorStand))
            return;

        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if(island == null)
            return;

        EntityUtils.cacheArmorStandEquipment((ArmorStand) e.getEntity());
    }

    /*
     *   HOPPERS LIMIT
     */

    private Set<UUID> noRightClickTwice = new HashSet<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHopperCartPlaceMonitor(PlayerInteractEvent e){
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK || noRightClickTwice.contains(e.getPlayer().getUniqueId()) ||
                !e.getClickedBlock().getType().name().contains("RAIL") || e.getItem() == null || e.getItem().getType() != Material.HOPPER_MINECART)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getClickedBlock().getLocation());

        if(island == null)
            return;

        noRightClickTwice.add(e.getPlayer().getUniqueId());
        Executor.sync(() -> noRightClickTwice.remove(e.getPlayer().getUniqueId()), 2L);

        island.handleBlockPlace(Key.of("HOPPER"), 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHopperCartBreakMonitor(VehicleDestroyEvent e){
        if(!(e.getVehicle() instanceof HopperMinecart))
            return;

        Island island = plugin.getGrid().getIslandAt(e.getVehicle().getLocation());

        if(island == null)
            return;

        island.handleBlockBreak(Key.of("HOPPER"), 1);
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
    public void onHopperCartPlace(PlayerInteractEvent e) {
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK || noRightClickTwice.contains(e.getPlayer().getUniqueId()) ||
                !e.getClickedBlock().getType().name().contains("RAIL") || e.getItem() == null ||
                e.getItem().getType() != Material.HOPPER_MINECART)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getClickedBlock().getLocation());

        if(island == null)
            return;

        if(island.hasReachedBlockLimit(Key.of("HOPPER"))){
            e.setCancelled(true);
            Locale.REACHED_BLOCK_LIMIT.send(e.getPlayer(), StringUtils.format("hopper"));
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

}

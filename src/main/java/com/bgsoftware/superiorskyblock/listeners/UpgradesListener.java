package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.hooks.EconomyHook;
import com.bgsoftware.superiorskyblock.island.Island;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.wrappers.WrappedPlayer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.UnsafeValues;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("unused")
public class UpgradesListener implements Listener {

    private SuperiorSkyblock plugin;

    private Map<String, Byte> maxGrowthData = new HashMap<>();

    public UpgradesListener(SuperiorSkyblock plugin){
        this.plugin = plugin;
        maxGrowthData.put("BEETROOT_BLOCK", (byte) 3);
        maxGrowthData.put("NETHER_WARTS", (byte) 3);
    }

    /*
     *   getUpgradeCommands
     */

    /*
     *   MENU
     */

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        if(e.getClickedInventory() == null || !(e.getWhoClicked() instanceof Player) ||
                !e.getClickedInventory().getName().equals(plugin.getUpgrades().getTitle()))
            return;

        WrappedPlayer wrappedPlayer = WrappedPlayer.of(e.getWhoClicked());
        e.setCancelled(true);

        String upgradeName = plugin.getUpgrades().getUpgrade(e.getRawSlot());

        if(!upgradeName.isEmpty()){
            Island island;
            int level = (island = wrappedPlayer.getIsland()) == null ? 1 : island.getUpgradeLevel(upgradeName);
            double nextUpgradePrice = plugin.getUpgrades().getUpgradePrice(upgradeName, level);
            boolean hasNextLevel;

            if(EconomyHook.getMoneyInBank(wrappedPlayer) < nextUpgradePrice){
                Locale.NOT_ENOUGH_MONEY_TO_UPGRADE.send(wrappedPlayer);
                hasNextLevel = false;
            }

            else{
                List<String> commands = plugin.getUpgrades().getUpgradeCommands(upgradeName, level);

                EconomyHook.withdrawMoney(wrappedPlayer, nextUpgradePrice);
                for (String command : commands) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", wrappedPlayer.getName()));
                }

                hasNextLevel = true;
            }

            Sound sound = plugin.getUpgrades().getClickSound(upgradeName, level, hasNextLevel);
            if(sound != null)
                ((Player) e.getWhoClicked()).playSound(e.getWhoClicked().getLocation(), sound, 1, 1);

            e.getWhoClicked().closeInventory();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player) ||
                !e.getInventory().getName().equals(plugin.getUpgrades().getTitle()))
            return;

        plugin.getUpgrades().playCloseSound(WrappedPlayer.of(e.getPlayer()));
    }


    /*
     *   CROP GROWTH
     */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    @Deprecated
    @SuppressWarnings({"JavaReflectionMemberAccess", "JavaReflectionInvocation"})
    public void onGrow(BlockGrowEvent e) {
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());

        if(island == null)
            return;

        double cropGrowthMultiplier = island.getCropGrowthMultiplier();

        if(cropGrowthMultiplier > 1){
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                byte newData = (byte) (e.getBlock().getData() + cropGrowthMultiplier);
                if(newData > maxGrowthData.getOrDefault(e.getBlock().getType().name(), (byte) 7))
                    newData = maxGrowthData.getOrDefault(e.getBlock().getType().name(), (byte) 7);
                if(Bukkit.getBukkitVersion().contains("1.13")){
                    try {
                        Object blockData = UnsafeValues.class.getMethod("fromLegacy", Material.class, byte.class)
                                .invoke(Bukkit.getUnsafe(), e.getBlock().getType(), newData);
                        e.getBlock().getClass().getMethod("setBlockData", BlockData.class).invoke(e.getBlock(), blockData);
                    }catch(Exception ignored){}
                }else{
                    e.getBlock().setData(newData);
                }
                e.getBlock().getState().update();
            }, 2L);
        }
    }

    /*
     *   SPAWNER RATES
     */

    private Set<UUID> alreadySet = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSpawn(SpawnerSpawnEvent e) {
        Island island = plugin.getGrid().getIslandAt(e.getSpawner().getLocation());

        if(island == null)
            return;

        double spawnerRatesMultiplier = island.getSpawnerRatesMultiplier();

        if(spawnerRatesMultiplier > 1){
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if(!alreadySet.contains(island.getOwner().getUniqueId())) {
                    alreadySet.add(island.getOwner().getUniqueId());
                    e.getSpawner().setDelay((int) (
                            plugin.getNMSAdapter().getSpawnerDelay(e.getSpawner()) / spawnerRatesMultiplier));
                    e.getSpawner().update();
                    Bukkit.getScheduler().runTaskLater(plugin, () -> alreadySet.remove(island.getOwner().getUniqueId()), 10L);
                }
            }, 1L);
        }
    }

    /*
     *   MOB DROPS
     */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent e) {
        Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

        if(island == null)
            return;

        double mobDropsMultiplier = island.getMobDropsMultiplier();

        if(mobDropsMultiplier > 1){
            for(ItemStack itemStack : e.getDrops()){
                itemStack.setAmount((int) (itemStack.getAmount() * mobDropsMultiplier));
            }
        }
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
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,() -> noRightClickTwice.remove(e.getPlayer().getUniqueId()), 2L);

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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHopperPlace(BlockPlaceEvent e){
        if(e.getBlockPlaced().getType() != Material.HOPPER && e.getBlockPlaced().getType() != Material.HOPPER_MINECART)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getBlockPlaced().getLocation());

        if(island == null)
            return;

        int hoppersLimit = island.getHoppersLimit();

        if(hoppersLimit >= 0 && island.getHoppersAmount() >= hoppersLimit){
            e.setCancelled(true);
            Locale.REACHED_HOPPERS_LIMIT.send(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHopperCartPlace(PlayerInteractEvent e) {
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK || noRightClickTwice.contains(e.getPlayer().getUniqueId()) ||
                !e.getClickedBlock().getType().name().contains("RAIL") || e.getItem() == null ||
                e.getItem().getType() != Material.HOPPER_MINECART)
            return;

        Island island = plugin.getGrid().getIslandAt(e.getClickedBlock().getLocation());

        if(island == null)
            return;

        int hoppersLimit = island.getHoppersLimit();

        if(hoppersLimit >= 0 && island.getHoppersAmount() >= hoppersLimit){
            e.setCancelled(true);
            Locale.REACHED_HOPPERS_LIMIT.send(e.getPlayer());
        }
    }

}

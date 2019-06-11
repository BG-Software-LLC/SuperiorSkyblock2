package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.gui.GUIIdentifier;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unused")
public final class MenusListener implements Listener {

    private SuperiorSkyblockPlugin plugin;

    public MenusListener(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    private Map<UUID, ItemStack> latestClickedItem = new HashMap<>();

    /**
     * The following two events are here for patching a dupe glitch caused
     * by shift clicking and closing the inventory in the same time.
     */

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClickMonitor(InventoryClickEvent e){
        if(e.getCurrentItem() != null && e.isCancelled() && e.getInventory().getHolder() instanceof GUIIdentifier) {
            latestClickedItem.put(e.getWhoClicked().getUniqueId(), e.getCurrentItem());
            Bukkit.getScheduler().runTaskLater(plugin, () -> latestClickedItem.remove(e.getWhoClicked().getUniqueId()), 20L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryCloseMonitor(InventoryCloseEvent e){
        if(latestClickedItem.containsKey(e.getPlayer().getUniqueId())){
            ItemStack clickedItem = latestClickedItem.get(e.getPlayer().getUniqueId());
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                e.getPlayer().getInventory().removeItem(clickedItem);
                ((Player) e.getPlayer()).updateInventory();
            }, 1L);
        }
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent e){
        InventoryHolder inventoryHolder = e.getClickedInventory() == null ? null : e.getClickedInventory().getHolder();

        if(!(inventoryHolder instanceof SuperiorMenu) || !(e.getWhoClicked() instanceof Player))
            return;

        e.setCancelled(true);

        ((SuperiorMenu) inventoryHolder).onClick(e);
    }

    @EventHandler
    public void onMenuClose(InventoryCloseEvent e){
        InventoryHolder inventoryHolder = e.getInventory() == null ? null : e.getInventory().getHolder();

        if(!(inventoryHolder instanceof SuperiorMenu) || !(e.getPlayer() instanceof Player))
            return;

        ((SuperiorMenu) inventoryHolder).closeInventory(SSuperiorPlayer.of(e.getPlayer()));
    }

}

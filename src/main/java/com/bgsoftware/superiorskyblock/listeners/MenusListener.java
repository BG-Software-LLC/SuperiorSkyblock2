package com.bgsoftware.superiorskyblock.listeners;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.menu.StackedBlocksDepositMenu;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
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

    private final Map<UUID, ItemStack> latestClickedItem = new HashMap<>();
    private final SuperiorSkyblockPlugin plugin;

    public MenusListener(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    /**
     * The following two events are here for patching a dupe glitch caused
     * by shift clicking and closing the inventory in the same time.
     */

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClickMonitor(InventoryClickEvent e){
        if(e.getCurrentItem() != null && e.isCancelled() && e.getClickedInventory().getHolder() instanceof SuperiorMenu) {
            latestClickedItem.put(e.getWhoClicked().getUniqueId(), e.getCurrentItem());
            Executor.sync(() -> latestClickedItem.remove(e.getWhoClicked().getUniqueId()), 20L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryCloseMonitor(InventoryCloseEvent e){
        if(latestClickedItem.containsKey(e.getPlayer().getUniqueId())){
            ItemStack clickedItem = latestClickedItem.get(e.getPlayer().getUniqueId());
            Executor.sync(() -> {
                e.getPlayer().getInventory().removeItem(clickedItem);
                ((Player) e.getPlayer()).updateInventory();
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMenuClick(InventoryClickEvent e){
        if(!(e.getWhoClicked() instanceof Player) || e.getView().getTopInventory() == null ||
                e.getClickedInventory() == null)
            return;

        InventoryHolder inventoryHolder = e.getView().getTopInventory().getHolder();

        if(inventoryHolder instanceof SuperiorMenu) {
            e.setCancelled(true);

            if (e.getClickedInventory().equals(e.getView().getTopInventory()))
                ((SuperiorMenu) inventoryHolder).onClick(e);
        }
        else if(inventoryHolder instanceof StackedBlocksDepositMenu){
            ((StackedBlocksDepositMenu) inventoryHolder).onInteract(e);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMenuClose(InventoryCloseEvent e){
        InventoryHolder inventoryHolder = e.getInventory() == null ? null : e.getInventory().getHolder();

        if(!(e.getPlayer() instanceof Player))
            return;

        if(inventoryHolder instanceof SuperiorMenu) {
            ((SuperiorMenu) inventoryHolder).closeInventory(plugin.getPlayers().getSuperiorPlayer(e.getPlayer()));
        }
        else if(inventoryHolder instanceof StackedBlocksDepositMenu){
            ((StackedBlocksDepositMenu) inventoryHolder).onClose(e);
        }
    }

}

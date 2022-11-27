package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.core.collections.AutoRemovalMap;
import com.bgsoftware.superiorskyblock.core.menu.impl.internal.StackedBlocksDepositMenu;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"rawtypes", "unchecked"})
public class MenusListener implements Listener {

    private final Map<UUID, ItemStack> latestClickedItem = AutoRemovalMap.newHashMap(1, TimeUnit.SECONDS);

    /*
     * The following two events are here for patching a dupe glitch caused
     * by shift clicking and closing the inventory in the same time.
     */

    @EventHandler(priority = EventPriority.MONITOR)
    private void onInventoryClickMonitor(InventoryClickEvent e) {
        if (e.getCurrentItem() != null && e.isCancelled() && e.getClickedInventory().getHolder() instanceof MenuView) {
            latestClickedItem.put(e.getWhoClicked().getUniqueId(), e.getCurrentItem());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onInventoryCloseMonitor(InventoryCloseEvent e) {
        ItemStack clickedItem = latestClickedItem.get(e.getPlayer().getUniqueId());
        if (clickedItem != null) {
            BukkitExecutor.sync(() -> {
                e.getPlayer().getInventory().removeItem(clickedItem);
                ((Player) e.getPlayer()).updateInventory();
            }, 1L);
        }
    }

    /* MENU INTERACTIONS HANDLING */


    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onMenuClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player) || e.getView().getTopInventory() == null ||
                e.getClickedInventory() == null)
            return;

        InventoryHolder inventoryHolder = e.getView().getTopInventory().getHolder();

        if (inventoryHolder instanceof MenuView) {
            e.setCancelled(true);

            if (e.getClickedInventory().equals(e.getView().getTopInventory())) {
                MenuView menuView = (MenuView) inventoryHolder;
                menuView.getMenu().onClick(e, menuView);
            }
        } else if (inventoryHolder instanceof StackedBlocksDepositMenu) {
            ((StackedBlocksDepositMenu) inventoryHolder).onInteract(e);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onMenuClose(InventoryCloseEvent e) {
        InventoryHolder inventoryHolder = e.getInventory() == null ? null : e.getInventory().getHolder();

        if (!(e.getPlayer() instanceof Player))
            return;

        if (inventoryHolder instanceof MenuView) {
            MenuView menuView = (MenuView) inventoryHolder;
            menuView.getMenu().onClose(e, menuView);
        } else if (inventoryHolder instanceof StackedBlocksDepositMenu) {
            ((StackedBlocksDepositMenu) inventoryHolder).onClose(e);
        }
    }

}
